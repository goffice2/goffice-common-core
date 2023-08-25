package net.gvcc.goffice;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@SpringBootTest
@EnableAutoConfiguration
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = { RestTemplate.class }, loader = AnnotationConfigContextLoader.class)
@TestPropertySources({ @TestPropertySource("classpath:application.properties") })
@Import(TestApplication.class)
public class BaseServiceApplicationTest {
	private static Logger LOGGER = LoggerFactory.getLogger(BaseServiceApplicationTest.class);

	// ==================================================================================================================================== //

	@Autowired
	private TestApplication testAppWithProperties;

	@Autowired
	private TestApplication testAppWithoutProperties;

	// ==================================================================================================================================== //

	@Test
	void startApplicationWithPropertiesTest() {
		LOGGER.info("startApplicationWithPropertiesTest - START");

		OpenAPI openApi = testAppWithProperties.customizeOpenAPI();
		assertNotNull(openApi);

		Info info = openApi.getInfo();
		assertNotNull(info);
		assertTrue(TestApplication.ARTIFACT.equals(info.getTitle()));
		assertTrue(TestApplication.ARTIFACT.concat(" - API Swagger documentation").equals(info.getDescription()));
		assertTrue(TestApplication.VERSION.equals(info.getVersion()));

		License license = info.getLicense();
		assertNotNull(license);
		assertNotNull(license.getName());
		assertNotNull(license.getUrl());

		List<Server> serverList = openApi.getServers();
		assertNotNull(serverList);
		assertEquals(1, serverList.size());
		Server server = serverList.get(0);
		assertNotNull(server);
		assertTrue("https://test.mockito.net".equals(server.getUrl()));
		assertTrue("server-test".equals(server.getDescription()));

		LOGGER.info("startApplicationWithPropertiesTest - END");
	}

	@Test
	void startApplicationWithoutProperties1Test() {
		LOGGER.info("startApplicationWithoutProperties2Test - START");

		testAppWithProperties.setName(TestApplication.NAME);

		OpenAPI openApi = testAppWithProperties.customizeOpenAPI();
		assertNotNull(openApi);

		Info info = openApi.getInfo();
		assertNotNull(info);
		assertTrue(TestApplication.NAME.equals(info.getTitle()));
		assertTrue(TestApplication.NAME.concat(" - API Swagger documentation").equals(info.getDescription()));
		assertTrue(TestApplication.VERSION.equals(info.getVersion()));

		License license = info.getLicense();
		assertNotNull(license);
		assertNotNull(license.getName());
		assertNotNull(license.getUrl());

		List<Server> serverList = openApi.getServers();
		assertNotNull(serverList);
		assertEquals(1, serverList.size());
		Server server = serverList.get(0);
		assertNotNull(server);
		assertTrue("https://test.mockito.net".equals(server.getUrl()));
		assertTrue("server-test".equals(server.getDescription()));

		LOGGER.info("startApplicationWithoutProperties2Test - END");
	}

	@Test
	void startApplicationWithoutProperties2Test() {
		LOGGER.info("startApplicationWithoutProperties2Test - START");

		try {
			// =================================================================== //
			// reset single properties
			// =================================================================== //
			resetFieldValue(testAppWithoutProperties, "openapiInfoLicenseUrl", null);

			OpenAPI openApi = testAppWithoutProperties.customizeOpenAPI();
			assertNotNull(openApi);

			Info info = openApi.getInfo();
			assertNotNull(info);

			License license = info.getLicense();
			assertNotNull(license);
			assertNotNull(license.getName());
			assertNull(license.getUrl());
		} catch (Exception e) {
			LOGGER.error("startApplicationWithoutProperties2Test", e);
			fail(e);
		}

		LOGGER.info("startApplicationWithoutProperties2Test - END");
	}

	@Test
	void startApplicationWithoutProperties3Test() {
		LOGGER.info("startApplicationWithoutProperties3Test - START");

		try {
			testAppWithProperties.clear();

			// =================================================================== //
			// reset all other properties
			// =================================================================== //
			resetFieldValue(testAppWithoutProperties, "openapiRemoveSnapshotSuffix", false);
			resetFieldValue(testAppWithoutProperties, "openapiInfoLicenseUrl", null);
			resetFieldValue(testAppWithoutProperties, "openapiServerUrl", null);
			resetFieldValue(testAppWithoutProperties, "openapiServerDescription", null);
			resetFieldValue(testAppWithoutProperties, "openapiInfoLicenseName", null);

			OpenAPI openApi = testAppWithoutProperties.customizeOpenAPI();
			assertNotNull(openApi);

			Info info = openApi.getInfo();
			assertNotNull(info);

			License license = info.getLicense();
			assertNull(license);

			List<Server> serverList = openApi.getServers();
			assertNull(serverList);
		} catch (Exception e) {
			LOGGER.error("startApplicationWithoutProperties3Test", e);
			fail(e);
		}

		LOGGER.info("startApplicationWithoutProperties3Test - END");
	}

	private static void resetFieldValue(TestApplication app, String fieldName, Object value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = app.getClass();
		Class<?> superClazz = clazz.getSuperclass();

		Field field = superClazz.getDeclaredField(fieldName);
		field.setAccessible(true);

		field.set(app, value);
	}
}
