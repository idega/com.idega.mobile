package com.idega.mobile.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Data structure to hold response after user attempted to login
 *
 * @author valdas
 *
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LoginResult implements Serializable {

	private static final long serialVersionUID = -6075144888803306519L;

	private boolean success;

	private String sessionId, userId, orderRef, homePage, apiKey;

	public LoginResult() {
		super();
	}

	public LoginResult(boolean success) {
		this();

		this.success = success;
	}

	private LoginResult(boolean success, String sessionId) {
		this(success);

		this.sessionId = sessionId;
	}

	public LoginResult(boolean success, String sessionId, String userId, String apiKey) {
		this(success, sessionId);

		this.userId = userId;
		this.apiKey = apiKey;
	}

	public LoginResult(boolean success, String sessionId, String userId, String orderRef, String homePage, String apiKey) {
		this(success, sessionId, userId, apiKey);

		this.orderRef = orderRef;
		this.homePage = homePage;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getHomePage() {
		return homePage;
	}

	public void setHomePage(String homePage) {
		this.homePage = homePage;
	}

	public String getOrderRef() {
		return orderRef;
	}

	public void setOrderRef(String orderRef) {
		this.orderRef = orderRef;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

}