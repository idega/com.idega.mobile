package com.idega.mobile.notifications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.idega.core.business.DefaultSpringBean;
import com.idega.mobile.bean.Notification;
import com.idega.mobile.data.NotificationSubscription;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.LocaleUtil;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;

public abstract class NotificationsSender extends DefaultSpringBean {

	public boolean doSendNotification(Notification notification, List<NotificationSubscription> subscriptions) {
		if (notification == null || ListUtil.isEmpty(subscriptions)) {
			getLogger().warning("Either notification (" + notification + ") or subscriptions (" + subscriptions + ") are not provided");
			return false;
		}

		Map<Locale, String> messages = notification.getMessages();
		if (MapUtil.isEmpty(messages)) {
			getLogger().warning("There are no messages to send");
			return false;
		}

		Map<Locale, List<NotificationSubscription>> groupedSubscriptions = new HashMap<Locale, List<NotificationSubscription>>();
		for (NotificationSubscription subscription: subscriptions) {
			Locale locale = null;

			String localeId = subscription.getLocale();
			if (!StringUtil.isEmpty(localeId))
				locale = LocaleUtil.getLocale(localeId);
			if (locale == null)
				locale = Locale.ENGLISH;

			List<NotificationSubscription> group = groupedSubscriptions.get(locale);
			if (group == null) {
				group = new ArrayList<NotificationSubscription>();
				groupedSubscriptions.put(locale, group);
			}
			group.add(subscription);
		}

		return doSendNotification(notification, messages, groupedSubscriptions);
	}

	public abstract boolean doSendNotification(Notification notification, Map<Locale, String> messages, Map<Locale, List<NotificationSubscription>> groupedSubscriptions);

	public abstract String getSupportedDeviceType();

	public abstract String getKeyStore();

	public abstract String getPassword();

	protected String getMessage(Notification notification, Map<Locale, String> messages, Locale subscribedLocale, List<NotificationSubscription> groupedSubscriptions) {
		if (notification == null) {
			getLogger().warning("Unknown notification. Messages: " + messages + ", client's locale: " + subscribedLocale + ", subscriptions: " + groupedSubscriptions);
			return null;
		}
		if (subscribedLocale == null) {
			getLogger().warning("Unknown locale for client. Notification: " + notification + ", messages: " + messages + ", subscriptions: " + groupedSubscriptions);
			return null;
		}

		Locale tmpLocale = null;
		String msg = messages.get(subscribedLocale);
		if (StringUtil.isEmpty(msg) && !MapUtil.isEmpty(messages)) {
			for (Iterator<Locale> localesIter = messages.keySet().iterator(); (StringUtil.isEmpty(msg) && localesIter.hasNext());) {
				tmpLocale = localesIter.next();
				if (tmpLocale.getLanguage().equals(subscribedLocale.getLanguage())) {
					msg = messages.get(tmpLocale);
				} else {
					msg = null;
					tmpLocale = null;
				}
			}
		}
		if (StringUtil.isEmpty(msg)) {
			getLogger().warning("There is no message for locale " + subscribedLocale + (tmpLocale == null ? CoreConstants.EMPTY : " and locale " + tmpLocale) + ". All messages: " + messages + ", subscriptions: " +
					groupedSubscriptions + ", notification: " + notification);
			return null;
		}

		return msg;
	}

	protected boolean isNeededToInformAboutNotificationToSend() {
		return getSettings().getBoolean("push_notif.print_msg_to_send", false);
	}

}