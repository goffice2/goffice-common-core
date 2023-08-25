package net.gvcc.goffice.modelmapper;

/**
 *
 * <p>
 * The <code>AlreadyExistsConfigurationException</code> class
 * </p>
 * <p>
 * Data: 10 ott 2022
 * </p>
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @version 1.0
 */
public class AlreadyExistsConfigurationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param msg
	 *            the message for AlreadyExistsConfigurationException
	 */
	public AlreadyExistsConfigurationException(String msg) {
		super(msg);
	}
}
