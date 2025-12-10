package com.spectralink.aimwright.injection;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spectralink.aimwright.api.AmieApiClient;
import com.spectralink.aimwright.common.Defaults;
import com.spectralink.aimwright.common.Session;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

import static com.spectralink.aimwright.api.ApiClient.Method.GET;
import static com.spectralink.aimwright.injection.SimulatedPhone.Model.*;

/**
 * Simulates a Spectralink phone device for test data injection.
 * Generates device, network, battery, and call metrics via MQTT.
 */
public class SimulatedPhone {

    public enum Model {
        VERSITY_9540("Versity ", "9540", 95),
        VERSITY_9553("Versity ", "9553", 95),
        VERSITY_9640("Versity ", "9640", 95),
        VERSITY_9653("Versity ", "9653", 95),
        VERSITY_9740("Versity ", "9740", 97),
        VERSITY_9753("Versity ", "9753", 97),
        ORION_9240("VC", "9240", 92),
        ORION_9253("VC", "9253", 92);

        final String platform;
        final String model;
        final Integer series;

        Model(String platform, String model, Integer series) {
            this.platform = platform;
            this.model = model;
            this.series = series;
        }

        public String model() {
            return this.model;
        }

        public String fullModel() {
            return this.platform + this.model;
        }

        public Integer series() {
            return this.series;
        }
    }

    public enum CpuUtilizationDriftType {
        INCREASING(0.1, 100.0),
        DECREASING(0.1, 100.0),
        STABLE(40.25, 40.25);

        final Double lowerLimit;
        final Double upperLimit;

        CpuUtilizationDriftType(Double lowerLimit, Double upperLimit) {
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
        }

        public Double lowerLimit() {
            return this.lowerLimit;
        }

        public Double upperLimit() {
            return this.upperLimit;
        }
    }

    public enum RamUtilizationDriftType {
        INCREASING(20, 100),
        DECREASING(20, 100),
        STABLE(56, 56);

        final Integer lowerLimit;
        final Integer upperLimit;

        RamUtilizationDriftType(Integer lowerLimit, Integer upperLimit) {
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
        }

        public Integer lowerLimit() {
            return this.lowerLimit;
        }

        public Integer upperLimit() {
            return this.upperLimit;
        }
    }

    public enum MetricsType {
        ALL,
        DEVICE,
        BATTERY,
        NETWORK,
        CALL,
        IN_USE,
        WIFI_SCAN,
        BLUETOOTH_SCAN,
        IP_ADDRESS,
        BARCODE_SCAN
    }

    public enum ConnectionState {
        CONNECTED("connected"),
        DISCONNECTED("disconnected");

        final String state;

        ConnectionState(String state) {
            this.state = state;
        }

        public String state() {
            return this.state;
        }
    }

    public enum CallDirection {
        OUTGOING("CALL_STATE_CALLING", "outgoing"),
        INCOMING("CALL_STATE_INCOMING", "incoming");

        final String event;
        final String direction;

        CallDirection(String event, String direction) {
            this.event = event;
            this.direction = direction;
        }

        public String event() {
            return this.event;
        }

        public String direction() {
            return this.direction;
        }
    }

    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ZoneId zoneId = ZoneId.systemDefault();

    private ArrayNode buffer = mapper.createArrayNode();
    private SimulatedAP current_ap;
    private SimulatedBattery current_battery;
    private SimulatedCall current_call;
    private Model designated_model = VERSITY_9740;
    private String device_os_revision = "13";
    private String device_name = "Spectralink cnnc03bp5pf2169";
    private String device_sw_revision = "13.3.0.1576-user";
    private String device_info_1 = null;
    private String device_info_2 = null;
    private String device_info_3 = null;
    private String device_info_4 = null;
    private String device_model = VERSITY_9740.fullModel();
    private Map<String, String> app_versions = new HashMap<>();
    private String imei = "03dcfeefcb581a3c";
    private List<Map<String, String>> device_mac_addresses = new ArrayList<>();
    private String device_serial_number = "cnnc03bp5pf2169";
    private Double cpu_utilization_pct_last_1 = 0.55;
    private Double cpu_utilization_pct = 0.5325;
    private CpuUtilizationDriftType cpu_utilization_CONTROL = CpuUtilizationDriftType.STABLE;
    private Double cpu_utilization_INCREMENT = 4.0;
    private Double cpu_utilization_pct_last_15 = 0.51125;
    private Integer ram_utilization_pct = 41;
    private RamUtilizationDriftType ram_utilization_pct_CONTROL = RamUtilizationDriftType.STABLE;
    private Integer ram_utilization_pct_INCREMENT = 5;
    private Long total_storage_bytes = 101824802816L;
    private Long used_storage_bytes = 7520411648L;
    private String ip_address = "192.168.5.10";
    private String mac_address = "00:90:7a:b7:23:46";
    private ConnectionState connection_status = ConnectionState.DISCONNECTED;
    private String designated_battery;
    private String designated_ap;
    private Integer packetCount = 250;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    private static String endpoint = "/api/administration/locations/gateway-summary?start=0&length=50&sortField=gateway_name&sortOrder=ASC";

