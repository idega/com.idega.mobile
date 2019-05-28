package com.idega.mobile.restful;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import com.idega.mobile.bean.LoginResult;
import com.idega.mobile.bean.PayloadData;
import com.idega.mobile.bean.Subscription;
import com.idega.user.data.User;

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
	 * Sends push notification. POST: /mobile/notification
	 *
	 * @param data
	 * @return
	 */
	public Response doSendNotification(PayloadData data);

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

	/**
	 *
	 * <p>Webservice to get homepage of the {@link User}</p>
	 * @return /pages/... or <code>null</code> on failure;
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 */
	public Response getUserHomePage(HttpServletRequest request, HttpServletResponse response, ServletContext context);

}