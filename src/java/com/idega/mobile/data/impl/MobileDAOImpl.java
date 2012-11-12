package com.idega.mobile.data.impl;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.idega.core.persistence.Param;
import com.idega.core.persistence.impl.GenericDaoImpl;
import com.idega.mobile.data.MobileDAO;
import com.idega.mobile.data.NotificationSubscription;

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class MobileDAOImpl extends GenericDaoImpl implements MobileDAO {

	@Override
	public List<NotificationSubscription> getSubscriptionsForObject(String objectId) {
		return getResultListByInlineQuery("select s from " + NotificationSubscription.class.getName() + " s where s.subscribedOn = :notifyOn",
				NotificationSubscription.class,
				new Param("notifyOn", objectId));
	}

	@Override
	public List<NotificationSubscription> getSubscriptionsForObjectExcludingUsers(String objectId, List<Integer> userIds) {
		return getResultListByInlineQuery("select s from " + NotificationSubscription.class.getName() +
				" s where s.subscribedOn = :notifyOn and s.userId not in (:userIds)",
				NotificationSubscription.class,
				new Param("notifyOn", objectId),
				new Param("userIds", userIds)
		);
	}

	@Override
	public NotificationSubscription getNotificationSubscription(Integer userId, String token, String objectId) {
		return getSingleResultByInlineQuery("select s from " + NotificationSubscription.class.getName() +
				" s where s.userId = :userId and s.token = :tokenId and s.subscribedOn = :notifyOn",
				NotificationSubscription.class,
				new Param("userId", userId),
				new Param("tokenId", token),
				new Param("notifyOn", objectId)
		);
	}

	@Override
	@Transactional(readOnly = false)
	public NotificationSubscription doCreateSubscription(Integer userId, String token, Locale locale, String objectId, String device) {
		NotificationSubscription subscription = getNotificationSubscription(userId, token, objectId);
		if (subscription == null)
			subscription = new NotificationSubscription();

		subscription.setUserId(userId);
		subscription.setToken(token);
		if (locale != null)
			subscription.setLocale(locale.toString());
		subscription.setSubscribedOn(objectId);
		subscription.setDevice(device);

		if (subscription.getId() == null)
			persist(subscription);
		else
			merge(subscription);

		return subscription.getId() == null ? null : subscription;
	}

	@Override
	@Transactional(readOnly = false)
	public boolean doDeleteSubscription(Integer userId, String token, String objectId) {
		NotificationSubscription subscription = getNotificationSubscription(userId, token, objectId);
		if (subscription == null)
			return true;

		remove(subscription);

		return true;
	}

}