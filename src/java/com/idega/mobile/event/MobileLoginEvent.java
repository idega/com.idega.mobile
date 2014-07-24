package com.idega.mobile.event;

import org.springframework.context.ApplicationEvent;

import com.idega.mobile.bean.LoginResult;

public class MobileLoginEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1369785248493378945L;

	private LoginResult result;

	public MobileLoginEvent(LoginResult result) {
		super(result);

		this.result = result;
	}

	public LoginResult getResult() {
		return result;
	}

	public void setResult(LoginResult result) {
		this.result = result;
	}

}