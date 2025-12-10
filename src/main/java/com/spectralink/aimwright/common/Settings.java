package com.spectralink.aimwright.common;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {

    private static Properties properties;
    private static String settingsPath = "src/main/resources/settings.properties";
    private static final Logger log = (Logger) LoggerFactory.getLogger(Settings.class.getName());

    public static void loadAll() {
        loadAll(settingsPath);
    }

    public static void loadAll(String filePath) {
        properties = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
            log.info("Loaded run settings file at {}", settingsPath);
        } catch (IOException ioe) {
            log.error("Could not load settings file {}: {}", settingsPath, ioe.getMessage());
        }
    }

    public static String get(String key) {
        if (properties == null) {
            loadAll();
            if (properties == null) return null;
        }
        return properties.getProperty(key);
    }
}