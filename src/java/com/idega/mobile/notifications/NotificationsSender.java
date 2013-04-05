package com.idega.mobile.notifications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.idega.core.business.DefaultSpringBean;
import com.idega.mobile.bean.Notification;
import com.idega.mobile.data.NotificationSubscription;
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

	public abstract boolean doSendNotification(Notification notification, Map<Locale, String> messages,
			Map<Locale, List<NotificationSubscription>> groupedSubscriptions);

	public abstract String getSupportedDeviceType();

	public abstract String getKeyStore();

	public abstract String getPassword();
}