package com.idega.mobile.notifications;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.idega.core.business.DefaultSpringBean;
import com.idega.mobile.bean.Notification;
import com.idega.mobile.data.MobileDAO;
import com.idega.mobile.data.NotificationSubscription;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class NotificationsCenter extends DefaultSpringBean {

	@Autowired
	private MobileDAO mobileDAO;

	private Collection<NotificationsSender> getNotificationsSenders() {
		Map<?, ?> beans = WebApplicationContextUtils.getWebApplicationContext(getApplication().getServletContext())
				.getBeansOfType(NotificationsSender.class);

		if (MapUtil.isEmpty(beans))
			return Collections.emptyList();

		@SuppressWarnings("unchecked")
		Collection<NotificationsSender> senders = (Collection<NotificationsSender>) beans.values();
		return senders;
	}

	private Map<String, List<NotificationSubscription>> getSubscriptions(String notificationObject, List<Integer> subscribersToExclude) {
		if (StringUtil.isEmpty(notificationObject)) {
			getLogger().warning("Notification object is not provided");
			return Collections.emptyMap();
		}

		List<NotificationSubscription> subscriptions = ListUtil.isEmpty(subscribersToExclude) ?
				mobileDAO.getSubscriptionsForObject(notificationObject) :
				mobileDAO.getSubscriptionsForObjectExcludingUsers(notificationObject, subscribersToExclude);
		if (ListUtil.isEmpty(subscriptions))
			return Collections.emptyMap();

		return getSubscriptions(subscriptions);
	}

	private Map<String, List<NotificationSubscription>> getSubscriptions(List<NotificationSubscription> subscriptions) {
		Map<String, List<NotificationSubscription>> groupedSubscriptions = new HashMap<String, List<NotificationSubscription>>();
		for (NotificationSubscription subscription: subscriptions) {
			String device = subscription.getDevice();
			if (StringUtil.isEmpty(device)) {
				getLogger().warning("Unknown device type for subscription " + subscription);
				continue;
			}

			List<NotificationSubscription> group = groupedSubscriptions.get(device);
			if (group == null) {
				group = new ArrayList<NotificationSubscription>();
				groupedSubscriptions.put(device, group);
			}
			group.add(subscription);
		}

		return groupedSubscriptions;
	}

	public boolean doSubscribe(Integer userId, String token, Locale locale, String objectId, String device) {
		try {
			return mobileDAO.doCreateSubscription(userId, token, locale, objectId, device) != null;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error subscribing user " + userId + " to " + objectId + " by token " + token + " and locale " + locale, e);
		}
		return false;
	}

	public boolean doUnSubscribe(Integer userId, String token, String objectId) {
		try {
			return mobileDAO.doDeleteSubscription(userId, token, objectId);
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error unsubscribing from " + objectId + " user " + userId + " device: " + token);
		}
		return false;
	}

	public boolean doSendNotification(Notification notification) {
		if (notification == null) {
			getLogger().warning("Notification is not provided");
			return false;
		}

		Map<String, List<NotificationSubscription>> subscriptions = ListUtil.isEmpty(notification.getSubscriptions()) ?
				getSubscriptions(notification.getNotifyOn(), notification.getExclusions()) :
				getSubscriptions(notification.getSubscriptions());
		if (MapUtil.isEmpty(subscriptions)) {
			getLogger().warning("No devices subscribed for " + notification);
			return false;
		}

		Collection<NotificationsSender> senders = getNotificationsSenders();
		if (ListUtil.isEmpty(senders))
			return false;

		for (NotificationsSender sender: senders) {
			List<NotificationSubscription> subscribers = subscriptions.get(sender.getSupportedDeviceType());
			if (ListUtil.isEmpty(subscribers))
				continue;

			sender.doSendNotification(notification, subscribers);
		}

		return true;
	}

	public boolean isSubscribed(Integer userId, String token, String objectId) {
		try {
			return mobileDAO.getNotificationSubscription(userId, token, objectId) != null;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error verifying whether user (ID: " + userId + ") is subscribed to " + objectId + " by token " + token, e);
		}
		return false;
	}

}