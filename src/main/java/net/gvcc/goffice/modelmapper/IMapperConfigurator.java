package net.gvcc.goffice.modelmapper;

import org.modelmapper.ModelMapper;

/**
 *
 * <p>
 * The <code>IMapperConfigurator</code> class
 * </p>
 * <p>
 * Data: 10 ott 2022
 * </p>
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @version 1.0
 */
public interface IMapperConfigurator {
	/**
	 * Used to add a mapper configuration
	 * 
	 * @param mapper
	 */
	void configure(ModelMapper mapper);
}