    public SimulatedPhone() {
        if (getCurrent_ap() != null) {
            setConnection_status(ConnectionState.CONNECTED);
        }
    }

    /**
     * Gets the gateway IP address for a location via API.
     */
    public static String locationIpAddress(String locationName) {
        locationName = Defaults.getLocationName();
        String result = "";
        AmieApiClient apiClient = Session.getAmieApiClient();
        ObjectNode bodyData = apiClient.sendRequest(GET, endpoint).getJsonObjectBody();
        ArrayNode gwData = (ArrayNode) bodyData.get("data");

        Assert.assertFalse(bodyData.isEmpty(), "no data present in the body");
        for (JsonNode node : gwData) {
            if (node.get("tenant_name").asText().contains(locationName)) {
                result = String.valueOf(node.get("gateway_ip_address"));
            }
        }
        return result.substring(1, result.length() - 1);
    }

    public SimulatedPhone(Model designatedModel) {
        designated_model = designatedModel;
        device_model = designated_model.fullModel();
        ip_address = getRandomIpAddress();
        mac_address = getRandomSpectralinkMacAddress();
        if (getCurrent_ap() != null) {
            setConnection_status(ConnectionState.CONNECTED);
        }
    }

    @SuppressWarnings("unchecked")
    public SimulatedPhone(Model designatedModel, Map<String, Object> settings) {
        designated_model = designatedModel;
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            switch (entry.getKey()) {
                case "current_ap":
                    current_ap = (SimulatedAP) entry.getValue();
                    break;
                case "current_battery":
                    current_battery = (SimulatedBattery) entry.getValue();
                    break;
                case "device_os_revision":
                    device_os_revision = (String) entry.getValue();
                    break;
                case "device_name":
                    device_name = (String) entry.getValue();
                    break;
                case "device_sw_revision":
                    device_sw_revision = (String) entry.getValue();
                    break;
                case "device_info_1":
                    device_info_1 = (String) entry.getValue();
                    break;
                case "device_info_2":
                    device_info_2 = (String) entry.getValue();
                    break;
                case "device_info_3":
                    device_info_3 = (String) entry.getValue();
                    break;
                case "device_info_4":
                    device_info_4 = (String) entry.getValue();
                    break;
                case "app_versions":
                    app_versions = (Map<String, String>) entry.getValue();
                    break;
                case "imei":
                    imei = (String) entry.getValue();
                    break;
                case "device_mac_addresses":
                    device_mac_addresses = (List<Map<String, String>>) entry.getValue();
                    break;
                case "device_serial_number":
                    device_serial_number = (String) entry.getValue();
                    break;
                case "cpu_utilization_pct_last_1":
                    cpu_utilization_pct_last_1 = (Double) entry.getValue();
                    break;
                case "cpu_utilization_pct":
                    cpu_utilization_pct = (Double) entry.getValue();
                    break;
                case "cpu_utilization_CONTROL":
                    cpu_utilization_CONTROL = (CpuUtilizationDriftType) entry.getValue();
                    break;
                case "cpu_utilization_INCREMENT":
                    cpu_utilization_INCREMENT = (Double) entry.getValue();
                    break;
                case "cpu_utilization_pct_last_15":
                    cpu_utilization_pct_last_15 = (Double) entry.getValue();
                    break;
                case "ram_utilization_pct":
                    ram_utilization_pct = (Integer) entry.getValue();
                    break;
                case "ram_utilization_pct_CONTROL":
                    ram_utilization_pct_CONTROL = (RamUtilizationDriftType) entry.getValue();
                    break;
                case "ram_utilization_pct_INCREMENT":
                    ram_utilization_pct_INCREMENT = (Integer) entry.getValue();
                    break;
                case "total_storage_bytes":
                    total_storage_bytes = (Long) entry.getValue();
                    break;
                case "used_storage_bytes":
                    used_storage_bytes = (Long) entry.getValue();
                    break;
                case "ip_address":
                    ip_address = (String) entry.getValue();
                    break;
                case "mac_address":
                    mac_address = (String) entry.getValue();
                    break;
                default:
                    log.error("Invalid key sent to SimulatedPhone: {}", entry.getKey());
                    break;
            }
        }
        device_model = designated_model.fullModel();
        if (ip_address == null) ip_address = getRandomIpAddress();
        if (mac_address == null) mac_address = getRandomSpectralinkMacAddress();
    }

    // Getters and Setters
    public ArrayNode getBuffer() {
        return buffer;
    }

    public void clearBuffer() {
        buffer = mapper.createArrayNode();
    }

    public SimulatedAP getCurrent_ap() {
        return current_ap;
    }

    public void setCurrent_ap(SimulatedAP current_ap) {
        this.current_ap = current_ap;
    }

    public SimulatedBattery getCurrent_battery() {
        return current_battery;
    }

    public void setCurrent_battery(SimulatedBattery current_battery) {
        this.current_battery = current_battery;
    }

    public SimulatedCall getCurrent_call() {
        return current_call;
    }

    public void setCurrent_call(SimulatedCall current_call) {
        this.current_call = current_call;
    }

    public Model getDesignated_model() {
        return designated_model;
    }

    public void setDesignated_model(Model designated_model) {
        this.designated_model = designated_model;
    }

    public String getDevice_os_revision() {
        return device_os_revision;
    }

    public void setDevice_os_revision(String device_os_revision) {
        this.device_os_revision = device_os_revision;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public String getDevice_sw_revision() {
        return device_sw_revision;
    }

    public void setDevice_sw_revision(String device_sw_revision) {
        this.device_sw_revision = device_sw_revision;
    }

    public String getDevice_info_1() {
        return device_info_1;
    }

    public void setDevice_info_1(String device_info_1) {
        this.device_info_1 = device_info_1;
    }

    public String getDevice_info_2() {
        return device_info_2;
    }

    public void setDevice_info_2(String device_info_2) {
        this.device_info_2 = device_info_2;
    }

    public String getDevice_info_3() {
        return device_info_3;
    }

    public void setDevice_info_3(String device_info_3) {
        this.device_info_3 = device_info_3;
    }

    public String getDevice_info_4() {
        return device_info_4;
    }

    public void setDevice_info_4(String device_info_4) {
        this.device_info_4 = device_info_4;
    }

    public String getDevice_model() {
        return device_model;
    }

    public void setDevice_model(String device_model) {
        this.device_model = device_model;
    }

    public Map<String, String> getApp_versions() {
        return app_versions;
    }

    public void setApp_versions(Map<String, String> app_versions) {
        this.app_versions = app_versions;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public List<Map<String, String>> getDevice_mac_addresses() {
        return device_mac_addresses;
    }

    public void setDevice_mac_addresses(List<Map<String, String>> device_mac_addresses) {
        this.device_mac_addresses = device_mac_addresses;
    }

    public String getDevice_serial_number() {
        return device_serial_number;
    }

    public void setDevice_serial_number(String device_serial_number) {
        this.device_serial_number = device_serial_number;
    }

    public Double getCpu_utilization_pct_last_1() {
        return cpu_utilization_pct_last_1;
    }

    public void setCpu_utilization_pct_last_1(Double cpu_utilization_pct_last_1) {
        this.cpu_utilization_pct_last_1 = cpu_utilization_pct_last_1;
    }

    public Double getCpu_utilization_pct() {
        if (getCpu_utilization_CONTROL().equals(CpuUtilizationDriftType.INCREASING)) {
            cpu_utilization_pct_last_1 = cpu_utilization_pct;
            if (cpu_utilization_pct + getCpu_utilization_INCREMENT() > getCpu_utilization_CONTROL().upperLimit()) {
                cpu_utilization_pct = getCpu_utilization_CONTROL().upperLimit();
            } else {
                cpu_utilization_pct += getCpu_utilization_INCREMENT();
            }
        } else if (getRam_utilization_pct_CONTROL().equals(RamUtilizationDriftType.DECREASING)) {
            cpu_utilization_pct_last_1 = cpu_utilization_pct;
            if (cpu_utilization_pct - getCpu_utilization_INCREMENT() < getCpu_utilization_CONTROL().lowerLimit()) {
                cpu_utilization_pct = getCpu_utilization_CONTROL().lowerLimit();
            } else {
                cpu_utilization_pct -= getCpu_utilization_INCREMENT();
            }
        }
        return cpu_utilization_pct;
    }

    public void setCpu_utilization_pct(Double cpu_utilization_pct) {
        cpu_utilization_pct_last_1 = this.cpu_utilization_pct;
        this.cpu_utilization_pct = cpu_utilization_pct;
    }

    public CpuUtilizationDriftType getCpu_utilization_CONTROL() {
        return cpu_utilization_CONTROL;
    }

    public void setCpu_utilization_CONTROL(CpuUtilizationDriftType cpu_utilization_CONTROL) {
        this.cpu_utilization_CONTROL = cpu_utilization_CONTROL;
    }

    public Double getCpu_utilization_INCREMENT() {
        return cpu_utilization_INCREMENT;
    }

    public void setCpu_utilization_INCREMENT(Double cpu_utilization_INCREMENT) {
        this.cpu_utilization_INCREMENT = cpu_utilization_INCREMENT;
    }

    public Double getCpu_utilization_pct_last_15() {
        return cpu_utilization_pct_last_15;
    }

    public void setCpu_utilization_pct_last_15(Double cpu_utilization_pct_last_15) {
        this.cpu_utilization_pct_last_15 = cpu_utilization_pct_last_15;
    }

    public Integer getRam_utilization_pct() {
        if (getRam_utilization_pct_CONTROL().equals(RamUtilizationDriftType.INCREASING)) {
            if (ram_utilization_pct + getRam_utilization_pct_INCREMENT() > getRam_utilization_pct_CONTROL().upperLimit()) {
                ram_utilization_pct = getRam_utilization_pct_CONTROL().upperLimit();
            } else {
                ram_utilization_pct += getRam_utilization_pct_INCREMENT();
            }
        } else if (getRam_utilization_pct_CONTROL().equals(RamUtilizationDriftType.DECREASING)) {
            if (ram_utilization_pct - getRam_utilization_pct_INCREMENT() < getRam_utilization_pct_CONTROL().lowerLimit()) {
                ram_utilization_pct = getRam_utilization_pct_CONTROL().lowerLimit();
            } else {
                ram_utilization_pct -= getRam_utilization_pct_INCREMENT();
            }
        }
        return ram_utilization_pct;
    }

    public void setRam_utilization_pct(Integer ram_utilization_pct) {
        this.ram_utilization_pct = ram_utilization_pct;
    }

    public RamUtilizationDriftType getRam_utilization_pct_CONTROL() {
        return ram_utilization_pct_CONTROL;
    }

    public void setRam_utilization_pct_CONTROL(RamUtilizationDriftType ram_utilization_pct_CONTROL) {
        this.ram_utilization_pct_CONTROL = ram_utilization_pct_CONTROL;
    }

    public Integer getRam_utilization_pct_INCREMENT() {
        return ram_utilization_pct_INCREMENT;
    }

    public void setRam_utilization_pct_INCREMENT(Integer ram_utilization_pct_INCREMENT) {
        this.ram_utilization_pct_INCREMENT = ram_utilization_pct_INCREMENT;
    }

    public Long getTotal_storage_bytes() {
        return total_storage_bytes;
    }

    public void setTotal_storage_bytes(Long total_storage_bytes) {
        this.total_storage_bytes = total_storage_bytes;
    }

    public Long getUsed_storage_bytes() {
        return used_storage_bytes;
    }

    public void setUsed_storage_bytes(Long used_storage_bytes) {
        this.used_storage_bytes = used_storage_bytes;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public ConnectionState getConnection_status() {
        return connection_status;
    }

    public void setConnection_status(ConnectionState connection_status) {
        this.connection_status = connection_status;
    }

    public String getDesignated_battery() {
        return designated_battery;
    }

    public void setDesignated_battery(String designated_battery) {
        this.designated_battery = designated_battery;
    }

    public String getDesignated_ap() {
        return designated_ap;
    }

    public void setDesignated_ap(String designated_ap) {
        this.designated_ap = designated_ap;
    }

    // WiFi and Battery operations
    public void disconnectWifi() {
        setConnection_status(ConnectionState.DISCONNECTED);
    }

    public void connectWifi(SimulatedAP ap) {
        if (ap != null) {
            setConnection_status(ConnectionState.CONNECTED);
            setCurrent_ap(ap);
            ap.setHandOff(true);
            ap.setConnection_state(ConnectionState.CONNECTED);
        } else {
            log.error("No AP connected");
        }
    }

    public void insertBattery(SimulatedBattery battery) {
        if (battery != null) {
            battery.setHousingPhone(this);
            setCurrent_battery(battery);
        } else {
            log.error("No battery inserted");
        }
    }

    public void startCall(CallDirection direction) {
        if (getCurrent_ap() != null) {
            SimulatedCall newCall = new SimulatedCall(this);
            newCall.setCall_type(direction);
            setCurrent_call(newCall);
        } else {
            log.error("Cannot start a call without an AP connected");
        }
    }

    // Helper methods for random data generation
    private String getRandomIpAddress() {
        StringBuilder newIpAddress = new StringBuilder();
        newIpAddress.append("192.168.");
        Random generator = new Random();
        int thisOctet = generator.nextInt(255);
        newIpAddress.append(String.format("%02d.", thisOctet));
        thisOctet = generator.nextInt(255);
        newIpAddress.append(String.format("%02d", thisOctet));
        return newIpAddress.toString();
    }

    private String getRandomMacAddress() {
        StringBuilder newMacAddress = new StringBuilder();
        Random generator = new Random();
        for (int pair = 0; pair < 6; pair++) {
            int thisPair = generator.nextInt(255);
            newMacAddress.append(pair == 5 ? String.format("%02x", thisPair) : String.format("%02x:", thisPair));
        }
        return newMacAddress.toString().toUpperCase();
    }

    private String getRandomSpectralinkMacAddress() {
        StringBuilder newMacAddress = new StringBuilder();
        newMacAddress.append("00:90:7a:");
        Random generator = new Random();
        for (int pair = 0; pair < 3; pair++) {
            int thisPair = generator.nextInt(255);
            newMacAddress.append(pair == 2 ? String.format("%02x", thisPair) : String.format("%02x:", thisPair));
        }
        return newMacAddress.toString().toUpperCase();
    }

    public ArrayNode getDeviceMacAddresses() {
        ArrayNode interfaceList = mapper.createArrayNode();
        if (getDesignated_model().series() != 97) {
            ObjectNode interface9295 = mapper.createObjectNode();
            interface9295.put("interface", "dummy0");
            interface9295.put("address", getRandomMacAddress());
            interfaceList.add(interface9295);
        }
        if (getDesignated_model().series() == 95) {
            ObjectNode interface95 = mapper.createObjectNode();
            interface95.put("address", getRandomMacAddress());
            interface95.put("interface", "p2p0");
            interfaceList.add(interface95);

            interface95 = mapper.createObjectNode();
            interface95.put("address", getRandomMacAddress());
            interface95.put("interface", "bond0");
            interfaceList.add(interface95);

            interface95 = mapper.createObjectNode();
            interface95.put("address", getRandomMacAddress());
            interface95.put("interface", "wifi-aware0");
            interfaceList.add(interface95);
        }
        ObjectNode interfaceAll = mapper.createObjectNode();
        interfaceAll.put("address", getMac_address());
        interfaceAll.put("interface", "wlan0");
        interfaceList.add(interfaceAll);
        return interfaceList;
    }

    public ObjectNode getAppVersions() {
        Path appVersionsPath;
        ObjectNode appVersions = null;
        if (getApp_versions().isEmpty()) {
            if (getDesignated_model().series() == 95) {
                appVersionsPath = Paths.get(Environment.getProjectDirectory(), "src", "main", "resources", "test_data", "injection_data", "app_versions_versity.json");
            } else if (getDesignated_model().series() == 97) {
                appVersionsPath = Paths.get(Environment.getProjectDirectory(), "src", "main", "resources", "test_data", "injection_data", "app_versions_x1.json");
            } else {
                appVersionsPath = Paths.get(Environment.getProjectDirectory(), "src", "main", "resources", "test_data", "injection_data", "app_versions_orion.json");
            }
            File appVersionsFile = appVersionsPath.toFile();
            try {
                appVersions = (ObjectNode) mapper.readTree(appVersionsFile);
            } catch (IOException ioe) {
                log.error("Could not load app versions file {}: {}", appVersionsPath, ioe.getMessage());
            }
        } else {
            appVersions = mapper.valueToTree(getApp_versions());
        }
        return appVersions;
    }

    // Metrics generation methods
    public ObjectNode getDeviceMetrics(long timestamp) {
        ObjectNode metrics = mapper.createObjectNode();
        metrics.put("device_os_revision", getDevice_os_revision());
        metrics.put("device_name", getDevice_name());
        metrics.put("device_sw_revision", getDevice_sw_revision());
        metrics.put("device_info_1", getDevice_info_1());
        metrics.put("device_info_2", getDevice_info_2());
        metrics.put("device_info_3", getDevice_info_3());
        metrics.put("device_info_4", getDevice_info_4());
        metrics.put("device_model", getDevice_model());
        metrics.set("app_versions", getAppVersions());
        if (getDesignated_model().equals(VERSITY_9653) || getDesignated_model().equals(VERSITY_9753)) {
            metrics.put("imei", getImei());
        }
        metrics.set("device_mac_addresses", getDeviceMacAddresses());
        metrics.put("device_serial_number", getDevice_serial_number());
        metrics.put("cpu_utilization_pct_last_1", getCpu_utilization_pct_last_1());
        metrics.put("cpu_utilization_pct", getCpu_utilization_pct());
        metrics.put("cpu_utilization_pct_last_15", getCpu_utilization_pct_last_15());
        metrics.put("ram_utilization_pct", getRam_utilization_pct());
        metrics.put("total_storage_bytes", getTotal_storage_bytes());
        metrics.put("used_storage_bytes", getUsed_storage_bytes());
        metrics.put("timestamp", timestamp);
        metrics.put("type", "DEVICE_METRICS");
        return metrics;
    }

    public ObjectNode getNetworkMetrics(long timestamp) {
        if (getCurrent_ap() != null) {
            ArrayNode networks = mapper.createArrayNode();
            if (getConnection_status().equals(ConnectionState.CONNECTED)) {
                for (Map.Entry<String, SimulatedAP> entry : Environment.getSimNetworks().entrySet()) {
                    if (!entry.getKey().contentEquals(getCurrent_ap().getAp_ssid())) {
                        networks.add(entry.getValue().getCandidateAP());
                    }
                }
            }
            return getCurrent_ap().getSelectedAP(networks, timestamp);
        } else {
            log.error("No network connected to phone {}", getDevice_serial_number());
            return null;
        }
    }

    public ObjectNode getDeviceIpAddress(long timestamp) {
        ObjectNode metrics = mapper.createObjectNode();
        metrics.put("device_ip_address", getIp_address());
        metrics.put("timestamp", timestamp);
        metrics.put("type", "DEVICE_METRICS");
        return metrics;
    }

    public ObjectNode getBarcodeData(long timestamp) {
        ObjectNode outsideMetrics = mapper.createObjectNode();
        ObjectNode insideMetrics = mapper.createObjectNode();
        insideMetrics.put("length", 12);
        insideMetrics.put("aim_code", "UPC-A");
        insideMetrics.put("decode_status", "success");
        outsideMetrics.set("barcode", insideMetrics);
        outsideMetrics.put("type", "DEVICE_METRICS");
        outsideMetrics.put("timestamp", timestamp);
        return outsideMetrics;
    }

    /**
     * Sends buffered metrics to the MQTT broker.
     */
    public void sendBufferedMetrics() {
        ObjectNode fullMqttMessage = mapper.createObjectNode();
        if (!getBuffer().isEmpty()) {
            fullMqttMessage.put("deviceSerial", getDevice_serial_number());
            fullMqttMessage.put("timestamp", System.currentTimeMillis());
            fullMqttMessage.set("data", getBuffer());
            log.debug("Sending {} updates to MQTT broker from buffer", getBuffer().size());
            if (Defaults.isDebugMode()) {
                log.debug(fullMqttMessage.toPrettyString());
            } else {
                MqttAgent agent = new MqttAgent(getDevice_serial_number(), null);
                agent.connect();
                agent.sendMessage(fullMqttMessage);
                agent.disconnect();
            }
        } else {
            log.error("No events were in the buffer - no metrics sent");
        }
    }

    @Override
    public String toString() {
        String summary = "";
        summary += getCurrent_ap() == null ? "current_ap: none \n" : "current_ap: " + current_ap.getAp_ssid() + "\n";
        summary += getCurrent_battery() == null ? "current_battery: none \n" : "current_battery: " + current_battery.getBattery_serial_num() + "\n";
        summary += "designated_model: " + designated_model + "\n" +
                "device_serial_number: " + device_serial_number + "\n" +
                "device_model: " + device_model + "\n" +
                "ip_address: " + ip_address + "\n" +
                "mac_address: " + mac_address + "\n" +
                "connection_status: " + connection_status;
        return summary;
    }
}
