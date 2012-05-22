package com.idega.mobile.restful.impl;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

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

import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.event.IWHttpSessionsManager;
import com.idega.mobile.MobileConstants;
import com.idega.mobile.restful.DefaultRestfulService;
import com.idega.mobile.restful.MobileWebservice;
import com.idega.presentation.IWContext;
import com.idega.slide.business.IWSlideService;
import com.idega.util.CoreConstants;
import com.idega.util.CoreUtil;
import com.idega.util.FileUtil;
import com.idega.util.IOUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

/**
 * Description
 * User: Simon Sönnby
 * Date: 2012-03-14
 * Time: 09:33
 */
@Path(MobileConstants.URI)
public class MobileWebserviceImpl extends DefaultRestfulService implements MobileWebservice {

	@Autowired
	private IWHttpSessionsManager httpSessionsManager;

    @GET
    @Path(MobileConstants.URI_LOGIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLogin(@QueryParam("username") String username, @QueryParam("password") String password) {
        String message = null;
    	if (StringUtil.isEmpty(username) || StringUtil.isEmpty(password)) {
    		message = "User name or password is not provided";
    		getLogger().warning(message);
        	return getResponse(Response.Status.UNAUTHORIZED, message);
    	}

    	try {
	    	IWContext iwc = CoreUtil.getIWContext();
	    	HttpServletRequest request = iwc.getRequest();
	    	HttpSession session = request.getSession();
	    	LoginBusinessBean login = LoginBusinessBean.getLoginBusinessBean(request);
	    	if (login.isLoggedOn(request)) {
	    		message = "User " + username + " is already logged in";
	    		getLogger().info(message);
	    		return getResponse(Response.Status.ACCEPTED, message);
	    	}

	    	boolean success = login.logInUser(request, username, password);
	    	message = success ? session.getId() : "Failed";
	    	return getResponse(success ? Response.Status.ACCEPTED : Response.Status.UNAUTHORIZED, message);
    	} catch (Exception e) {
    		message = "Error while trying to login user " + username;
    		getLogger().log(Level.WARNING, message, e);
    		return getResponse(Response.Status.UNAUTHORIZED, message);
    	}
    }

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
	public Response getImage(@QueryParam("url") String url) {
		String errorMessage = null;
		if (StringUtil.isEmpty(url)) {
			errorMessage = "URL is not provided";
			getLogger().warning(errorMessage);
			return getResponse(Response.Status.BAD_REQUEST, errorMessage);
		}

		//	TODO: improve this!
		File attachment = getResource(url);
		if (attachment == null || !attachment.exists()) {
			errorMessage = "Attachment " + attachment + " is not defined or does not exist";
			getLogger().warning(errorMessage);
			return getResponse(Response.Status.INTERNAL_SERVER_ERROR, errorMessage);
		}

		String mimeType = MimeTypeUtil.resolveMimeTypeFromFileName(attachment.getName());
		return Response.ok(attachment, mimeType).build();
	}

	private File getResource(String pathInSlide) {
		if (!pathInSlide.startsWith(CoreConstants.WEBDAV_SERVLET_URI))
			pathInSlide = new StringBuilder(CoreConstants.WEBDAV_SERVLET_URI).append(pathInSlide).toString();

		String fileName = pathInSlide;
		int index = fileName.lastIndexOf(CoreConstants.SLASH);
		if (index != -1)
			fileName = pathInSlide.substring(index + 1);

		File file = new File(fileName);
		if (file.exists())
			return file;

		IWSlideService slide = getServiceInstance(IWSlideService.class);
		if (slide == null)
			return null;

		InputStream stream = null;
		try {
			stream = slide.getInputStream(pathInSlide);
		} catch(Exception e) {
			getLogger().log(Level.SEVERE, "Error getting InputStream for: " + pathInSlide, e);
		}
		if (stream == null)
			return null;

		try {
			FileUtil.streamToFile(stream, file);
		} catch(Exception e) {
			getLogger().log(Level.SEVERE, "Error streaming from " + pathInSlide + " to file: " + file.getName(), e);
		} finally {
			IOUtil.closeInputStream(stream);
		}

		return file;
	}

	private IWHttpSessionsManager getSessionsManager() {
		if (httpSessionsManager == null)
			ELUtil.getInstance().autowire(this);
		return httpSessionsManager;
	}

	@GET
	@Path(MobileConstants.URI_PING)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doPing(@QueryParam("JSESSIONID") String httpSessionId) {
		String message = null;
		if (StringUtil.isEmpty(httpSessionId)) {
			message = "HTTP session ID is not provided. It should be provided by parameter 'JSESSIONID'";
			getLogger().warning(message);
			return getResponse(Response.Status.BAD_REQUEST, message);
		}

		try {
			HttpSession session = CoreUtil.getIWContext().getRequest().getSession(false);
			String sessionIdFromRequest = session == null ? CoreConstants.EMPTY : session.getId();
			if (getSessionsManager().isSessionValid(httpSessionId) && httpSessionId.equals(sessionIdFromRequest))
				return getResponse(Response.Status.OK, "Session is valid");

			message = "Session by ID '" + httpSessionId + "' is not valid: probably it has expired or is not the same as expected. " +
					"Expected session ID: '" + sessionIdFromRequest + "', got: '" + httpSessionId + "'";
			getLogger().warning(message);
			return getResponse(Response.Status.NOT_FOUND, message);
		} catch (Exception e) {
			message = "Error pinging session by ID: " + httpSessionId;
			getLogger().log(Level.WARNING, message, e);
		}
		return getResponse(Response.Status.INTERNAL_SERVER_ERROR, message);
	}

}
