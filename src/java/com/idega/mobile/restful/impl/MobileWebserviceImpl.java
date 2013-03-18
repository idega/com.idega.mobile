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

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.accesscontrol.business.LoginDBHandler;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.event.IWHttpSessionsManager;
import com.idega.mobile.MobileConstants;
import com.idega.mobile.bean.LoginResult;
import com.idega.mobile.bean.Notification;
import com.idega.mobile.data.MobileDAO;
import com.idega.mobile.data.NotificationSubscription;
import com.idega.mobile.notifications.NotificationsCenter;
import com.idega.mobile.restful.MobileWebservice;
import com.idega.presentation.IWContext;
import com.idega.restful.business.DefaultRestfulService;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

/**
 * Description
 * User: Simon Sönnby
 * Date: 2012-03-14
 * Time: 09:33
 */

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
    		@QueryParam("password") String password
    ) {
        String message = null;
    	if (StringUtil.isEmpty(username) || StringUtil.isEmpty(password)) {
    		message = "User name or password is not provided";
    		getLogger().warning(message);
        	return getResponse(Response.Status.UNAUTHORIZED, new LoginResult(false));
    	}

    	try {
	    	IWContext iwc = CoreUtil.getIWContext();
	    	HttpServletRequest request = iwc.getRequest();
	    	HttpSession session = request.getSession();

	    	String userId = getUserIdByLogin(username);
	    	if (StringUtil.isEmpty(userId))
	    		return getResponse(Response.Status.UNAUTHORIZED, new LoginResult(Boolean.FALSE));

	    	LoginBusinessBean login = LoginBusinessBean.getLoginBusinessBean(request);
	    	String sessionId = session.getId();
	    	if (login.isLoggedOn(request)) {
	    		message = "User " + username + " is already logged in";
	    		getLogger().info(message);
	    		return getResponse(Response.Status.ACCEPTED, new LoginResult(Boolean.TRUE, sessionId, userId));
	    	}

	    	boolean success = login.logInUser(request, username, password);
	    	return getResponse(success ? Response.Status.ACCEPTED : Response.Status.UNAUTHORIZED, new LoginResult(success, success ? sessionId : null,
	    			success ? userId : null));
    	} catch (Exception e) {
    		message = "Error while trying to login user " + username;
    		getLogger().log(Level.WARNING, message, e);
    		return getResponse(Response.Status.UNAUTHORIZED, new LoginResult(Boolean.FALSE));
    	}
    }

    private String getUserIdByLogin(String username) {
    	if (StringUtil.isEmpty(username))
    		return null;

    	Integer id = null;
    	try {
    		id = LoginDBHandler.getUserLoginByUserName(username).getUserId();
    	} catch (Exception e) {
    		getLogger().warning("User ID can not be found for provided user name: " + username);
    	}

    	return id == null ? null : String.valueOf(id);
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
		if (StringUtil.isEmpty(path))
			return null;

		if (path.startsWith(CoreConstants.WEBDAV_SERVLET_URI) || path.startsWith(CoreConstants.PATH_FILES_ROOT)) {
			try {
			if (getRepositoryService().getExistence(path))
				return getRepositoryService().getInputStreamAsRoot(path);
			} catch (RepositoryException e) {
				getLogger().log(Level.WARNING, "Error getting stream to " + path, e);
			}
		}

		File tmp = new File(path);
		if (!tmp.exists() || !tmp.canRead())
			return null;

		return new FileInputStream(tmp);
	}

	private IWHttpSessionsManager getSessionsManager() {
		if (httpSessionsManager == null)
			ELUtil.getInstance().autowire(this);
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
			HttpSession session = CoreUtil.getIWContext().getRequest().getSession(false);
			String sessionIdFromRequest = session == null ? CoreConstants.EMPTY : session.getId();
			if (getSessionsManager().isSessionValid(httpSessionId) && httpSessionId.equals(sessionIdFromRequest))
				return getResponse(Response.Status.OK, "Session is valid");

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
		if (mobileDAO == null)
			ELUtil.getInstance().autowire(this);
		return mobileDAO;
	}

	private NotificationsCenter getNotificationsCenter() {
		if (notificationsCenter == null)
			ELUtil.getInstance().autowire(this);
		return notificationsCenter;
	}

	@Override
	@GET
	@Path(MobileConstants.URI_NOTIFICATION)
	@Produces("*/*")
	public Response doSendNotification(
			@QueryParam(MobileConstants.PARAM_TOKEN) String token,
			@QueryParam(MobileConstants.PARAM_MSG) String message,
			@QueryParam(MobileConstants.PARAM_LOCALE) String locale,
			@QueryParam(MobileConstants.PARAM_OBJECT_ID) String objectId
	) {
		String msg = null;
		if (StringUtil.isEmpty(token)) {
			msg = "Token is not provided";
			getLogger().warning(msg);
			return getResponse(Response.Status.BAD_REQUEST, msg);
		}
		if (StringUtil.isEmpty(message)) {
			msg = "Message is not provided";
			getLogger().warning(msg);
			return getResponse(Response.Status.BAD_REQUEST, msg);
		}
		if (StringUtil.isEmpty(locale)) {
			getLogger().warning("Locale is not provided, using " + Locale.ENGLISH);
			locale = Locale.ENGLISH.toString();
		}
		if (StringUtil.isEmpty(objectId)) {
			objectId = getApplication().getSettings().getProperty("default_notification_object");
			if (StringUtil.isEmpty(objectId)) {
				msg = "Object ID is not provided";
				getLogger().warning(msg);
				return getResponse(Response.Status.BAD_REQUEST, msg);
			}
		}

		List<NotificationSubscription> subscriptions = getMobileDAO().getSubscriptions(Arrays.asList(token), objectId);
		if (ListUtil.isEmpty(subscriptions)) {
			msg = "There are no subscriptions by token " + token;
			getLogger().warning(msg);
			return getResponse(Response.Status.BAD_REQUEST, msg);
		}

		Map<Locale, String> messages = new HashMap<Locale, String>();
		messages.put(ICLocaleBusiness.getLocaleFromLocaleString(locale), message);
		Notification notification = new Notification(messages, subscriptions);
		if (getNotificationsCenter().doSendNotification(notification))
			return getResponse(Response.Status.OK, token);

		return getResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error sending notification (" + message + ") to token " + token);
	}

}
