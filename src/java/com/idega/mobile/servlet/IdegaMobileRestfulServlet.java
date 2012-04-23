package com.idega.mobile.servlet;

import java.io.IOException;
import java.net.URI;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.idega.presentation.IWContext;
import com.idega.util.CoreUtil;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class IdegaMobileRestfulServlet extends ServletContainer {

	private static final long serialVersionUID = -6197510276227271433L;

	@Override
	public int service(URI baseUri, URI requestUri, HttpServletRequest request,	HttpServletResponse response) throws ServletException, IOException {
		initializeContext(request, response);
		return super.service(baseUri, requestUri, request, response);
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		initializeContext(request, response);
		super.service(request, response);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		initializeContext(request, response);
		super.doFilter(request, response, chain);
	}

	private void initializeContext(ServletRequest request, ServletResponse response) {
		if (CoreUtil.getIWContext() == null)
			new IWContext((HttpServletRequest) request, (HttpServletResponse) response, getServletContext());
	}

	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		initializeContext(request, response);
		super.doFilter(request, response, chain);
	}

}