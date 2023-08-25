package net.gvcc.goffice.opentracing;

import java.util.function.Predicate;

public class HeadersFilter {
	/**
	 * The standard name of the opentracing HTTP header
	 */
	public static final String REQUEST_ID = "x-request-id";
	/**
	 * The prefix of the standard B3 HTTP headers
	 */
	public static final String REQUEST_B3_PREFIX = "x-b3-";

	/**
	 * Filter that is applied to identify the opentracing headers
	 */
	public static final Predicate<String> IS_OPENTRACING_HEADER = headerName -> headerName.equalsIgnoreCase(REQUEST_ID) || headerName.startsWith(REQUEST_B3_PREFIX);
}
