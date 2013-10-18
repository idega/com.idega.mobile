package com.idega.mobile.restful;

import javax.ws.rs.core.Response;

import com.idega.mobile.bean.LoginResult;
import com.idega.mobile.bean.Subscription;

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
	 * Logs in user via bank
	 *
	 * @param personalId
	 * @param country - ISO 3166-1 code
	 * @return Response to the request ({@link LoginResult}) about the attempt to login
	 */
	public Response doBankLogin(String personalId, String country);

	/**
	 * Checks if user has logged in via BankID
	 *
	 * @param personalId
	 * @param country - ISO 3166-1 code
	 * @param orderRef
	 * @return
	 */
	public Response isLoggedInViaBankId(String personalId, String country, String orderRef);

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

	/**
	 * Subscribes user to the push notifications. POST: /mobile/subscribe
	 *
	 * @param userId
	 * @param subscription
	 * @return
	 */
	public Response doSubscribe(String userId, Subscription subscription);

	/**
	 * Verifies whether user is subscribed for push notifications. GET: /mobile/subscribe
	 *
	 * @param userId
	 * @param token
	 * @param notifyOn
	 * @return
	 */
	public Response isSubscribed(String userId, String token, String notifyOn);
}