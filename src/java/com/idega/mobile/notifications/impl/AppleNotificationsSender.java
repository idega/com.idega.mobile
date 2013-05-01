package com.idega.mobile.notifications.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotifications;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.mobile.MobileConstants;
import com.idega.mobile.bean.Notification;
import com.idega.mobile.data.NotificationSubscription;
import com.idega.mobile.notifications.NotificationsSender;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AppleNotificationsSender extends NotificationsSender {

	@Override
	public boolean doSendNotification(Notification notification, Map<Locale, String> messages,
			Map<Locale, List<NotificationSubscription>> groupedSubscriptions) {

		//	Settings
		IWMainApplicationSettings settings = getApplication().getSettings();
		boolean production = settings.getBoolean("ios_push_production", !isDevelopementState());
		String keystore = getKeyStore();
		if (StringUtil.isEmpty(keystore)) {
			getLogger().warning("Invalid path to keystore");
			return false;
		}
		File tmp = new File(keystore);
		if (!tmp.exists()) {
			getLogger().warning("Keystore at " + keystore + " does not exist");
			return false;
		}
		String password = getPassword();
		if (StringUtil.isEmpty(password)) {
			getLogger().warning("Password for keystore is invalid");
			return false;
		}

		String badgeValue = settings.getProperty("notification_badge", "0");
		int badge = -1;
		if (!StringUtil.isEmpty(badgeValue)) {
			try {
				badge = Integer.valueOf(badgeValue);
			} catch (NumberFormatException e) {}
		}
		String sound = settings.getProperty("notification_sound", "default");

		//	Sending messages
		for (Locale locale: groupedSubscriptions.keySet()) {
			List<NotificationSubscription> localizedSubscriptions = groupedSubscriptions.get(locale);
			if (ListUtil.isEmpty(localizedSubscriptions)) {
				getLogger().warning("There are no messages for locale " + locale + ". All subscriptions: " + groupedSubscriptions);
				continue;
			}

			String message = messages.get(locale);
			if (StringUtil.isEmpty(message)) {
				getLogger().warning("Message is not provided for locale " + locale + ". All messages: " + messages);
				continue;
			}

			List<String> devices = new ArrayList<String>();
			for (NotificationSubscription subscription: localizedSubscriptions)
				devices.add(subscription.getToken());
			if (ListUtil.isEmpty(devices)) {
				getLogger().warning("There are no tokens for localized subscriptions: " + localizedSubscriptions);
				continue;
			}

			PushedNotifications sent = null;
			try {
				getLogger().info("Sending message '" + message + "' to device(s): " + devices + ". Subscriptions: " + localizedSubscriptions);

				if (settings.getBoolean("notification_send_test", Boolean.FALSE)) {
					sent = Push.test(keystore, password, production, devices);
				} else {
					PushNotificationPayload payload = PushNotificationPayload.complex();
					payload.addAlert(message);
					if (badge != -1)
						payload.addBadge(badge);
					if (!StringUtil.isEmpty(sound))
						payload.addSound(sound);
					payload.addCustomDictionary("id", new Random().nextInt(Integer.MAX_VALUE));
					Map<String, String> dictionaries = notification.getDictionaries();
					if (!MapUtil.isEmpty(dictionaries)) {
						for (Map.Entry<String, String> dictionary: dictionaries.entrySet()) {
							payload.addCustomDictionary(dictionary.getKey(), dictionary.getValue());
						}
					}

					sent = Push.payload(payload, keystore, password, production, devices);
				}
			} catch (CommunicationException e) {
				String errorMessage = "Error sending message '" + message + "' to devices " + devices;
				getLogger().log(Level.WARNING, errorMessage, e);
				CoreUtil.sendExceptionNotification(errorMessage, e);
				return false;
			} catch (KeystoreException e) {
				String errorMessage = "Error while trying to intercept with keystore at " + keystore;
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
					} catch (Exception e) {
						getLogger().log(Level.WARNING, "Failed to log status about notification " + notification + ", message: " + messages +
								" addressed to " + groupedSubscriptions, e);
					}
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