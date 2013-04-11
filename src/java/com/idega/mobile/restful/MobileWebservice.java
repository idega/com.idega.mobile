package com.idega.mobile.restful;

import javax.ws.rs.core.Response;

import com.idega.mobile.bean.LoginResult;

public interface MobileWebservice {

	/**
	 * Logs in user. GET: /mobile/login
	 *
	 * @param username
	 * @param password
	 * @param type - optional (bank ID, Facebook etc.)
	 * @return Response to the request ({@link LoginResult}) about the attempt to login
	 */
	public Response doLogin(String username, String password, String type);

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

	/**
	 * Sends push notification. GET: /mobile/notification?token=...
	 *
	 * @param token
	 * @param message
	 * @param locale
	 * @param objectId
	 * @return
	 */
	public Response doSendNotification(String token, String message, String locale, String objectId);
}