package net.gvcc.goffice.modelmapper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.gvcc.goffice.modelmapper.analizer.DefaultParserAnalizer;
import net.gvcc.goffice.modelmapper.analizer.EmptyValueIfNullParserAnalizer;
import net.gvcc.goffice.modelmapper.analizer.IParserAnalizer;
import net.gvcc.goffice.modelmapper.analizer.NotNullValuesParserAnalizer;

/**
 *
 * <p>
 * The <code>ModelMapperHelper</code> class
 * </p>
 * <p>
 * Data: 10 ott 2022
 * </p>
 * 
 * @author <a href="mailto:edv@gvcc.net"></a>
 * @version 1.0
 */
public class ModelMapperHelper {
	private static Logger LOGGER = LoggerFactory.getLogger(ModelMapperHelper.class);

	private static Map<String, ModelMapper> mappers = new HashMap<>();
	private static Map<String, IMapperConfigurator> mapperConfigurations = new HashMap<>();

	private static final String PLACEHOLDER_DELIMITER_START = "{";
	private static final String PLACEHOLDER_DELIMITER_END = "}";

	// =========================================================================================== //

	public static final IParserAnalizer EMPTY_VALUE_IF_NULL = new EmptyValueIfNullParserAnalizer();
	public static final IParserAnalizer NOT_NULL_VALUES = new NotNullValuesParserAnalizer();

	// =========================================================================================== //

	public static final Object NULL_VALUE = new Object() {
		@Override
		public String toString() {
			return "This object identifies a NULL value!";
		}
	};

	/**
	 * Adds a new model mapper configuration
	 * 
	 * This method throws an AlreadyExistsConfigurationException if the configuration already exists
	 * 
	 * @param sourceType
	 *            The sourceType object represent the class
	 * @param destinationType
	 *            The destinationType object represent the class
	 * @param configurator
	 *            The configurator object represent the IMapperConfigurator
	 * 
	 * @see addConfiguration(Class<?> sourceType, Class<?> destinationType, IMapperConfigurator configurator, boolean allowOverride)
	 */
	public static void addConfiguration(Class<?> sourceType, Class<?> destinationType, IMapperConfigurator configurator) {
		addConfiguration(sourceType, destinationType, configurator, false);
	}

	/**
	 * Adds a new model mapper configuration
	 * 
	 * @param sourceType
	 *            The sourceType object represent the class
	 * @param destinationType
	 *            The destinationType object represent the class
	 * @param configurator
	 *            The configurator object represent the IMapperConfigurator
	 * @param allowOverride
	 *            The allowOverride boolean. When false and a configurarion already exists, an AlreadyExistsConfigurationException will be thrown
	 * 
	 * @see addConfiguration(Class<?> sourceType, Class<?> destinationType, IMapperConfigurator configurator)
	 */
	public static void addConfiguration(Class<?> sourceType, Class<?> destinationType, IMapperConfigurator configurator, boolean allowOverride) {
		if (configurator != null) {
			String mapperName = makeKey(sourceType, destinationType);
			if (mapperConfigurations.containsKey(mapperName) && !allowOverride) { // if already exists!
				throw new AlreadyExistsConfigurationException("Already exists a configuration for classes: " //
						.concat(sourceType.getName()) //
						.concat(" -> ") //
						.concat(destinationType.getName()) //
				);
			}
			mapperConfigurations.put(mapperName, configurator);
		}
	}

	/**
	 * Returns a mapper which corresponding to a parameters
	 * 
	 * @param sourceType
	 *            The sourceType object represent the class
	 * @param destinationType
	 *            The destinationType object represent the class
	 * @return The mapper as ModelMapper object
	 */
	public static ModelMapper getMapper(Class<?> sourceType, Class<?> destinationType) {
		LOGGER.info("getMapper - START");

		String mapperName = makeKey(sourceType, destinationType);

		ModelMapper mapper = mappers.get(mapperName);
		if (mapper == null) {
			LOGGER.debug("getMapper - create new mapper instance...");
			mapper = new ModelMapper();

			IMapperConfigurator configurator = mapperConfigurations.get(mapperName);
			if (configurator != null) {
				LOGGER.debug("getMapper - configure mapper instance...");
				configurator.configure(mapper);
				LOGGER.debug("getMapper - configure mapper instance...done");
			}
			// put the new mapper in cache
			mappers.put(mapperName, mapper);

			LOGGER.debug("getMapper - create new mapper instance...done");
		} else {
			LOGGER.debug("getMapper - got mapper instance from cache!");
		}

		LOGGER.info("getMapper - END");

		return mapper;
	}

