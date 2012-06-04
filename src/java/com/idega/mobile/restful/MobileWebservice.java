package com.idega.mobile.restful;

import javax.ws.rs.core.Response;

import com.idega.mobile.bean.LoginResult;

public interface MobileWebservice {

	/**
	 * Logs in user. GET: /mobile/login
	 *
	 * @param username
	 * @param password
	 * @return Response to the request ({@link LoginResult}) about the attempt to login
	 */
	public Response doLogin(String username, String password);

	/**
	 * Logs out user. GET: /mobile/logout
	 *
	 * @param username
	 * @return
	 */
	public Response doLogout(String username);

	/**
	 *  Pings HTTP session. GET: /mobile/ping?JSESSIONID=...
	 *
	 * @return
	 */
	public Response doPing(String httpSessionId);

}