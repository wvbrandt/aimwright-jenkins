package com.spectralink.aimwright.injection;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the simulated test environment including phones, batteries, and access points.
 * Provides singleton access to simulated device collections.
 */
public class Environment {
    private static Environment myself;
    private static final Logger log = (Logger) LoggerFactory.getLogger(Environment.class.getName());
    private static final String projectDirectory = System.getProperty("user.dir");
    private static final String testDataDirectory = projectDirectory + "/src/main/resources/test_data";
    private static final Map<String, SimulatedPhone> simPhones = new HashMap<>();
    private static final Map<String, SimulatedBattery> simBatteries = new HashMap<>();
    private static final Map<String, SimulatedAP> simNetworks = new HashMap<>();
    private static final Map<String, Object> temporaryStorage = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Environment getInstance() {
        if (myself == null) {
            myself = new Environment();
        }
        return myself;
    }

    /**
     * Loads access points from JSON configuration file.
     */
    public static void loadAPs() {
        Path apData = Paths.get(testDataDirectory, "injection_data", "ap_defaults.json");
        try {
            List<SimulatedAP> apList = objectMapper.readValue(apData.toFile(), new TypeReference<>() {});
            simNetworks.clear();
            for (SimulatedAP eachAp : apList) {
                simNetworks.put(eachAp.getAp_ssid(), eachAp);
                log.trace("Loaded AP '{}' into Environment", eachAp.getAp_ssid());
            }
        } catch (IOException ioe) {
            log.error("Could not load file in the path {}: {}", apData, ioe.getMessage());
        }
    }

    public static List<String> getAPList() {
        return new ArrayList<>(simNetworks.keySet());
    }

    /**
     * Loads batteries from JSON configuration file.
     */
    public static void loadBatteries() {
        Path apData = Paths.get(testDataDirectory, "injection_data", "battery_defaults.json");
        try {
            List<SimulatedBattery> batteryList = objectMapper.readValue(apData.toFile(), new TypeReference<>() {});
            simBatteries.clear();
            for (SimulatedBattery eachBattery : batteryList) {
                simBatteries.put(eachBattery.getBattery_serial_num(), eachBattery);
                log.trace("Loaded battery {} into Environment", eachBattery.getBattery_serial_num());
            }
        } catch (IOException ioe) {
            log.error("Could not load file in the path {}: {}", apData, ioe.getMessage());
        }
    }

    public static List<String> getBatteryList() {
        return new ArrayList<>(simBatteries.keySet());
    }

    /**
     * Loads devices from JSON configuration file.
     */
    public static void loadDevices() {
        Path apData = Paths.get(testDataDirectory, "injection_data", "device_defaults.json");
        try {
            List<SimulatedPhone> phoneList = objectMapper.readValue(apData.toFile(), new TypeReference<>() {});
            simPhones.clear();
            for (SimulatedPhone eachPhone : phoneList) {
                simPhones.put(eachPhone.getDevice_serial_number(), eachPhone);
                log.info("Loaded device {} into Environment", eachPhone.getDevice_serial_number());
                if (getAPList().contains(eachPhone.getDesignated_ap())) {
                    eachPhone.connectWifi(simNetworks.get(eachPhone.getDesignated_ap()));
                    log.debug("Connected {} to AP SSID '{}'", eachPhone.getDevice_serial_number(), eachPhone.getDesignated_ap());
                } else {
                    log.warn("No AP was available for device {}", eachPhone.getDevice_serial_number());
                }

                if (getBatteryList().contains(eachPhone.getDesignated_battery())) {
                    eachPhone.insertBattery(simBatteries.get(eachPhone.getDesignated_battery()));
                    log.debug("Inserted battery {} into {}", eachPhone.getDesignated_battery(), eachPhone.getDevice_serial_number());
                } else {
                    log.warn("No battery was available for device {}", eachPhone.getDevice_serial_number());
                }
            }
        } catch (IOException ioe) {
            log.error("Could not load file in the path {}: {}", apData, ioe.getMessage());
        }
    }

    /**
     * Reloads a specific device from JSON configuration.
     */
    public static void reloadDevice(String serial) {
        Path apData = Paths.get(testDataDirectory, "injection_data", "device_defaults.json");
        try {
            List<SimulatedPhone> phoneList = objectMapper.readValue(apData.toFile(), new TypeReference<>() {});
            for (SimulatedPhone eachPhone : phoneList) {
                if (eachPhone.getDevice_serial_number().contentEquals(serial)) {
                    simPhones.put(eachPhone.getDevice_serial_number(), eachPhone);
                    log.info("Reloaded device {} into Environment", eachPhone.getDevice_serial_number());
                    if (getAPList().contains(eachPhone.getDesignated_ap())) {
                        eachPhone.connectWifi(simNetworks.get(eachPhone.getDesignated_ap()));
                        log.debug("Connected {} to AP SSID '{}'", eachPhone.getDevice_serial_number(), eachPhone.getDesignated_ap());
                    } else {
                        log.warn("No AP was available for device {}", eachPhone.getDevice_serial_number());
                    }

                    if (getBatteryList().contains(eachPhone.getDesignated_battery())) {
                        eachPhone.insertBattery(simBatteries.get(eachPhone.getDesignated_battery()));
                        log.debug("Inserted battery {} into {}", eachPhone.getDesignated_battery(), eachPhone.getDevice_serial_number());
                    } else {
                        log.warn("No battery was available for device {}", eachPhone.getDevice_serial_number());
                    }
                }
            }
        } catch (IOException ioe) {
            log.error("Could not load file in the path {}: {}", apData, ioe.getMessage());
        }
    }

    public static List<String> getDeviceList() {
        return new ArrayList<>(simPhones.keySet());
    }

    public static String getProjectDirectory() {
        return projectDirectory;
    }

    public static String getTestDataDirectory() {
        return testDataDirectory;
    }

    public static SimulatedPhone getSimPhone(String serial) {
        if (simPhones.containsKey(serial)) {
            return simPhones.get(serial);
        } else {
            log.error("No phone present with the serial number {}", serial);
            return null;
        }
    }

    public static Map<String, SimulatedPhone> getSimPhones() {
        return simPhones;
    }

    public static void setSimPhone(String token, SimulatedPhone phone) {
        simPhones.put(token, phone);
    }

    public static void removeSimPhone(String token) {
        simPhones.remove(token);
    }

    public static SimulatedBattery getSimBattery(String serial) {
        if (simBatteries.containsKey(serial)) {
            return simBatteries.get(serial);
        } else {
            log.error("No battery present with the serial number {}", serial);
            return null;
        }
    }

    public static Map<String, SimulatedBattery> getSimBatteries() {
        return simBatteries;
    }

    public static void setSimBattery(String token, SimulatedBattery battery) {
        simBatteries.put(token, battery);
    }

    public static void removeSimBattery(String token) {
        simBatteries.remove(token);
    }

    public static SimulatedAP getSimNetwork(String ssid) {
        if (simNetworks.containsKey(ssid)) {
            return simNetworks.get(ssid);
        } else {
            log.error("No AP present with the SSID {}", ssid);
            return null;
        }
    }

    public static Map<String, SimulatedAP> getSimNetworks() {
        return simNetworks;
    }

    public static void setSimNetwork(String bssid, SimulatedAP newAP) {
        simNetworks.put(bssid, newAP);
    }

    public static void removeSimNetwork(String ssid) {
        simNetworks.remove(ssid);
    }
}
