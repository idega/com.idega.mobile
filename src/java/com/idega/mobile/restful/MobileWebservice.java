package com.idega.mobile.restful;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import com.google.gson.Gson;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.file.util.MimeTypeUtil;
import com.idega.mobile.MobileConstants;
import com.idega.slide.business.IWSlideService;
import com.idega.util.CoreConstants;
import com.idega.util.FileUtil;
import com.idega.util.IOUtil;
import com.idega.util.StringUtil;

/**
 * Description
 * User: Simon Sönnby
 * Date: 2012-03-14
 * Time: 09:33
 */
@Path(MobileConstants.URI)
public class MobileWebservice extends DefaultSpringBean  {

    @GET
    @Path("/getJSON")
    @Produces(MediaType.APPLICATION_JSON)
    public String getJSON() {
        // https://localhost:8443/mobile/getJSON

        final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<simple-list>\n" +
                "  <timestamp>232423423423</timestamp>\n" +
                "  <authors>\n" +
                "    <author>\n" +
                "      <firstName>Tim</firstName>\n" +
                "      <lastName>Leary</lastName>\n" +
                "    </author>\n" +
                "  </authors>\n" +
                "  <title>Flashbacks</title>\n" +
                "  <shippingWeight>1.4 pounds</shippingWeight>\n" +
                "  <isbn>978-0874778700</isbn>\n" +
                "</simple-list>";

        XMLSerializer xmlSerializer = new XMLSerializer();
        JSON json = xmlSerializer.read(xml);

        return json.toString();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String index() {
        // https://localhost:8443/mobile/
        return "{\"test\":\"test\"}";
    }

    @GET
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(@QueryParam("username") String username, @QueryParam("password") String password) {
        // https://localhost:8443/mobile/login?username=test&password=test
        return username.equals("test") && password.equals("test") ?
                Response.status(Response.Status.ACCEPTED).entity("Success").build() :
                Response.status(Response.Status.UNAUTHORIZED).entity("Failed").build();
    }

    private Response getResponse(Response.Status status, Serializable message) {
		ResponseBuilder responseBuilder = Response.status(status.getStatusCode());
		Response response = responseBuilder.entity(getJSON(message)).build();
		return response;
	}

    private String getJSON(Serializable object) {
		Gson gson = new Gson();
		return gson.toJson(object);
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

		String fileName = pathInSlide;
		int index = fileName.lastIndexOf(CoreConstants.SLASH);
		if (index != -1)
			fileName = pathInSlide.substring(index + 1);

		File file = new File(fileName);
		if (file.exists())
			return file;

		try {
			FileUtil.streamToFile(stream, file);
		} catch(Exception e) {
			getLogger().log(Level.SEVERE, "Error streaming from " + pathInSlide + " to file: " + file.getName(), e);
		} finally {
			IOUtil.closeInputStream(stream);
		}

		return file;
	}
}
