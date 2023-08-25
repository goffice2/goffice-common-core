package net.gvcc.goffice.modelmapper.analizer;

/**
 * A default parser which returns keys/values as they are provided
 *
 * <p>
 * The <code>DefaultParserAnalizer</code> class
 * </p>
 * <p>
 * Data: Jul 21, 2023
 * </p>
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @version 1.0
 */
public class DefaultParserAnalizer implements IParserAnalizer {

	@Override
	public boolean accept(String key, Object value) {
		return true;
	}

	@Override
	public String key(String key) {
		return key;
	}

	@Override
	public Object value(Object value) {
		return value;
	}
}
