package com.idega.mobile.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Subscription implements Serializable {

	private static final long serialVersionUID = 8032562482163166470L;

	private Boolean subscribe = Boolean.TRUE, success;

	private String token, localeId, device, notifyOn;

	public Subscription() {
		super();
	}

	public Subscription(boolean subscribe) {
		this();

		this.subscribe = subscribe;
	}

	public Subscription(String token, String localeId, String device, String notifyOn) {
		this(Boolean.TRUE, token, localeId, device, notifyOn);
	}

	public Subscription(boolean subscribe, String token, String localeId, String device, String notifyOn) {
		this(subscribe);

		this.token = token;
		this.localeId = localeId;
		this.device = device;
		this.notifyOn = notifyOn;
	}

	public Boolean getSubscribe() {
		return subscribe;
	}

	public boolean isSubscribe() {
		return subscribe == null ? Boolean.FALSE : subscribe;
	}

	public void setSubscribe(Boolean subscribe) {
		this.subscribe = subscribe;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getLocaleId() {
		return localeId;
	}

	public void setLocaleId(String localeId) {
		this.localeId = localeId;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getNotifyOn() {
		return notifyOn;
	}

	public void setNotifyOn(String notifyOn) {
		this.notifyOn = notifyOn;
	}

	@Override
	public String toString() {
		return "Subscribe: " + isSubscribe() + ", token: " + getToken() + ", device: " + getDevice() + ", locale: " + getLocaleId() + " on: " +
				getNotifyOn();
	}
}
