package com.idega.mobile.bean;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PayloadData implements Serializable {

	private static final long serialVersionUID = 1533378539003950620L;

	private String token, locale, message, notifyOn;

	private List<PayloadItem> data;

	public PayloadData() {
		super();
	}

	public PayloadData(String token, String locale, String message, String notifyOn) {
		this();

		this.token = token;
		this.locale = locale;
		this.message = message;
		this.notifyOn = notifyOn;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getNotifyOn() {
		return notifyOn;
	}

	public void setNotifyOn(String notifyOn) {
		this.notifyOn = notifyOn;
	}

	public List<PayloadItem> getData() {
		return data;
	}

	public void setData(List<PayloadItem> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Payload token: " + getToken() + ", locale: " + getLocale() + ", message: " + getMessage() + ", notify on: " + getNotifyOn() + ", data: " +
				getData();
	}

}