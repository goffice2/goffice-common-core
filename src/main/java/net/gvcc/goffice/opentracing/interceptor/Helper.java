package net.gvcc.goffice.opentracing.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.gvcc.goffice.opentracing.HeadersFilter;
import net.gvcc.goffice.opentracing.IOpenTracingStorage;

public class Helper {
	private static Logger LOGGER = LoggerFactory.getLogger(Helper.class);

	/**
	 * The structure of the object which walks the headers storage.
	 *
	 * <p>
	 * The <code>IHeaderWalker</code> class
	 * </p>
	 * <p>
	 * Data: Jul 24, 2023
	 * </p>
	 * 
	 * @author Renzo Poli
	 */
	public interface IHeaderWalker {
		Enumeration<String> getHeaderNames();

		Enumeration<String> getHeaders(String headerName);
	}

	/**
	 * Extracts the headers values from the walker and set them into the opentracing storage (like threadlocal storage)
	 * 
	 * @param openTracingStorage
	 *            The target storage
	 * @param headersWalker
	 *            The source headers
	 */
	public static void setOpentracingHeaders(IOpenTracingStorage openTracingStorage, IHeaderWalker headersWalker) {
		LOGGER.trace("setOpentracingHeaders - START");

		Map<String, List<String>> headers = new HashMap<>();

		Enumeration<String> names = headersWalker.getHeaderNames();
		if (names != null) {
			Collections.list(names).stream() //
					.filter(HeadersFilter.IS_OPENTRACING_HEADER) //
					.forEach(headerName -> {
						Enumeration<String> values = headersWalker.getHeaders(headerName);
						if (values != null) {
							List<String> headerValues = new ArrayList<>();
							Collections.list(values).forEach(value -> headerValues.add(value));
							headers.put(headerName, headerValues);
						}
					});
			openTracingStorage.setHeaders(headers);

			LOGGER.trace("setOpentracingHeaders - registering headers into opentracing threadlocal: {}", headers);
		}

		// String requestId = request.getHeader(Constants.OPENTRACING_REQUEST_ID);
		// if (requestId != null) {
		// openTracingStorage.setRequestId(requestId);
		// }

		LOGGER.trace("setOpentracingHeaders - END");
	}
}
