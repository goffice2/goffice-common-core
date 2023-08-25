package net.gvcc.goffice.modelmapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.web.client.RestTemplate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@SpringBootTest
@EnableAutoConfiguration
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = { RestTemplate.class }, loader = AnnotationConfigContextLoader.class)
public class ModelMapperHelperTest {
	private static Logger LOGGER = LoggerFactory.getLogger(ModelMapperHelperTest.class);

	// ======================================================================================================================== //

	@AllArgsConstructor
	@Getter
	private static class From {
		private String field1;
		private int field2;
		private boolean field3;
	}

	@Setter
	private static class To {
		private String field1;
		private int field2;
		private boolean field3;
	}

	@Setter
	private static class ToWithDifferences {
		private String field1;
		private int field2;
		private boolean differentField; // different field name
	}

	static {
		ModelMapperHelper.addConfiguration(From.class, ToWithDifferences.class, new IMapperConfigurator() {

			@Override
			public void configure(ModelMapper mapper) {
				mapper.getConfiguration().setAmbiguityIgnored(true);

				TypeMap<From, ToWithDifferences> propertyMapper = mapper.createTypeMap(From.class, ToWithDifferences.class);
				propertyMapper.addMapping(From::isField3, ToWithDifferences::setDifferentField);
			}
		});
	}

	// ======================================================================================================================== //

	@Test
	public void objectConversionTest() {
		LOGGER.info("modelMapperHelperTest - START");

		From from = new From("test", 1, true);

		To to = ModelMapperHelper.map(from, To.class);

		assertNotNull(to);
		assertEquals(to.field1, from.field1);
		assertEquals(to.field2, from.field2);
		assertEquals(to.field3, from.field3);

		LOGGER.info("modelMapperHelperTest - END");
	}

	@Test
	public void listConversionTest() {
		LOGGER.info("listConversionTest - START");

		List<From> listIn = Arrays.asList(//
				new From("test1", 1, true), //
				new From("test2", 2, false) //
		);

		final List<To> listOut = ModelMapperHelper.mapList(listIn, To.class);

		assertNotNull(listOut);
		assertEquals(listIn.size(), listOut.size());

		IntStream.range(0, listIn.size()).forEach(index -> {
			From from = listIn.get(index);
			To to = listOut.get(index);
			assertEquals(to.field1, from.field1);
			assertEquals(to.field2, from.field2);
			assertEquals(to.field3, from.field3);
		});

		// with empty list
		final List<To> emptyListOut = ModelMapperHelper.mapList(Collections.emptyList(), To.class);

		assertNotNull(emptyListOut);
		assertTrue(emptyListOut.isEmpty());

		LOGGER.info("listConversionTest - END");
	}

	@Test
	public void objectConversionWithMappingsTest() {
		LOGGER.info("objectConversionWithMappingsTest - START");

		From from = new From("test", 1, true);

		ToWithDifferences to = ModelMapperHelper.map(from, ToWithDifferences.class);

		assertNotNull(to);
		assertEquals(to.field1, from.field1);
		assertEquals(to.field2, from.field2);
		assertEquals(to.differentField, from.field3); // here is the difference with field names of the two objects

		LOGGER.info("objectConversionWithMappingsTest - END");
	}

	@Test
	public void objectConversionWithMappingsErrorTest() {
		LOGGER.info("objectConversionWithMappingsErrorTest - START");

		try {
			ModelMapperHelper.addConfiguration(From.class, ToWithDifferences.class, new IMapperConfigurator() {
				@Override
				public void configure(ModelMapper mapper) {
				}
			});

			throw new Exception("If this exception was thrown, the test is not verified!");
		} catch (AlreadyExistsConfigurationException e) {
			LOGGER.error("objectConversionWithMappingsErrorTest", e);
			assertNotNull(e);
			assertTrue(e.getMessage().startsWith("Already exists a configuration for classes: "));
		} catch (Exception e) {
			LOGGER.error("objectConversionWithMappingsErrorTest", e);
			fail(e);
		}

		LOGGER.info("objectConversionWithMappingsErrorTest - END");

	}

	@Test
	public void parseTextUsingObjectTest() {
		LOGGER.info("parseTextUsingObjectTest - START");

		final String template = "This is a template: field1={field1}, field2={field2}, field3={field3}";

		From from = new From("test", 1, true);

		String body = ModelMapperHelper.parseObject(template, from);
		String expected = "This is a template: field1=test, field2=1, field3=true";
		assertNotNull(body);
		assertEquals(expected, body);

		// with null values
		from = new From(null, 1, true);

		body = ModelMapperHelper.parseObject(template, from, ModelMapperHelper.EMPTY_VALUE_IF_NULL);
		expected = "This is a template: field1=, field2=1, field3=true";
		assertNotNull(body);
		assertEquals(expected, body);

		body = ModelMapperHelper.parseObject(template, from, ModelMapperHelper.NOT_NULL_VALUES);
		expected = "This is a template: field1={field1}, field2=1, field3=true";
		assertNotNull(body);
		assertEquals(expected, body);

		LOGGER.info("parseTextUsingObjectTest - END");
	}

	@Test
	public void parseTextUsingStringTest() {
		LOGGER.info("parseTextUsingStringTest - START");

		String template = "This is a template: myVar={myVar}";

		String expected = "This is a template: myVar=test";
		String body = ModelMapperHelper.parseText(template, "myVar", "test");
		assertNotNull(body);
		assertEquals(expected, body);

		expected = "This is a template: myVar=";
		body = ModelMapperHelper.parseText(template, "myVar", null, ModelMapperHelper.EMPTY_VALUE_IF_NULL);
		assertNotNull(body);
		assertEquals(expected, body);

		expected = "This is a template: myVar={myVar}";
		body = ModelMapperHelper.parseText(template, "myVar", null, ModelMapperHelper.NOT_NULL_VALUES);
		assertNotNull(body);
		assertEquals(expected, body);

		LOGGER.info("parseTextUsingStringTest - END");
	}

	@Test
	public void parseTextUsingMapTest() {
		LOGGER.info("parseTextUsingStringTest - START");

		String template = "This is a template: myVar1={myVar1}, myVar2={myVar2}, myVar3={myVar3}, myVar4={myVar4}";

		Map<String, Object> values = new HashMap<>();
		values.put("myVar1", "1");
		values.put("myVar2", false);
		values.put("myVar3", new Object() {
			@Override
			public String toString() {
				return "3";
			}
		});

		String expected = "This is a template: myVar1=1, myVar2=false, myVar3=3, myVar4={myVar4}";
		String body = ModelMapperHelper.parseMap(template, values);
		assertNotNull(body);
		assertEquals(expected, body);

		values.put("myVar4", null);

		expected = "This is a template: myVar1=1, myVar2=false, myVar3=3, myVar4=";
		body = ModelMapperHelper.parseMap(template, values, ModelMapperHelper.EMPTY_VALUE_IF_NULL);
		assertNotNull(body);
		assertEquals(expected, body);

		expected = "This is a template: myVar1=1, myVar2=false, myVar3=3, myVar4={myVar4}";
		body = ModelMapperHelper.parseMap(template, values, ModelMapperHelper.NOT_NULL_VALUES);
		assertNotNull(body);
		assertEquals(expected, body);

		LOGGER.info("parseTextUsingStringTest - END");
	}
}
