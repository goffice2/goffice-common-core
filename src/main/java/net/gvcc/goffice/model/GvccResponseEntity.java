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
package net.gvcc.goffice.model;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 *
 * <p>
 * The <code>GvccResponseEntity</code> class
 * </p>
 * <p>
 * Data: 21 mar 2022
 * </p>
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @author cristian muraca
 * @version 1.0
 */
public class GvccResponseEntity {
	private static final Logger LOGGER = LoggerFactory.getLogger(GvccResponseEntity.class);

	/**
	 * @param <T>
	 * @param body
	 *            The body of the response
	 * @param map
	 *            The error maps of the current component
	 * @return A ResonseEntity
	 */
	public static <T extends ResponseDTO> ResponseEntity<T> body(T body, Map<String, GvccError> map) {
		return ResponseEntity.status(processStatus(body, map)).body(body);
	}

	/**
	 * @param <T>
	 * @param body
	 * @param map
	 * @return HttpStatus
	 */
	private static <T extends ResponseDTO> HttpStatus processStatus(T body, Map<String, GvccError> map) {
		LOGGER.debug("processStatus - START");

		// HttpStatus status = HttpStatus.OK;
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

		if (body != null) {
			String statusCode = StringUtils.defaultIfBlank(body.getErrorCode(), "OK");
			LOGGER.debug("processStatus - statusCode: {}", statusCode);

			switch (statusCode) {
				case "OK":
					status = HttpStatus.OK;
					break;

				default:
					GvccError info = map.get(statusCode);
					try {
						if (info == null) {
							throw new Exception("Error Code not managed");
						}

						status = info.getStatus();
						body.setGvccKey(info.getGvccKey());
						body.setGvccCode(info.getGvccCode());
					} catch (Exception ex) {
						LOGGER.error("processStatus - {}" // message
								+ "\nerror code:..{}" // errorCode
								+ "\nbody:........{}", // body
								ex.getMessage(), statusCode, body);
						status = HttpStatus.INTERNAL_SERVER_ERROR;
					}
			}
		}

		LOGGER.debug("processStatus - STOP");

		return status;
	}

	private static final GvccError RESPONSE_OK = new GvccError() {

		@Override
		public HttpStatus getStatus() {
			return HttpStatus.OK;
		}

		@Override
		public String getDescription() {
			return "OK";
		}

		@Override
		public String getGvccCode() {
			return "";
		}

		@Override
		public String getGvccKey() {
			return "";
		}
	};

	public static <T extends ResponseDTO> ResponseEntity<T> toResponseEntity(GvccError error, Map<String, GvccError> errorByExtCode, Class<T> clazz) {
		ResponseEntity<T> responseEntity = null;

		if (error == null) {
			error = RESPONSE_OK;
		}

		try {
			Class<?>[] arguments = null;
			T response = clazz.getConstructor(arguments).newInstance();
			response.setErrorCode(error.getGvccCode());
			response.setDescription(error.getDescription());
			responseEntity = body(response, errorByExtCode);
		} catch (Exception e) {
			LOGGER.error("toResponseEntity", e);
			throw new RuntimeException(HttpStatus.INTERNAL_SERVER_ERROR.toString());
		}

		// commented by SPOTBUGS violation
		// if (responseEntity == null) {
		// responseEntity = ResponseEntity.internalServerError().build();
		// }

		return responseEntity;
	}
}
