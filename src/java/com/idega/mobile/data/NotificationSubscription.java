package com.idega.mobile.data;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(
		name = NotificationSubscription.TABLE,
		indexes = {
				@Index(columnList = NotificationSubscription.userIdColumn, name = "userIdIndex"),
				@Index(columnList = NotificationSubscription.tokenColumn, name = "tokenIndex"),
				@Index(columnList = NotificationSubscription.localeColumn, name = "localeIndex"),
				@Index(columnList = NotificationSubscription.subscribedOnColumn, name = "subscribedOnIndex"),
				@Index(columnList = NotificationSubscription.deviceColumn, name = "deviceIndex")
		}
)
@Cacheable
public class NotificationSubscription implements Serializable {

	private static final long serialVersionUID = 823309220497260027L;

	public static final String TABLE = "notification_subscription",

								userIdColumn = "user_id",
								tokenColumn = "token",
								localeColumn = "locale",
								subscribedOnColumn = "subscribed_on",
								deviceColumn = "device";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = userIdColumn)
	private Integer userId;

	@Column(name = tokenColumn)
	private String token;

	@Column(name = localeColumn)
	private String locale;

	@Column(name = subscribedOnColumn)
	private String subscribedOn;

	@Column(name = deviceColumn)
	private String device;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getSubscribedOn() {
		return subscribedOn;
	}

	public void setSubscribedOn(String subscribedOn) {
		this.subscribedOn = subscribedOn;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	@Override
	public String toString() {
		return "ID: " + getId() + ", user ID: " + getUserId() + ", token: " + getToken() + ", subscribed on: " + getSubscribedOn() + ", device: " +
				getDevice() + ", locale: " + getLocale();
	}
}