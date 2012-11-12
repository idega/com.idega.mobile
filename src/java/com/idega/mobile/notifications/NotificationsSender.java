package com.idega.mobile.notifications;

import java.util.List;

import com.idega.mobile.bean.Notification;
import com.idega.mobile.data.NotificationSubscription;

public interface NotificationsSender {

	public boolean doSendNotification(Notification notification, List<NotificationSubscription> subscriptions);

	public String getSupportedDeviceType();

}