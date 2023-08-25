package net.gvcc.goffice.modelmapper.analizer;

import net.gvcc.goffice.modelmapper.ModelMapperHelper;

/**
 * A parser which exclude the null value from the elaboration
 *
 * <p>
 * The <code>NotNullValuesParserAnalizer</code> class
 * </p>
 * <p>
 * Data: Jul 21, 2023
 * </p>
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @version 1.0
 */
public class NotNullValuesParserAnalizer extends DefaultParserAnalizer {
	@Override
	public boolean accept(String key, Object value) {
		// return value == null || value == ModelMapperHelper.NULL_VALUE ? false : super.accept(key, value);
		return value != null && value != ModelMapperHelper.NULL_VALUE && super.accept(key, value); // replace previous line because PDM violetion
	}
}
