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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 *
 * <p>
 * The <code>ResponseDTO</code> class
 * </p>
 * <p>
 * Data: 3 mar 2022
 * </p>
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @author cristian muraca
 * @version 1.0
 */
public class ResponseDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String description;
	private String errorCode;
	@SuppressFBWarnings(value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" }, justification = "avoid clone objects")
	private List<MessageDTO> localizedMessages;
	private String gvccCode;

	/**
	 * @param erroCode
	 */
	public ResponseDTO(String erroCode) {
		this.setErrorCode(erroCode);
	}

	public ResponseDTO() {
	}

	/**
	 * @return String
	 */
	public String getGvccCode() {
		return gvccCode;
	}

	/**
	 * @param gvccCode
	 */
	public void setGvccCode(String gvccCode) {
		this.gvccCode = gvccCode;
	}

	/**
	 * @return String
	 */
	public String getGvccKey() {
		return gvccKey;
	}

	/**
	 * @param gvccKey
	 */
	public void setGvccKey(String gvccKey) {
		this.gvccKey = gvccKey;
	}

	private String gvccKey;

	/**
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return String
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @return List
	 */
	public List<MessageDTO> getLocalizedMessages() {
		return localizedMessages;
	}

	/**
	 * @param localizedMessages
	 */
	public void setLocalizedMessages(List<MessageDTO> localizedMessages) {
		this.localizedMessages = localizedMessages;
	}

	/**
	 * @return long
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
	}

	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
	}

}