	/**
	 * Transform an object to a different one and copy its properties
	 * 
	 * @param <D>
	 *            The entity type
	 * @param source
	 *            The source object
	 * @param destinationType
	 *            The destinationType object represent the class
	 * @return Destination as generic entity
	 */
	public static <D> D map(Object source, Class<D> destinationType) {
		D destination = null;

		if (source != null) {
			ModelMapper mapper = getMapper(source.getClass(), destinationType);
			destination = mapper.map(source, destinationType);
		}

		return destination;
	}

	/**
	 * Maps a list of object to a list of different objects and copy the properties of each one
	 * 
	 * @param <D>
	 *            the entity type
	 * @param sourceList
	 *            the list of source object
	 * @param destinationType
	 * @return Destination as generic list entity
	 */
	public static <D> List<D> mapList(List<? extends Object> sourceList, Class<D> destinationType) {
		List<D> destination = null;

		if (sourceList != null) {
			if (sourceList.isEmpty()) {
				destination = Collections.emptyList();
			} else {
				ModelMapper[] mapper = new ModelMapper[] { null };
				destination = sourceList.stream() //
						.filter(Objects::nonNull) //
						.map(source -> {
							if (mapper[0] == null) {
								mapper[0] = getMapper(source.getClass(), destinationType);
							}
							return mapper[0].map(source, destinationType);
						}) //
						.collect(Collectors.toList());
			}
		}

		return destination;
	}

