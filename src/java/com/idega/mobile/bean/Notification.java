package com.idega.mobile.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.idega.mobile.data.NotificationSubscription;

public class Notification {

	private Integer[] exclusions;

	private Map<Locale, String> messages;

	private String notifyOn;

	private List<NotificationSubscription> subscriptions;

	private Map<String, String> dictionaries;

	public Notification(Map<Locale, String> messages, List<NotificationSubscription> subscriptions) {
		super();

		this.messages = messages;
		this.subscriptions = subscriptions;
	}

	public Notification(Map<Locale, String> messages, String notifyOn, List<NotificationSubscription> subscriptions) {
		this(messages, subscriptions);

		this.notifyOn = notifyOn;
	}

	public Integer[] getExclusions() {
		return exclusions;
	}

	public void setExclusions(Integer[] exclusions) {
		this.exclusions = exclusions;
	}

	public Map<Locale, String> getMessages() {
		return messages;
	}

	public void setMessages(Map<Locale, String> messages) {
		this.messages = messages;
	}

	public String getNotifyOn() {
		return notifyOn;
	}

	public void setNotifyOn(String notifyOn) {
		this.notifyOn = notifyOn;
	}

	public List<NotificationSubscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(List<NotificationSubscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public Map<String, String> getDictionaries() {
		return dictionaries;
	}

	public void setDictionaries(Map<String, String> dictionaries) {
		this.dictionaries = dictionaries;
	}

	public void addDictionary(String name, String value) {
		if (dictionaries == null)
			dictionaries = new HashMap<String, String>();
		dictionaries.put(name, value);
	}

	@Override
	public String toString() {
		return "Notification on: " + getNotifyOn() + ", messages: " + getMessages() + ", receivers: " + getSubscriptions();
	}

}
