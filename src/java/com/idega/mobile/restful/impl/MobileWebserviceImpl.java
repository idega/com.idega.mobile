package com.idega.mobile.restful.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.idega.block.login.bean.BankLoginInfo;
import com.idega.block.login.business.BankIDLogin;
import com.idega.block.login.business.OAuth2Service;
import com.idega.builder.business.BuilderLogic;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.business.LoginDBHandler;
import com.idega.core.accesscontrol.dao.UserLoginDAO;
import com.idega.core.accesscontrol.data.bean.UserLogin;
import com.idega.core.builder.data.ICPage;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.data.IDOLookup;
import com.idega.event.IWHttpSessionsManager;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.mobile.MobileConstants;
import com.idega.mobile.bean.LoginResult;
import com.idega.mobile.bean.Notification;
import com.idega.mobile.bean.PayloadData;
import com.idega.mobile.bean.Subscription;
import com.idega.mobile.data.MobileDAO;
import com.idega.mobile.data.NotificationSubscription;
import com.idega.mobile.event.MobileLoginEvent;
import com.idega.mobile.notifications.NotificationsCenter;
import com.idega.mobile.restful.MobileWebservice;
import com.idega.presentation.IWContext;
import com.idega.restful.business.DefaultRestfulService;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.LocaleUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

@Component
@Path(MobileConstants.URI)
public class MobileWebserviceImpl extends DefaultRestfulService implements MobileWebservice {

	@Autowired
	private IWHttpSessionsManager httpSessionsManager;

	@Autowired
	private MobileDAO mobileDAO;

	@Autowired
	private NotificationsCenter notificationsCenter;

