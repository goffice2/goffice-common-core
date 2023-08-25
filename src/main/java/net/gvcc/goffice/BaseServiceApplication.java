package net.gvcc.goffice;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * A base application to use to create SpingBoot application.
 * <p>
 * It provides some features such as OpenAPI customizations.
 *
 * <p>
 * The <code>BaseServiceApplication</code> class
 * </p>
 * <p>
 * Data: Jul 21, 2023
 * </p>
 * 
 * @author Renzo Poli
 */
public abstract class BaseServiceApplication {
	private static Logger LOGGER = LoggerFactory.getLogger(BaseServiceApplication.class);

	/**
	 * In OpenAPI info, when this value is true, the snapshot mave suffix will be stripped
	 * <p>
	 * Default value: false
	 */
	@Value("${goffice.common.openapi.info.version.removeSnapshotSuffix:false}")
	private boolean openapiRemoveSnapshotSuffix;

	/**
	 * In OpenAPI info, this represent the server url
	 * <p>
	 * Default value: none
	 */
	@Value("${goffice.common.openapi.server.url:#{null}}")
	private String openapiServerUrl;

	/**
	 * In OpenAPI info, this represent the server description
	 * <p>
	 * Default value: goffice2
	 */
	@Value("${goffice.common.openapi.server.description:goffice2}")
	private String openapiServerDescription;

	/**
	 * In OpenAPI info, this represent the license name
	 * <p>
	 * Default value: none
	 */
	@Value("${goffice.common.openapi.info.license.name:#{null}}")
	private String openapiInfoLicenseName;

	/**
	 * In OpenAPI info, this represent the license url
	 * <p>
	 * Default value: none
	 */
	@Value("${goffice.common.openapi.info.license.url:#{null}}")
	private String openapiInfoLicenseUrl;

	/**
	 * Returns the maven build properties, as injected by the SprintBoot framework
	 * 
	 * @return An instance of the maven build properties
	 */
	protected abstract BuildProperties getBuildProperties();

	/**
	 * Method which customize the OpenAPI info
	 * 
	 * @return
	 */
	@Bean
	OpenAPI customizeOpenAPI() {
		LOGGER.debug("customizeOpenAPI - START");

		BuildProperties buildProperties = getBuildProperties();

		String infoTitle = buildProperties.getName();
		if (StringUtils.isBlank(infoTitle)) {
			LOGGER.warn("customizeOpenAPI *** NO NAME WAS SPECIFIED in pom.xml *** Please, write a meaningful value in <name> tag!");
			infoTitle = buildProperties.getArtifact();
		}

		String infoDescription = String.format("%s - API Swagger documentation", infoTitle);
		String infoVersion = buildProperties.getVersion();
		if (openapiRemoveSnapshotSuffix && infoVersion != null) {
			infoVersion = infoVersion.replaceFirst("-SNAPSHOT", "");
		}

		LOGGER.debug("customizeOpenAPI - title:...............{}", infoTitle);
		LOGGER.debug("customizeOpenAPI - description:.........{}", infoDescription);
		LOGGER.debug("customizeOpenAPI - version:.............{}", infoVersion);

		OpenAPI openAPI = new OpenAPI();

		Info info = new Info() //
				.title(infoTitle) //
				.version(infoVersion) //
				.description(infoDescription);
		openAPI.info(info);

		// license info
		if (StringUtils.isNotBlank(openapiInfoLicenseName)) {
			LOGGER.debug("customizeOpenAPI - license name:........{}", openapiInfoLicenseName);
			License license = new License() //
					.name(openapiInfoLicenseName);

			if (StringUtils.isNotBlank(openapiInfoLicenseUrl)) {
				LOGGER.debug("customizeOpenAPI - license url:.........{}", openapiInfoLicenseUrl);
				license.url(openapiInfoLicenseUrl);
			}

			info.license(license);
		}

		// server info
		if (StringUtils.isNotBlank(openapiServerUrl)) {
			LOGGER.debug("customizeOpenAPI - server url:..........{}", openapiServerUrl);
			LOGGER.debug("customizeOpenAPI - server description:..{}", openapiServerDescription);
			Server server = new Server() //
					.url(openapiServerUrl) //
					.description(openapiServerDescription);
			openAPI.servers(Collections.singletonList(server));
		}

		LOGGER.debug("customizeOpenAPI - END");

		return openAPI;
	}
}
