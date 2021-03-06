package com.kings.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

public interface GenericKingsControllerInterface {
	
	/**
	 * Called before each request
	 * @param request
	 * @param response
	 * @throws Exception 
	 */
	public boolean preHandleRequest(
			HttpServletRequest request,
            HttpServletResponse response) throws Exception;
	
	/**
	 * Called after each request
	 * @param request
	 * @param response
	 * @param modelAndView
	 */
	public void postHandleRequest(
			HttpServletRequest request, 
			HttpServletResponse response, 
			ModelAndView modelAndView);
}
