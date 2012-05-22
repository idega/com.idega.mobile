package com.idega.mobile.restful;

import javax.ws.rs.core.Response;

public interface MobileWebservice {

	/**
	 * Logs in user. GET: /mobile/login
	 *
	 * @param username
	 * @param password
	 * @return HTTP session ID on successful login or failure message
	 */
	public Response doLogin(String username, String password);

	/**
	 * Logs out user. GET: /mobile/logout
	 *
	 * @param username
	 * @return
	 */
	public Response doLogout(String username);

}