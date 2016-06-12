package org.echocat.gradle.plugins.golang;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class Version {

    private static final Properties PROPERTIES = load();

    public static final String VERSION = PROPERTIES.getProperty("version", "unknown");
    public static final String GROUP_ID = PROPERTIES.getProperty("groupId", Version.class.getPackage().getName());
    public static final String ARTIFACT_ID = PROPERTIES.getProperty("artifactId", "unknown");
    public static final String NAME = PROPERTIES.getProperty("name", Version.class.getPackage().getName());
    public static final String DESCIPTION = PROPERTIES.getProperty("description", "");
    public static final String URL = PROPERTIES.getProperty("url", "");

    private static Properties load() {
        final Properties result = new Properties();
        final InputStream is = Version.class.getClassLoader().getResourceAsStream("org/echocat/gradle/plugins/golang/version.properties");
        if (is != null) {
            try (final Reader reader = new InputStreamReader(is, "UTF-8")) {
                result.load(reader);
            } catch (final IOException e) {
                throw new RuntimeException("Could not read version.properties.", e);
            } finally {
                closeQuietly(is);
            }
        }
        return result;
    }

}
