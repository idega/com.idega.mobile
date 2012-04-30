package com.idega.mobile.restful;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import com.idega.core.business.DefaultSpringBean;

/**
 * Description
 * User: Simon Sönnby
 * Date: 2012-03-14
 * Time: 09:33
 */
@Path("/*/")
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
}
