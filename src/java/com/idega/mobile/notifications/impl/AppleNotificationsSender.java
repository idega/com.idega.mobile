package com.idega.mobile.notifications.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.core.business.DefaultSpringBean;
import com.idega.mobile.MobileConstants;
import com.idega.mobile.bean.Notification;
import com.idega.mobile.data.NotificationSubscription;
import com.idega.mobile.notifications.NotificationsSender;
import com.idega.util.IOUtil;
import com.idega.util.ListUtil;
import com.idega.util.LocaleUtil;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AppleNotificationsSender extends DefaultSpringBean implements NotificationsSender {

	private KeyStore keyStore;

	private Map<String, X509Certificate> certificatesCache = new HashMap<String, X509Certificate>();

	private String getPassword() {
		return "sdslj356fdgopewrdf4xgmfdngr";
	}

	private KeyStore getKeyStore() {
		if (keyStore != null)
			return keyStore;

		String alias = "munizapp";

		X509Certificate certificate = getCertificate(alias, getPassword());
		if (certificate == null) {
			return null;
		}

		try {
			File f = new File(alias.concat(MobileConstants.CERTIFICATE_FILE_ENDING));
			if (!f.exists())
				f.createNewFile();

			InputStream streamToStoreFile = new FileInputStream(f);
			keyStore = initializeKeyStore(streamToStoreFile, getPassword());
			return keyStore;
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error loading key store for: " + alias, e);
		}

		return null;
	}

	private KeyStore initializeKeyStore(InputStream streamToStoreFile, String password) {
		KeyStore store = null;
		try {
			store = KeyStore.getInstance(MobileConstants.SECURITY_TYPE, MobileConstants.SECURITY_PROVIDER_BOUNCY_CASTLE);
		} catch (KeyStoreException e) {
			getLogger().log(Level.SEVERE, "Error initiating KeyStore!", e);
		} catch (NoSuchProviderException e) {
			getLogger().log(Level.SEVERE, "There is no such provider: " + MobileConstants.SECURITY_PROVIDER_BOUNCY_CASTLE, e);
		}

		if (store == null) {
			return null;
		}

		try {
			store.load(streamToStoreFile, StringUtil.isEmpty(password) ? null : password.toCharArray());
		} catch (NoSuchAlgorithmException e) {
			getLogger().log(Level.SEVERE, "There is no such algorithm: " + MobileConstants.SECURITY_ALGORITHM_RSA, e);
			return null;
		} catch (CertificateException e) {
			getLogger().log(Level.SEVERE, "There was a problem with certificate", e);
			return null;
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "There was an error while loading key store", e);
			return null;
		} finally {
			IOUtil.close(streamToStoreFile);
		}

		return store;
	}

	private X509Certificate getCertificate(String alias, String password) {
		if (StringUtil.isEmpty(alias)) {
			getLogger().warning("Invalid alias for certificate!");
			return null;
		}

		X509Certificate peerCertificate = certificatesCache.get(alias);
		if (peerCertificate != null)
			return peerCertificate;

		peerCertificate = getInitializedCertificate(alias, password);
		if (peerCertificate != null)
			certificatesCache.put(alias, peerCertificate);

		return peerCertificate;
	}

	private X509Certificate getInitializedCertificate(String alias, String password) {
		X509Certificate certificate = new sun.security.x509.X509CertImpl();
		return certificate;
//		if (CERTIFICATE_AUTHORITY == null) {
//			try {
//				CERTIFICATE_AUTHORITY = CAToolImpl.getCATool(alias, StringUtil.isEmpty(password) ? Constants.EMPTY.toCharArray() : password.toCharArray());
//			} catch (Exception e) {
//				LOGGER.log(Level.SEVERE, "There was an error while loading Certificate Authority", e);
//			}
//		}
//		return CERTIFICATE_AUTHORITY;
	}

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

			try {
				KeyStore keyStore = getKeyStore();
				if (keyStore == null) {
					getLogger().warning("Invalid key store");
					return false;
				}

				Push.alert(message, keyStore, getPassword(), false, devices);
			} catch (CommunicationException e) {
				getLogger().log(Level.WARNING, "Error sending message '" + message + "' to devices " + devices, e);
				return false;
			} catch (KeystoreException e) {
				getLogger().log(Level.WARNING, "Error while trying to intercept with keystore", e);
				return false;
			}
		}

		return true;
	}

	@Override
	public String getSupportedDeviceType() {
		return MobileConstants.DEVICE_IOS;
	}

}