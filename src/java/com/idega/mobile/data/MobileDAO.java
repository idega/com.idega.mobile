package com.idega.mobile.data;

import java.util.List;
import java.util.Locale;

import com.idega.core.persistence.GenericDao;

public interface MobileDAO extends GenericDao {

	public List<NotificationSubscription> getSubscriptionsForObject(String objectId);
	public List<NotificationSubscription> getSubscriptionsForObjectExcludingUsers(String objectId, List<Integer> userIds);

	public List<NotificationSubscription> getSubscriptions(List<String> tokens, String objectId);

	public NotificationSubscription getNotificationSubscription(Integer userId, String token, String objectId);
	public List<NotificationSubscription> getNotificationSubscriptions(Integer userId, String objectId);

	public NotificationSubscription doCreateSubscription(Integer userId, String token, Locale locale, String objectId, String device);
	public boolean doDeleteSubscription(Integer userId, String token, String objectId);

}