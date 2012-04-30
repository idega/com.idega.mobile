package com.idega.mobile;

import java.util.logging.Level;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.x_rd.ee.municipality.producer.MunicipalityserviceStub.CaseListEntry_type0;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.idega.core.business.DefaultSpringBean;
import com.idega.util.ArrayUtil;
import com.idega.util.expression.ELUtil;
import com.idega.xroad.business.CasesDataProvider;

/**
 * Description
 * User: Simon Sönnby
 * Date: 2012-03-14
 * Time: 09:33
 */
@Path("/mobile/test")
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WebserviceTest extends DefaultSpringBean {

	@Autowired
	private CasesDataProvider caseDataProvider;

	CasesDataProvider getCaseDataProvider() {
		if (caseDataProvider == null)
			ELUtil.getInstance().autowire(this);
		return caseDataProvider;
	}

	@GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public String getTestJSON() {
        return "{\"Test\":\"hello\"}";
    }

	@GET
	@Path("/cases")
	@Produces(MediaType.APPLICATION_JSON)
    public String getCasesList() {
		Gson gson = new Gson();
		CaseListEntry_type0[] cases = null;
		try {
			cases = getCaseDataProvider().getCases();
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Error getting test cases list", e);
		}
    	if (ArrayUtil.isEmpty(cases)) {
    		getLogger().warning("No cases found");
    		return gson.toJson("Error");
    	}

    	return gson.toJson(cases);
    }

//    @POST
//    @Path("/post")
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response createTestInJSON(String test) {
//        return Response.status(201).entity("Success").build();
//    }
}
