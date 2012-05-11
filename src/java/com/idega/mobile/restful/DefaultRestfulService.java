package com.idega.mobile.restful;

import java.io.Serializable;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.google.gson.Gson;
import com.idega.core.business.DefaultSpringBean;

public abstract class DefaultRestfulService extends DefaultSpringBean {

    protected Response getResponse(Response.Status status, Serializable message) {
		ResponseBuilder responseBuilder = Response.status(status.getStatusCode());
		Response response = responseBuilder.entity(getJSON(message)).build();
		return response;
	}

    protected String getJSON(Serializable object) {
		Gson gson = new Gson();
		return gson.toJson(object);
	}

}