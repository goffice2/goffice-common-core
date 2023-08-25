package net.gvcc.goffice.modelmapper.analizer;

import net.gvcc.goffice.modelmapper.ModelMapperHelper;

/**
 * A parser which transforms a null values into a empty string
 *
 * <p>
 * The <code>EmptyValueIfNullParserAnalizer</code> class
 * </p>
 * <p>
 * Data: Jul 21, 2023
 * </p>
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @version 1.0
 */
public class EmptyValueIfNullParserAnalizer extends DefaultParserAnalizer {
	/**
	 * Return an empty value when the value is null
	 */
	@Override
	public Object value(Object value) {
		return value == null || value == ModelMapperHelper.NULL_VALUE ? "" : super.value(value);
	}
}
