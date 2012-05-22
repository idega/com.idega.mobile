package com.idega.mobile.bean;

import java.io.Serializable;

/**
 * Data structure to hold response after user attempted to login
 *
 * @author valdas
 *
 */
public class LoginResult implements Serializable {

	private static final long serialVersionUID = -6075144888803306519L;

	private boolean success;

	private String sessionId, userId;

	public LoginResult() {
		super();
	}

	public LoginResult(boolean success) {
		this();

		this.success = success;
	}

	public LoginResult(boolean success, String sessionId) {
		this(success);

		this.sessionId = sessionId;
	}

	public LoginResult(boolean success, String sessionId, String userId) {
		this(success, sessionId);

		this.userId = userId;
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

}