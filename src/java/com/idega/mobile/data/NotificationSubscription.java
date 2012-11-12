package com.idega.mobile.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

@Entity
@Table(name=NotificationSubscription.TABLE)
public class NotificationSubscription implements Serializable {

	private static final long serialVersionUID = 823309220497260027L;

	public static final String TABLE = "notification_subscription";

	public static final String userIdColumn = "user_id",
								tokenColumn = "token",
								localeColumn = "locale",
								subscribedOnColumn = "subscribed_on",
								deviceColumn = "device";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = userIdColumn)
	@Index(columnNames = {userIdColumn}, name = "userIdIndex")
	private Integer userId;

	@Column(name = tokenColumn)
	@Index(columnNames = {tokenColumn}, name = "tokenIndex")
	private String token;

	@Column(name = localeColumn)
	@Index(columnNames = {localeColumn}, name = "localeIndex")
	private String locale;

	@Column(name = subscribedOnColumn)
	@Index(columnNames = {subscribedOnColumn}, name = "subscribedOnIndex")
	private String subscribedOn;

	@Column(name = deviceColumn)
	@Index(columnNames = {deviceColumn}, name = "deviceIndex")
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