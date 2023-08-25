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
package net.gvcc.goffice.opentracing;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * @author marco.mancuso
 *
 */
@Component
public class ThreadLocalOpenTracingStorage implements IOpenTracingStorage {

	/**
	 * 
	 */
	private static ThreadLocal<Map<String, List<String>>> currentOpenTracingData = new ThreadLocal<>();

	public void clear() {
		currentOpenTracingData.remove();
	}

	@Override
	public void setHeaders(Map<String, List<String>> headers) {
		currentOpenTracingData.set(headers);
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		return currentOpenTracingData.get();
	}

}
