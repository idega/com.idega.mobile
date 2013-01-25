package com.idega.mobile.bean;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.idega.mobile.data.NotificationSubscription;

public class Notification {

	private List<Integer> exclusions;

	private Map<Locale, String> messages;

	private String notifyOn;

	private List<NotificationSubscription> subscriptions;

	public Notification(Map<Locale, String> messages, List<NotificationSubscription> subscriptions) {
		super();

		this.messages = messages;
		this.subscriptions = subscriptions;
	}

	public Notification(Map<Locale, String> messages, String notifyOn) {
		super();

		this.messages = messages;
		this.notifyOn = notifyOn;
	}

	public Notification(Map<Locale, String> messages, String notifyOn, List<Integer> exclusions) {
		this(messages, notifyOn);

		this.exclusions = exclusions;
	}

	public List<Integer> getExclusions() {
		return exclusions;
	}

	public void setExclusions(List<Integer> exclusions) {
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

	@Override
	public String toString() {
		return "Notification on: " + getNotifyOn() + ", excluding users: " + getExclusions() + ", messages: " + getMessages();
	}

}