	/**
	 * Transforms an object to a map, using the object declared fields.
	 * 
	 * The name of each field is used as a key of the map. The value of each field is used as a value of the map
	 * 
	 * @param data
	 *            The object instance
	 * @return A map as key-value pais data which represents the data fields
	 */
	public static Map<String, Object> toMap(Object data) {
		LOGGER.trace("toMap - START");

		Map<String, Object> map = null;
		if (data != null) {
			Field[] fields = data.getClass().getDeclaredFields();

			map = Arrays.asList(fields).stream() //
					.map(field -> {
						try {
							field.setAccessible(true);
							String name = field.getName();
							Object value = field.get(data);

							if (value == null) {
								value = NULL_VALUE;
							}

							return new Object[] { name, value };
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}) //
					.collect(Collectors.toMap(p -> (String) p[0], p -> p[1]));
		}

		LOGGER.trace("toMap - END");

		return map;
	}

	/**
	 * This method can help you to parse text (template) replacing the placeholder with a value.
	 * 
	 * Example:
	 * <p>
	 * template: "This is an {placeholder}"
	 * <p>
	 * placeholder: "placeholder"
	 * <p>
	 * value: "example"
	 * <p>
	 * Result: "This is an example"
	 * 
	 * @param template
	 * @param placeholder
	 * @param value
	 * @return a parsed text
	 * 
	 * @see parseText(String template, String name, Object value, IParserAnalizer analizer)
	 * @see parseObject(String template, Object data)
	 * @see parseObject(String template, Object data, IParserAnalizer analizer)
	 * @see parseMap(String template, Map<String, Object> data)
	 * @see parseMap(String template, Map<String, Object> data, IParserAnalizer analizer)
	 */
	public static String parseText(String template, String placeholder, Object value) {
		return parseMap(template, Collections.singletonMap(placeholder, value), null);
	}

	/**
	 * This method can help you to parse text (template) replacing the placeholder with a value.
	 * 
	 * Example:
	 * <p>
	 * template: "This is an {placeholder}"
	 * <p>
	 * placeholder: "placeholder"
	 * <p>
	 * value: "example"
	 * <p>
	 * Result: "This is an example"
	 * 
	 * @param template
	 * @param name
	 * @param value
	 * @param analizer
	 *            {@link net.gvcc.goffice.modelmapper.analizer.IParserAnalizer IParserAnalizer}
	 * @return a parsed text
	 * 
	 * @see parseText(String template, String name, Object value)
	 * @see parseObject(String template, Object data)
	 * @see parseObject(String template, Object data, IParserAnalizer analizer)
	 * @see parseMap(String template, Map<String, Object> data)
	 * @see parseMap(String template, Map<String, Object> data, IParserAnalizer analizer)
	 */

	public static String parseText(String template, String name, Object value, IParserAnalizer analizer) {
		if (value == null) {
			value = NULL_VALUE;
		}
		return parseMap(template, Collections.singletonMap(name, value), analizer);
	}

	/**
	 * This method can help you to parse text (template) replacing the placeholder with a values of data fields.
	 * 
	 * Example:
	 * <p>
	 * template: "This is an {placeholder1} about a {placeholder2}"
	 * <p>
	 * data: instace of a class like: { private String placeholder1 = "example"; private String placeholder2 = "replacement"; }
	 * <p>
	 * Result: "This is an example about a replacement"
	 * 
	 * @param template
	 * @param data
	 * @return a parsed text
	 * 
	 * @see parseObject(String template, Object data, IParserAnalizer analizer)
	 * @see parseText(String template, String name, Object value, IParserAnalizer analizer)
	 * @see parseText(String template, String name, Object value)
	 * @see parseMap(String template, Map<String, Object> data)
	 * @see parseMap(String template, Map<String, Object> data, IParserAnalizer analizer)
	 */
	public static String parseObject(String template, Object data) {
		return parseMap(template, toMap(data), null);
	}

	/**
	 * This method can help you to parse text (template) replacing the placeholder with a values of data fields.
	 * 
	 * Example:
	 * <p>
	 * template: "This is an {placeholder1} about a {placeholder2}"
	 * <p>
	 * data: instace of a class like: { private String placeholder1 = "example"; private String placeholder2 = "replacement"; }
	 * <p>
	 * Result: "This is an example about a replacement"
	 * 
	 * @param template
	 * @param data
	 * @param analizer
	 *            {@link net.gvcc.goffice.modelmapper.analizer.IParserAnalizer IParserAnalizer}
	 * @return a parsed text
	 * 
	 * @see parseObject(String template, Object data)
	 * @see parseText(String template, String name, Object value, IParserAnalizer analizer)
	 * @see parseText(String template, String name, Object value)
	 * @see parseMap(String template, Map<String, Object> data)
	 * @see parseMap(String template, Map<String, Object> data, IParserAnalizer analizer)
	 */
	public static String parseObject(String template, Object data, IParserAnalizer analizer) {
		return parseMap(template, toMap(data), analizer);
	}

	/**
	 * This method can help you to parse text (template) replacing the placeholder with a values of the map.
	 * 
	 * Example:
	 * <p>
	 * template: "This is an {placeholder1} about a {placeholder2}"
	 * <p>
	 * map: a map like: { "placeholder1": "example", "placeholder2" : "replacement" }
	 * <p>
	 * Result: "This is an example about a replacement"
	 * 
	 * @param template
	 * @param data
	 * @return a parsed text
	 * 
	 * @see parseMap(String template, Map<String, Object> data, IParserAnalizer analizer)
	 * @see parseObject(String template, Object data)
	 * @see parseObject(String template, Object data, IParserAnalizer analizer)
	 * @see parseText(String template, String name, Object value, IParserAnalizer analizer)
	 * @see parseText(String template, String name, Object value)
	 */
	public static String parseMap(String template, Map<String, Object> data) {
		return parseMap(template, data, null);
	}

	/**
	 * This method can help you to parse text (template) replacing the placeholder with a values of the map.
	 * 
	 * Example:
	 * <p>
	 * template: "This is an {placeholder1} about a {placeholder2}"
	 * <p>
	 * map: a map like: { "placeholder1": "example", "placeholder2" : "replacement" }
	 * <p>
	 * Result: "This is an example about a replacement"
	 * 
	 * @param template
	 * @param map
	 * @param analizer
	 * @return a parsed text
	 * 
	 * @see parseMap(String template, Map<String, Object> data)
	 * @see parseObject(String template, Object data)
	 * @see parseObject(String template, Object data, IParserAnalizer analizer)
	 * @see parseText(String template, String name, Object value, IParserAnalizer analizer)
	 * @see parseText(String template, String name, Object value)
	 */

	public static String parseMap(String template, Map<String, Object> map, IParserAnalizer analizer) {
		LOGGER.trace("parseText - START");

		LOGGER.trace("parseText - template={}", template);

		String[] text = new String[] { template };

		if (analizer == null) {
			analizer = new DefaultParserAnalizer();
		}

		final IParserAnalizer _analizer = analizer;

		if (template != null && map != null) {
			map.keySet().stream() //
					.forEach(key -> {
						Object value = map.get(key);

						if (_analizer.accept(key, value)) {
							key = _analizer.key(key);
							value = _analizer.value(value);

							if (value != null) {
								String regex = Pattern.quote(PLACEHOLDER_DELIMITER_START + key + PLACEHOLDER_DELIMITER_END);
								text[0] = text[0].replaceAll(regex, value.toString());
							}
						}
					});
		}

		LOGGER.trace("parseText - text={}", text[0]);
		LOGGER.trace("parseText - END");

		return text[0];
	}

	// =========================================================================================== //

	/**
	 * 
	 * @param sourceType
	 *            the sourceType object represent the class
	 * @param destinationType
	 *            the destinationType object represent the class
	 * @return the key as string
	 */
	private static String makeKey(Class<?> sourceType, Class<?> destinationType) {
		return sourceType.getName().concat(" -> ").concat(destinationType.getName());
	}
}
