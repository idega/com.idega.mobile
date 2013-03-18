package com.idega.mobile.notifications.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.PushedNotifications;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.core.business.DefaultSpringBean;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.mobile.MobileConstants;
import com.idega.mobile.bean.Notification;
import com.idega.mobile.data.NotificationSubscription;
import com.idega.mobile.notifications.NotificationsSender;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.LocaleUtil;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AppleNotificationsSender extends DefaultSpringBean implements NotificationsSender {

	@Override
	public boolean doSendNotification(Notification notification, List<NotificationSubscription> subscriptions) {
		if (notification == null || ListUtil.isEmpty(subscriptions))
			return false;

		Map<Locale, String> messages = notification.getMessages();
		if (MapUtil.isEmpty(messages))
			return false;

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

		for (Locale locale: groupedSubscriptions.keySet()) {
			List<NotificationSubscription> localizedSubscriptions = groupedSubscriptions.get(locale);
			if (ListUtil.isEmpty(localizedSubscriptions))
				continue;

			String message = messages.get(locale);
			if (StringUtil.isEmpty(message))
				continue;

			List<String> devices = new ArrayList<String>();
			for (NotificationSubscription subscription: localizedSubscriptions)
				devices.add(subscription.getToken());
			if (ListUtil.isEmpty(devices))
				continue;

			IWMainApplicationSettings settings = getApplication().getSettings();
			boolean production = settings.getBoolean("ios_push_production", !isDevelopementState());
			String keyStore = getKeyStore();
			if (StringUtil.isEmpty(keyStore)) {
				getLogger().warning("Invalid path to keystore");
				return false;
			}
			File tmp = new File(keyStore);
			if (!tmp.exists()) {
				getLogger().warning("Keystore at " + keyStore + " does not exist");
				return false;
			}
			String password = getPassword();
			if (StringUtil.isEmpty(password)) {
				getLogger().warning("Password for keystore is invalid");
				return false;
			}

			String badgeValue = settings.getProperty("notification_badge");
			int badge = -1;
			if (!StringUtil.isEmpty(badgeValue)) {
				try {
					badge = Integer.valueOf(badgeValue);
				} catch (NumberFormatException e) {}
			}
			String sound = settings.getProperty("notification_sound");

			PushedNotifications sent = null;
			try {
				if (settings.getBoolean("notification_send_test", Boolean.FALSE)) {
					sent = Push.test(keyStore, password, production, devices);
				} else {
					if (badge != -1 && !StringUtil.isEmpty(sound))
						sent = Push.combined(message, badge, sound, keyStore, password, production, devices);
					else
						sent = Push.alert(message, keyStore, password, production, devices);
				}
			} catch (CommunicationException e) {
				String errorMessage = "Error sending message '" + message + "' to devices " + devices;
				getLogger().log(Level.WARNING, errorMessage, e);
				CoreUtil.sendExceptionNotification(errorMessage, e);
				return false;
			} catch (KeystoreException e) {
				String errorMessage = "Error while trying to intercept with keystore at " + keyStore;
				getLogger().log(Level.WARNING, errorMessage, e);
				CoreUtil.sendExceptionNotification(errorMessage, e);
				return false;
			} catch (Exception e) {
				String errorMessage = "Error sending message '" + message + "' to devices " + devices;
				getLogger().log(Level.WARNING, errorMessage, e);
				CoreUtil.sendExceptionNotification(errorMessage, e);
				return false;
			} finally {
				if (sent == null) {
					getLogger().warning("Failed to sent notification " + message + " for devices " + devices);
				} else {
					try {
						PushedNotifications failed = sent.getFailedNotifications();
						PushedNotifications succeeded = sent.getSuccessfulNotifications();
						getLogger().info("Failed: " + failed + "\nSucceeded: " + succeeded);
					} catch (Exception e) {}
				}
			}
		}

		return true;
	}

	@Override
	public String getSupportedDeviceType() {
		return MobileConstants.DEVICE_IOS;
	}

	@Override
	public String getKeyStore() {
		return getApplication().getSettings().getProperty("ios_keystore");
	}

	@Override
	public String getPassword() {
		return getApplication().getSettings().getProperty("ios_keystore_psw");
	}

}