    @Override
	@GET
    @Path(MobileConstants.URI_LOGIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLogin(
    		@QueryParam("username") String username,
    		@QueryParam("password") String password,
    		@QueryParam("type") String type
    ) {
        String message = null;
    	if (StringUtil.isEmpty(username) || StringUtil.isEmpty(password)) {
    		message = "User name (" + username + ") or password (" + password + ") is not provided";
    		getLogger().warning(message);
        	return getResponse(Response.Status.UNAUTHORIZED, new LoginResult(Boolean.FALSE));
    	}

    	try {
	    	IWContext iwc = CoreUtil.getIWContext();
	    	HttpServletRequest request = iwc.getRequest();
	    	HttpSession session = request.getSession();

	    	String userId = getUserIdByLogin(username);
	    	if (StringUtil.isEmpty(userId)) {
	    		getLogger().warning("User can not be found by username " + username);
	    		return getResponse(Response.Status.UNAUTHORIZED, new LoginResult(Boolean.FALSE));
	    	}

	    	UserLoginDAO userLoginDAO = ELUtil.getInstance().getBean(UserLoginDAO.class);
	    	UserLogin userLogin = userLoginDAO.findLoginByUsername(username);
	    	if (userLogin == null) {
	    		getLogger().warning("Login not found by username " + username);
	    		return getResponse(Response.Status.UNAUTHORIZED, new LoginResult(Boolean.FALSE));
	    	}

	    	LoginBusinessBean loginBusinessBean = LoginBusinessBean.getLoginBusinessBean(request);
	    	if (!loginBusinessBean.verifyPassword(userLogin, password)) {
	    		getLogger().warning("Wrong password for username " + username);
	    		return getResponse(Response.Status.UNAUTHORIZED, new LoginResult(Boolean.FALSE));
	    	}

	    	String sessionId = session.getId();
	    	if (loginBusinessBean.isLoggedOn(request)) {
	    		message = "User " + username + " is already logged in";
	    		getLogger().info(message);
	    		LoginResult result = new LoginResult(Boolean.TRUE, sessionId, userId, null, getUserHomePage(userId), getApiKey(userId));
	    		try {
	    			return getResponse(Response.Status.ACCEPTED, result);
	    		} finally {
	    			ELUtil.getInstance().publishEvent(new MobileLoginEvent(result));
	    		}
	    	}

	    	boolean success = loginBusinessBean.logInUser(request, username, password);
	    	String homePage = null;
	    	if (success) {
	    		homePage = getUserHomePage(userId);
	    	}
	    	LoginResult result = new LoginResult(
					success,
					success ? sessionId : null,
					success ? userId : null,
					null,
					homePage,
					success ? getApiKey(userId) : null
			);
	    	try {
		    	return getResponse(success ? Response.Status.ACCEPTED : Response.Status.UNAUTHORIZED, result);
	    	} finally {
	    		if (success) {
	    			ELUtil.getInstance().publishEvent(new MobileLoginEvent(result));
	    		}
	    	}
    	} catch (Exception e) {
    		message = "Error while trying to login user " + username;
    		getLogger().log(Level.WARNING, message, e);
    		return getResponse(Response.Status.UNAUTHORIZED, new LoginResult(Boolean.FALSE));
    	}
    }

    private String getUserHomePage(String userId) {
    	if (StringUtil.isEmpty(userId)) {
    		return null;
    	}

    	try {
	    	UserHome userHome = (UserHome) IDOLookup.getHome(User.class);
	    	User user = userHome.findByPrimaryKeyIDO(Integer.valueOf(userId));

	    	BuilderLogic builderLogic = BuilderLogic.getInstance();
	    	ICPage homePage = builderLogic.getUsersHomePage(user);
	    	return homePage == null ? null : CoreConstants.PAGES_URI_PREFIX + homePage.getDefaultPageURI();
    	} catch (Exception e) {
    		getLogger().log(Level.WARNING, "Error getting home page for user " + userId, e);
    	}

    	return null;
    }

    private String getUserIdByLogin(String username) {
    	if (StringUtil.isEmpty(username)) {
			return null;
		}

    	Integer id = null;
    	try {
    		id = LoginDBHandler.getUserLoginByUserName(username).getUserId();
    	} catch (Exception e) {
    		getLogger().warning("User ID can not be found for provided user name: " + username);
    	}

    	return id == null ? null : String.valueOf(id);
    }

    @Autowired(required = false)
    private OAuth2Service oauth2;

    private OAuth2Service getOAuth2Service() {
    	if (this.oauth2 == null) {
    		ELUtil.getInstance().autowire(this);
    	}

    	return this.oauth2;
    }

    @Override
	@GET
	@Path(MobileConstants.URI_GET_USER_HOME_PAGE)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUserHomePage(
			@Context HttpServletRequest request,
			@Context HttpServletResponse response,
			@Context ServletContext context
	) {
    	try {
    		IWContext iwc = new IWContext(request, response, context);
    		OAuth2Service oauthService = getOAuth2Service();
    		if (oauthService == null) {
    			return getInternalServerErrorResponse("Error resolving current user");
    		}

			com.idega.user.data.bean.User user = oauthService.getAuthenticatedUser(iwc);
			if (user != null) {
				String homepage = getUserHomePage(user.getId().toString());
				return getOKResponse(homepage);
			}
    	} catch (Exception e) {
    		getLogger().log(Level.WARNING, "Error getting homepage for logged in user", e);
    	}

		return null;
	}

    @Override
	@GET
    @Path(MobileConstants.URI_LOGOUT)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLogout(@QueryParam("username") String username) {
    	String message = null;
     	if (StringUtil.isEmpty(username)) {
     		message = "User name is not provided";
     		getLogger().warning(message);
         	return getResponse(Response.Status.BAD_REQUEST, message);
     	}

     	try {
     		IWContext iwc = CoreUtil.getIWContext();
     		LoginBusinessBean login = LoginBusinessBean.getLoginBusinessBean(iwc.getRequest());
     		boolean success = login.logOutUser(iwc);
     		message = success ? "Success" : "Failed";
     		return getResponse(success ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR, message);
     	} catch (Exception e) {
     		message = "Error while logging out " + username;
     		getLogger().log(Level.WARNING, message, e);
     		return getResponse(Response.Status.INTERNAL_SERVER_ERROR, message);
     	}
    }

    @GET
	@Path(MobileConstants.URI_GET_REPOSITORY_ITEM)
	@Produces("*/*")
	public Response getFile(@QueryParam(MobileConstants.PARAM_URL) String url) {
		String errorMessage = null;
		if (StringUtil.isEmpty(url)) {
			errorMessage = "URL is not provided";
			getLogger().warning(errorMessage);
			return getResponse(Response.Status.BAD_REQUEST, errorMessage);
		}

		InputStream stream = null;
		try {
			stream = getStream(url);
		} catch (Exception e) {
			errorMessage = "Error getting attachment at " + url;
			getLogger().log(Level.WARNING, errorMessage, e);
			return getResponse(Response.Status.INTERNAL_SERVER_ERROR, errorMessage);
		}
		if (stream == null) {
			errorMessage = "Attachment at " + url + " is not defined or does not exist";
			getLogger().warning(errorMessage);
			return getResponse(Response.Status.INTERNAL_SERVER_ERROR, errorMessage);
		}

		String name = url.substring(url.lastIndexOf(File.separator) + 1);
		String mimeType = MimeTypeUtil.resolveMimeTypeFromFileName(name);
		return Response.ok(stream, mimeType).build();
	}

	private InputStream getStream(String path) throws Exception {
		if (StringUtil.isEmpty(path)) {
			return null;
		}

		if (path.startsWith(CoreConstants.WEBDAV_SERVLET_URI) || path.startsWith(CoreConstants.PATH_FILES_ROOT)) {
			try {
				if (getRepositoryService().getExistence(path)) {
					return getRepositoryService().getInputStreamAsRoot(path);
				}
			} catch (Exception e) {
				getLogger().log(Level.WARNING, "Error getting stream to " + path, e);
			}
		}

		File tmp = new File(path);
		if (!tmp.exists() || !tmp.canRead()) {
			if (path.startsWith(File.separator)) {
				path = path.substring(1);
				tmp = new File(path);
				if (!tmp.exists() || !tmp.canRead()) {
					return null;
				}
			} else {
				return null;
			}
		}

		return new FileInputStream(tmp);
	}

	private IWHttpSessionsManager getSessionsManager() {
		if (httpSessionsManager == null) {
			ELUtil.getInstance().autowire(this);
		}
		return httpSessionsManager;
	}

	@Override
	@GET
	@Path(MobileConstants.URI_PING)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doPing(@QueryParam("JSESSIONID") String httpSessionId) {
		String message = null;
		if (StringUtil.isEmpty(httpSessionId)) {
			message = "HTTP session ID is not provided. It should be provided by parameter JSESSIONID";
			getLogger().warning(message);
			return getResponse(Response.Status.BAD_REQUEST, message);
		}

		try {
			HttpSession session = CoreUtil.getIWContext().getRequest().getSession(Boolean.FALSE);
			String sessionIdFromRequest = session == null ? CoreConstants.EMPTY : session.getId();
			if (getSessionsManager().isSessionValid(httpSessionId) && httpSessionId.equals(sessionIdFromRequest)) {
				return getResponse(Response.Status.OK, "Session is valid");
			}

			message = "Session by ID " + httpSessionId + " is not valid: probably it has expired or is not the same as expected. " +
					"Expected session ID: " + sessionIdFromRequest + ", got: " + httpSessionId;
			getLogger().warning(message);
			return getResponse(Response.Status.NOT_FOUND, message);
		} catch (Exception e) {
			message = "Error pinging session by ID: " + httpSessionId;
			getLogger().log(Level.WARNING, message, e);
		}
		return getResponse(Response.Status.INTERNAL_SERVER_ERROR, message);
	}

	private MobileDAO getMobileDAO() {
		if (mobileDAO == null) {
			ELUtil.getInstance().autowire(this);
		}
		return mobileDAO;
	}

	private NotificationsCenter getNotificationsCenter() {
		if (notificationsCenter == null) {
			ELUtil.getInstance().autowire(this);
		}
		return notificationsCenter;
	}

	@Override
	@POST
	@Path(MobileConstants.URI_NOTIFICATION)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doSendNotification(PayloadData data) {
		String msg = null;
		if (data == null) {
			msg = "Data is not provided";
			getLogger().warning(msg);
			return getResponse(Response.Status.BAD_REQUEST, msg);
		}
		if (StringUtil.isEmpty(data.getToken())) {
			msg = "Token is not provided";
			getLogger().warning(msg);
			return getResponse(Response.Status.BAD_REQUEST, msg);
		}
		if (StringUtil.isEmpty(data.getMessage())) {
			msg = "Message is not provided";
			getLogger().warning(msg);
			return getResponse(Response.Status.BAD_REQUEST, msg);
		}
		if (StringUtil.isEmpty(data.getLocale())) {
			getLogger().warning("Locale is not provided, using " + Locale.ENGLISH);
			data.setLocale(Locale.ENGLISH.toString());
		}

		IWMainApplicationSettings settings = getApplication().getSettings();
		if (StringUtil.isEmpty(data.getNotifyOn())) {
			String notifyOn = settings.getProperty("default_notification_object", MobileConstants.NOTIFY_ON_ALL);
			if (StringUtil.isEmpty(notifyOn)) {
				msg = "Notify on is not provided";
				getLogger().warning(msg);
				return getResponse(Response.Status.BAD_REQUEST, msg);
			} else {
				data.setNotifyOn(notifyOn);
			}
		}

		List<NotificationSubscription> subscriptions = getMobileDAO().getSubscriptions(Arrays.asList(data.getToken()), data.getNotifyOn());
		if (ListUtil.isEmpty(subscriptions)) {
			msg = "There are no subscriptions by token " + data.getToken();
			getLogger().warning(msg);
			return getResponse(Response.Status.BAD_REQUEST, msg);
		}
		if (settings.getBoolean("push_notif_use_latest_subscr", Boolean.TRUE)) {
			subscriptions = Arrays.asList(subscriptions.get(subscriptions.size() - 1));
			getLogger().info("Using only the latest subscription: " + subscriptions);
		}

		Map<Locale, String> messages = new HashMap<Locale, String>();
		messages.put(ICLocaleBusiness.getLocaleFromLocaleString(data.getLocale()), data.getMessage());
		Notification notification = new Notification(messages, subscriptions);
		notification.setNotifyOn(data.getNotifyOn());
		if (getNotificationsCenter().doSendNotification(notification)) {
			return getResponse(Response.Status.OK, data.getToken());
		}

		return getResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error sending notification (" + data.getMessage() + ") to token " + data.getToken());
	}

	@Override
	@Path(MobileConstants.URI_SUBSCRIBE)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doSubscribe(
			@HeaderParam(MobileConstants.PARAM_USER_ID) String userId,
			Subscription subscription
	) {
		String message = null;
		if (StringUtil.isEmpty(userId)) {
			message = "User ID is not provided";
			getLogger().warning(message);
			return getResponse(Response.Status.BAD_REQUEST, message);
		}
		if (subscription == null) {
			message = "Subscription data is not provided";
			getLogger().warning(message);
			return getResponse(Response.Status.BAD_REQUEST, message);
		}
		if (StringUtil.isEmpty(subscription.getToken())) {
			message = "Device's token is not provided";
			getLogger().warning(message);
			return getResponse(Response.Status.BAD_REQUEST, message);
		}

		Boolean subscribing = null;
		try {
			String notifyOn = subscription.getNotifyOn();
			if (StringUtil.isEmpty(notifyOn)) {
				notifyOn = MobileConstants.NOTIFY_ON_ALL;
			}

			User user = getUser(userId);
			if (user == null) {
				return getResponse(Response.Status.BAD_REQUEST, "User can not be found by ID: " + userId);
			}
			Integer usrId = Integer.valueOf(user.getId());

			boolean success = Boolean.FALSE;
			subscribing = subscription.isSubscribe();
			if (subscribing != null && subscribing) {
				Locale locale = LocaleUtil.getLocale(subscription.getLocaleId());
				if (locale == null) {
					locale = Locale.ENGLISH;
				}
				success = getNotificationsCenter().doSubscribe(usrId, subscription.getToken(), locale, notifyOn, subscription.getDevice());
				message = success ? "User " + user + " successfully subscribed to notifications for " + notifyOn :
									"Error while subscribing user " + user + " to notifications for " + notifyOn;
			} else {
				success = getNotificationsCenter().doUnSubscribe(usrId, subscription.getToken(), notifyOn);
				message = success ? "User " + user + " successfully unsubscribed from notifications for all issues" :
									"Error while unsubscribing user " + user + " from notifications for all issues";
			}

			subscription.setSuccess(success);
			return getResponse(success ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR, subscription);
		} catch (Exception e) {
			message = "Error while " + (subscribing != null && subscribing ? "subscribing" : "unsubscribing") + " with data: user ID: " + userId +
					", susbcription data: " + subscription;
			getLogger().log(Level.WARNING, message, e);
			CoreUtil.sendExceptionNotification(message, e);
		}

		return getResponse(Response.Status.INTERNAL_SERVER_ERROR, message);
	}

	@Override
	@Path(MobileConstants.URI_SUBSCRIBE)
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response isSubscribed(
			@HeaderParam(MobileConstants.PARAM_USER_ID) String userId,

			@QueryParam(MobileConstants.PARAM_TOKEN) String token,
			@QueryParam(MobileConstants.PARAM_NOTIFY_ON) String notifyOn
	) {
		if (StringUtil.isEmpty(notifyOn)) {
			notifyOn = MobileConstants.NOTIFY_ON_ALL;
		}

		String message = "checking whether user is subscribed. User ID: " + userId + ", token: " + token + ", notify on: " + notifyOn;
		if (StringUtil.isEmpty(userId)) {
			message = "Error while " + message;
			getLogger().warning(message);
			return getResponse(Response.Status.BAD_REQUEST, message);
		}
		if (StringUtil.isEmpty(token)) {
			message = "Error while " + message;
			getLogger().warning(message);
			return getResponse(Response.Status.BAD_REQUEST, message);
		}

		try {
			User user = getUser(userId);
			if (user == null) {
				return getResponse(Response.Status.BAD_REQUEST, "User can not be found by ID: " + userId);
			}
			Integer usrId = Integer.valueOf(user.getId());

			Subscription result = new Subscription(getNotificationsCenter().isSubscribed(usrId, token, notifyOn));
			result.setToken(token);
			return getResponse(Response.Status.OK, result);
		} catch (Exception e) {
			message = "Error while " + message;
			getLogger().log(Level.WARNING, message, e);
			CoreUtil.sendExceptionNotification(message, e);
		}
		return getResponse(Response.Status.INTERNAL_SERVER_ERROR, message);
	}

	protected String getApiKey(String userId) {
		return getApiKey(getUser(userId));
	}

	protected String getApiKey(User user) {
		try {
			return new String(Base64.encodeBase64(user.getUniqueId().getBytes(CoreConstants.ENCODING_UTF8)), CoreConstants.ENCODING_UTF8);
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error encoding user's unique ID: " + user, e);
		}
		return null;
	}

	@Override
	@GET
	@Path(MobileConstants.URI_BANK_ID_LOGIN)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doBankLogin(
			@QueryParam(MobileConstants.PARAM_PERSONAL_ID) String personalId,
			@QueryParam(MobileConstants.PARAM_COUNTRY) String country
	) {
    	if (StringUtil.isEmpty(personalId) || StringUtil.isEmpty(country)) {
    		getLogger().warning("Personal ID or country are not provided");
        	return getResponse(Response.Status.UNAUTHORIZED, new LoginResult(Boolean.FALSE));
    	}

    	try {
    		country = country.toUpperCase();
    		BankIDLogin bankIdLoginByCountry = ELUtil.getInstance().getBean(BankIDLogin.BEAN_NAME_PREFIX + country);
    		if (bankIdLoginByCountry == null) {
        		getLogger().warning("There is no implementation of BankID login for country by code: " + country + ". Personal ID: " + personalId);
        		return getResponse(Response.Status.UNAUTHORIZED, new LoginResult(Boolean.FALSE));
    		}

    		User user = getUser(personalId, true);
    		if (user == null) {
        		getLogger().warning("Unable to create get/create user by personal ID: " + personalId);
        		return getResponse(Response.Status.UNAUTHORIZED, new LoginResult(Boolean.FALSE));
    		}

    		BankLoginInfo info = bankIdLoginByCountry.doLogin(personalId);
    		if (info != null && info.isSuccess()) {
    			IWContext iwc = CoreUtil.getIWContext();
    			HttpServletRequest request = iwc.getRequest();
    	    	HttpSession session = request.getSession();

    			LoginBusinessBean login = LoginBusinessBean.getLoginBusinessBean(request);
    	    	String userId = user.getId();
    			String sessionId = session.getId();
    	    	if (login.isLoggedOn(request)) {
    	    		getLogger().info("User by personal ID " + personalId + " is already logged in");
    	    		LoginResult result = new LoginResult(Boolean.TRUE, sessionId, userId, getApiKey(user));
    	    		try {
    	    			return getResponse(Response.Status.ACCEPTED, result);
    	    		} finally {
    	    			ELUtil.getInstance().publishEvent(new MobileLoginEvent(result));
    	    		}
    	    	}

    	    	boolean success = login.logInByPersonalID(request, personalId);
    	    	String homePage = success ? getUserHomePage(userId) : null;
    	    	LoginResult result = new LoginResult(
    					success,
    					success ? sessionId : null,
    					success ? userId : null,
    					success ? info.getOrderRef() : null,
    					homePage,
    					success ? getApiKey(user) : null
    			);
    	    	try {
    	    		return getResponse(success ? Response.Status.ACCEPTED : Response.Status.UNAUTHORIZED, result);
	    		} finally {
	    			if (success) {
	    				ELUtil.getInstance().publishEvent(new MobileLoginEvent(result));
	    			}
	    		}
    		}

    		getLogger().warning("Unable to login via BankID with personal ID: " + personalId + " and country: " + country);
    		return getResponse(Response.Status.UNAUTHORIZED, new LoginResult(Boolean.FALSE, null, null, info == null ? null : info.getOrderRef(), null, null));
    	} catch (Exception e) {
    		getLogger().log(Level.WARNING, "Error while trying to login via bank ID. Personal ID: " + personalId + ", country: " + country, e);
    		return getResponse(Response.Status.UNAUTHORIZED, new LoginResult(Boolean.FALSE));
    	}
	}

	@Override
	@GET
	@Path(MobileConstants.URI_BANK_ID_LOGIN + "/{orderRef}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response isLoggedInViaBankId(
			@QueryParam(MobileConstants.PARAM_PERSONAL_ID) String personalId,
			@QueryParam(MobileConstants.PARAM_COUNTRY) String country,
			@PathParam("orderRef") String orderRef
	) {
		if (StringUtil.isEmpty(personalId) || StringUtil.isEmpty(country) || StringUtil.isEmpty(orderRef)) {
    		getLogger().warning("Personal ID, country or order ref. are not provided");
        	return getResponse(Response.Status.UNAUTHORIZED, Boolean.FALSE);
    	}

		try {
    		country = country.toUpperCase();
    		BankIDLogin bankIdLoginByCountry = ELUtil.getInstance().getBean(BankIDLogin.BEAN_NAME_PREFIX + country);
    		if (bankIdLoginByCountry == null) {
        		getLogger().warning("There is no implementation of BankID login for country by code: " + country + ". Personal ID: " + personalId);
        		return getResponse(Response.Status.UNAUTHORIZED, new LoginResult(Boolean.FALSE));
    		}

    		Boolean loggedIn = bankIdLoginByCountry.isLoggedIn(personalId, orderRef);
    		return getResponse(
    				loggedIn ? Response.Status.ACCEPTED : Response.Status.UNAUTHORIZED,
	    			loggedIn ? Boolean.TRUE : Boolean.FALSE
	    	);
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error while verifying whether person by ID " + personalId + " has logged in via BankID (order ref.: " +
					orderRef +"). Country: " + country, e);
		}
		return getResponse(Response.Status.UNAUTHORIZED, Boolean.FALSE);
	}

}