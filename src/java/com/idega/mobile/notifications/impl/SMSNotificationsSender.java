package com.idega.mobile.notifications.impl;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.ws.rs.HttpMethod;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.idegaweb.DefaultIWBundle;
import com.idega.mobile.MobileConstants;
import com.idega.mobile.bean.Notification;
import com.idega.mobile.data.NotificationSubscription;
import com.idega.mobile.notifications.NotificationsSender;
import com.idega.restful.util.ConnectionUtil;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SMSNotificationsSender extends NotificationsSender {

	@Override
	public boolean doSendNotification(Notification notification, Map<Locale, String> messages, Map<Locale, List<NotificationSubscription>> groupedSubscriptions) {
		String smsAPIURL = getApplicationProperty("mobile.sms_api_url", "https://api.smsapi.com/sms.do?username={username}&password={password}&from={from}&to={to}&message={message}");
		if (getSettings().getBoolean("moble.sms_api_test", !DefaultIWBundle.isProductionEnvironment())) {
			smsAPIURL = smsAPIURL.concat("&test=1");
		}

		String username = getApplicationProperty("mobile.sms_api_username", CoreConstants.EMPTY);
		String password = getApplicationProperty("mobile.sms_api_password_md5", CoreConstants.EMPTY);
		String from = getApplicationProperty("mobile.sms_api_from", "Info");

		for (Locale locale: groupedSubscriptions.keySet()) {
			List<NotificationSubscription> localizedSubscriptions = groupedSubscriptions.get(locale);
			if (ListUtil.isEmpty(localizedSubscriptions)) {
				getLogger().warning("There are no messages for locale " + locale + ". All subscriptions: " + groupedSubscriptions);
				continue;
			}

			String message = getMessage(notification, messages, locale, localizedSubscriptions);
			if (StringUtil.isEmpty(message)) {
				getLogger().warning("Message is not provided for locale " + locale + ". All messages: " + messages);
				continue;
			}

			List<String> devices = new ArrayList<String>();
			for (NotificationSubscription subscription: localizedSubscriptions) {
				devices.add(subscription.getToken());
			}
			if (ListUtil.isEmpty(devices)) {
				getLogger().warning("There are no tokens for localized subscriptions: " + localizedSubscriptions);
				continue;
			}

			if (message.length() > 159) {
				message = message.substring(0, 157).concat(CoreConstants.DOT).concat(CoreConstants.DOT).concat(CoreConstants.DOT);
			}
			try {
				message = URLEncoder.encode(message, CoreConstants.ENCODING_UTF8);
			} catch (Exception e) {
				getLogger().log(Level.WARNING, "Error encoding message '" + message + "'", e);
			}
			for (String device: devices) {
				List<AdvancedProperty> pathParams = new ArrayList<>();
				pathParams.add(new AdvancedProperty("username", username, "username"));
				pathParams.add(new AdvancedProperty("password", password, "password"));
				pathParams.add(new AdvancedProperty("from", from, "from"));
				pathParams.add(new AdvancedProperty("to", device, "to"));
				pathParams.add(new AdvancedProperty("message", message, "message"));
				ClientResponse response = ConnectionUtil.getInstance().getResponseFromREST(smsAPIURL, null, MimeTypeUtil.MIME_TYPE_TEXT_PLAIN, HttpMethod.GET, null, null, pathParams);
				if (response == null || response.getStatus() != Status.OK.getStatusCode()) {
					getLogger().warning("Response is unknown (" + response + ") or not OK: " + (response == null ? "unknown" : response.getStatus()) + " when calling " + smsAPIURL + " with params " + pathParams);
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public String getSupportedDeviceType() {
		return MobileConstants.DEVICE_SMS;
	}

	@Override
	public String getKeyStore() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}

}