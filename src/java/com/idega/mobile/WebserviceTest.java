package com.idega.mobile;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Description
 * User: Simon Sönnby
 * Date: 2012-03-14
 * Time: 09:33
 */
@Path("/test")
public class WebserviceTest {
    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public String getTestJSON() {
        return "{\"Test\":\"hello\"}";
    }

//    @POST
//    @Path("/post")
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response createTestInJSON(String test) {
//        return Response.status(201).entity("Success").build();
//    }
}
