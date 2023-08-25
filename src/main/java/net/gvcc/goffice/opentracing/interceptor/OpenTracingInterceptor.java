/*
 * goffice... 
 * https://www.goffice.org
 * 
 * Copyright (c) 2005-2022 Consorzio dei Comuni della Provincia di Bolzano Soc. Coop. <https://www.gvcc.net>.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.gvcc.goffice.opentracing.interceptor;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import net.gvcc.goffice.opentracing.IOpenTracingStorage;
import net.gvcc.goffice.opentracing.interceptor.Helper.IHeaderWalker;

/**
 * @author renzo.poli
 *
 */
@Component
public class OpenTracingInterceptor implements HandlerInterceptor {
	private static Logger LOGGER = LoggerFactory.getLogger(OpenTracingInterceptor.class);

	@Autowired
	private IOpenTracingStorage openTracingStorage;

	/**
	 * This method walks the HttpServler request headers and returns the values.
	 * <p>
	 * Then, it puts them into an opentracing storage (like a threadlocal)
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		LOGGER.trace("preHandle - START");

		try {
			Helper.setOpentracingHeaders(openTracingStorage, new IHeaderWalker() {

				@Override
				public Enumeration<String> getHeaderNames() {
					return request.getHeaderNames();
				}

				@Override
				public Enumeration<String> getHeaders(String headerName) {
					return request.getHeaders(headerName);
				}
			});
		} catch (Exception e) {
			LOGGER.error("preHandle", e);
		}

		LOGGER.trace("preHandle - END");

		return true;
	}

	/**
	 * Opentracing storage cleaner
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		LOGGER.trace("postHandle - START");

		openTracingStorage.clear();

		LOGGER.trace("postHandle - END");
	}
}