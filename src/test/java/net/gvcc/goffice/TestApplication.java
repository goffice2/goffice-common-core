package net.gvcc.goffice;

import java.util.Properties;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class TestApplication extends BaseServiceApplication {
	public static final String ARTIFACT = "test-artifact-name";
	public static final String VERSION = "test-artifact-version";
	public static final String NAME = "test-name";

	private static Properties properties = new Properties();
	static {
		properties.setProperty("artifact", ARTIFACT);
		properties.setProperty("version", VERSION);
	}

	@Override
	protected BuildProperties getBuildProperties() {
		return new BuildProperties(properties);
	}

	public void clear() {
		properties.clear();
	}

	public void setName(String value) {
		properties.setProperty("name", value);
	}
}
