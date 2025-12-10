package com.spectralink.aimwright.injection;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simulates a device battery for test data injection.
 * Generates battery metrics including charge state, level, temperature, and health.
 */
public class SimulatedBattery {

    public enum LevelDriftType {
        INCREASING(1, 100),
        DECREASING(1, 100),
        STABLE(60, 60);

        final Integer lowerLimit;
        final Integer upperLimit;

        LevelDriftType(Integer lowerLimit, Integer upperLimit) {
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

    public enum DegradationPctDriftType {
        DECREASING(2, 100),
        STABLE(95, 95);

        final Integer lowerLimit;
        final Integer upperLimit;

        DegradationPctDriftType(Integer lowerLimit, Integer upperLimit) {
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

    public enum FullChargeDriftType {
        DECREASING(400, 3000),
        STABLE(2850, 2850);

        final Integer lowerLimit;
        final Integer upperLimit;

        FullChargeDriftType(Integer lowerLimit, Integer upperLimit) {
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

    public enum CycleCountDriftType {
        INCREASING(0, 200),
        STABLE(18, 18);

        final Integer lowerLimit;
        final Integer upperLimit;

        CycleCountDriftType(Integer lowerLimit, Integer upperLimit) {
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

    public enum TemperatureDriftType {
        INCREASING(20.0, 35.0),
        DECREASING(20.0, 35.0),
        STABLE(25.4, 25.4);

        final Double lowerLimit;
        final Double upperLimit;

        TemperatureDriftType(Double lowerLimit, Double upperLimit) {
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

    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());
    private static final ObjectMapper mapper = new ObjectMapper();

    private SimulatedPhone housingPhone;
    private Boolean is_ac_powered = true;
    private Boolean is_usb_powered = false;
    private String charge_state = "Charging";
    private Boolean is_main_battery_present = true;
    private Integer level = 98;
    private LevelDriftType level_CONTROL = LevelDriftType.STABLE;
    private Integer level_INCREMENT = 4;
    private Integer degradation_pct = 95;
    private Integer full_charge = 2883;
    private FullChargeDriftType full_charge_CONTROL = FullChargeDriftType.STABLE;
    private Integer full_charge_INCREMENT = 240;
    private String battery_serial_num = "VK22082416549";
    private Integer cycle_counter = 18;
    private CycleCountDriftType cycle_counter_CONTROL = CycleCountDriftType.STABLE;
    private Integer cycle_counter_INCREMENT = 4;
    private Double temperature_c = 25.0;
    private TemperatureDriftType temperature_c_CONTROL = TemperatureDriftType.STABLE;
    private Double temperature_c_INCREMENT = 0.6;
    private Double voltage = 4.394;
    private Integer current_ma = -173;
    private String technology = "Li-ion";
    private String health = "Good";
    private Integer remaining_capacity_mah = 2883;
    private Long remaining_energy_nwh = 12667901952L;
    private Integer revision_number = 1;
    private String sec_health = "Good";
    private Integer sec_level = 100;
    private Integer sec_cycle_counter = 5;
    private Double sec_voltage = 4.172;
    private Integer sec_current_ma = 0;
    private Integer sec_full_charge = 95;
    private Integer sec_remaining_capacity_mah = 95;
    private Long sec_remaining_energy_nwh = 396340000L;
    private List<Map<String, Object>> top_apps = new ArrayList<>();

    public SimulatedBattery() {
    }

    public SimulatedBattery(String battery_serial_num) {
        this.battery_serial_num = battery_serial_num;
    }

    public SimulatedBattery(String battery_serial_num, Map<String, Object> settings) {
        this.battery_serial_num = battery_serial_num;
        applySettings(settings);
    }

    @SuppressWarnings("unchecked")
    private void applySettings(Map<String, Object> settings) {
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            switch (entry.getKey()) {
                case "housing_phone":
                    housingPhone = (SimulatedPhone) entry.getValue();
                    break;
                case "is_ac_powered":
                    is_ac_powered = (Boolean) entry.getValue();
                    break;
                case "is_usb_powered":
                    is_usb_powered = (Boolean) entry.getValue();
                    break;
                case "charge_state":
                    charge_state = (String) entry.getValue();
                    break;
                case "is_main_battery_present":
                    is_main_battery_present = (Boolean) entry.getValue();
                    break;
                case "level":
                    level = (Integer) entry.getValue();
                    break;
                case "level_CONTROL":
                    level_CONTROL = (LevelDriftType) entry.getValue();
                    break;
                case "level_INCREMENT":
                    level_INCREMENT = (Integer) entry.getValue();
                    break;
                case "degradation_pct":
                    degradation_pct = (Integer) entry.getValue();
                    break;
                case "full_charge":
                    full_charge = (Integer) entry.getValue();
                    break;
                case "full_charge_CONTROL":
                    full_charge_CONTROL = (FullChargeDriftType) entry.getValue();
                    break;
                case "full_charge_INCREMENT":
                    full_charge_INCREMENT = (Integer) entry.getValue();
                    break;
                case "cycle_counter":
                    cycle_counter = (Integer) entry.getValue();
                    break;
                case "cycle_counter_CONTROL":
                    cycle_counter_CONTROL = (CycleCountDriftType) entry.getValue();
                    break;
                case "cycle_counter_INCREMENT":
                    cycle_counter_INCREMENT = (Integer) entry.getValue();
                    break;
                case "temperature_c":
                    temperature_c = (Double) entry.getValue();
                    break;
                case "temperature_c_CONTROL":
                    temperature_c_CONTROL = (TemperatureDriftType) entry.getValue();
                    break;
                case "temperature_c_INCREMENT":
                    temperature_c_INCREMENT = (Double) entry.getValue();
                    break;
                case "voltage":
                    voltage = (Double) entry.getValue();
                    break;
                case "current_ma":
                    current_ma = (Integer) entry.getValue();
                    break;
                case "technology":
                    technology = (String) entry.getValue();
                    break;
                case "health":
                    health = (String) entry.getValue();
                    break;
                case "remaining_capacity_mah":
                    remaining_capacity_mah = (Integer) entry.getValue();
                    break;
                case "remaining_energy_nwh":
                    remaining_energy_nwh = (Long) entry.getValue();
                    break;
                case "revision_number":
                    revision_number = (Integer) entry.getValue();
                    break;
                case "sec_health":
                    sec_health = (String) entry.getValue();
                    break;
                case "sec_level":
                    sec_level = (Integer) entry.getValue();
                    break;
                case "sec_cycle_counter":
                    sec_cycle_counter = (Integer) entry.getValue();
                    break;
                case "sec_voltage":
                    sec_voltage = (Double) entry.getValue();
                    break;
                case "sec_current_ma":
                    sec_current_ma = (Integer) entry.getValue();
                    break;
                case "sec_full_charge":
                    sec_full_charge = (Integer) entry.getValue();
                    break;
                case "sec_remaining_capacity_mah":
                    sec_remaining_capacity_mah = (Integer) entry.getValue();
                    break;
                case "sec_remaining_energy_nwh":
                    sec_remaining_energy_nwh = (Long) entry.getValue();
                    break;
                case "top_apps":
                    top_apps = (List<Map<String, Object>>) entry.getValue();
                    break;
                default:
                    log.error("Invalid key sent to SimulatedBattery: {}", entry.getKey());
                    break;
            }
        }
    }

    // Getters and Setters
    public SimulatedPhone getHousingPhone() {
        return housingPhone;
    }

    public void setHousingPhone(SimulatedPhone housingPhone) {
        this.housingPhone = housingPhone;
    }

    public Boolean getIs_ac_powered() {
        return is_ac_powered;
    }

    public void setIs_ac_powered(Boolean is_ac_powered) {
        this.is_ac_powered = is_ac_powered;
    }

    public Boolean getIs_usb_powered() {
        return is_usb_powered;
    }

    public void setIs_usb_powered(Boolean is_usb_powered) {
        this.is_usb_powered = is_usb_powered;
    }

    public String getCharge_state() {
        return charge_state;
    }

    public void setCharge_state(String charge_state) {
        this.charge_state = charge_state;
    }

    public Boolean getIs_main_battery_present() {
        return is_main_battery_present;
    }

    public void setIs_main_battery_present(Boolean is_main_battery_present) {
        this.is_main_battery_present = is_main_battery_present;
    }

    public Integer getLevel() {
        if (getLevel_CONTROL().equals(LevelDriftType.INCREASING)) {
            if (level + getLevel_INCREMENT() > getLevel_CONTROL().upperLimit()) {
                level = LevelDriftType.INCREASING.upperLimit();
            } else {
                level += getLevel_INCREMENT();
            }
        } else if (getLevel_CONTROL().equals(LevelDriftType.DECREASING)) {
            if (level - getLevel_INCREMENT() < getLevel_CONTROL().lowerLimit()) {
                level = LevelDriftType.DECREASING.lowerLimit();
            } else {
                level -= getLevel_INCREMENT();
            }
        }
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public LevelDriftType getLevel_CONTROL() {
        return level_CONTROL;
    }

    public void setLevel_CONTROL(LevelDriftType level_CONTROL) {
        this.level_CONTROL = level_CONTROL;
    }

    public Integer getLevel_INCREMENT() {
        return level_INCREMENT;
    }

    public void setLevel_INCREMENT(Integer level_INCREMENT) {
        this.level_INCREMENT = level_INCREMENT;
    }

    public Integer getDegradation_pct() {
        return degradation_pct;
    }

    public void setDegradation_pct(Integer degradation_pct) {
        this.degradation_pct = degradation_pct;
    }

    public Integer getFull_charge() {
        if (getFull_charge_CONTROL().equals(FullChargeDriftType.DECREASING)) {
            if (full_charge - getFull_charge_INCREMENT() < getFull_charge_CONTROL().lowerLimit()) {
                full_charge = FullChargeDriftType.DECREASING.lowerLimit();
            } else {
                full_charge -= getFull_charge_INCREMENT();
            }
        }
        return full_charge;
    }

    public void setFull_charge(Integer full_charge) {
        this.full_charge = full_charge;
    }

    public FullChargeDriftType getFull_charge_CONTROL() {
        return full_charge_CONTROL;
    }

    public void setFull_charge_CONTROL(FullChargeDriftType full_charge_CONTROL) {
        this.full_charge_CONTROL = full_charge_CONTROL;
    }

    public Integer getFull_charge_INCREMENT() {
        return full_charge_INCREMENT;
    }

    public void setFull_charge_INCREMENT(Integer full_charge_INCREMENT) {
        this.full_charge_INCREMENT = full_charge_INCREMENT;
    }

    public String getBattery_serial_num() {
        return battery_serial_num;
    }

    public void setBattery_serial_num(String battery_serial_num) {
        this.battery_serial_num = battery_serial_num;
    }

    public Integer getCycle_counter() {
        if (getCycle_counter_CONTROL().equals(CycleCountDriftType.INCREASING)) {
            if (cycle_counter + getCycle_counter_INCREMENT() > getCycle_counter_CONTROL().upperLimit()) {
                cycle_counter = CycleCountDriftType.INCREASING.upperLimit();
            } else {
                cycle_counter += getCycle_counter_INCREMENT();
            }
        }
        return cycle_counter;
    }

    public void setCycle_counter(Integer cycle_counter) {
        this.cycle_counter = cycle_counter;
    }

    public CycleCountDriftType getCycle_counter_CONTROL() {
        return cycle_counter_CONTROL;
    }

    public void setCycle_counter_CONTROL(CycleCountDriftType cycle_counter_CONTROL) {
        this.cycle_counter_CONTROL = cycle_counter_CONTROL;
    }

    public Integer getCycle_counter_INCREMENT() {
        return cycle_counter_INCREMENT;
    }

    public void setCycle_counter_INCREMENT(Integer cycle_counter_INCREMENT) {
        this.cycle_counter_INCREMENT = cycle_counter_INCREMENT;
    }

    public Double getTemperature_c() {
        if (getTemperature_c_CONTROL().equals(TemperatureDriftType.INCREASING)) {
            if (temperature_c + getTemperature_c_INCREMENT() > getTemperature_c_CONTROL().upperLimit()) {
                temperature_c = TemperatureDriftType.INCREASING.upperLimit();
            } else {
                temperature_c += getTemperature_c_INCREMENT();
            }
        } else if (getTemperature_c_CONTROL().equals(TemperatureDriftType.DECREASING)) {
            if (temperature_c - getTemperature_c_INCREMENT() < getTemperature_c_CONTROL().lowerLimit()) {
                temperature_c = TemperatureDriftType.DECREASING.lowerLimit();
            } else {
                temperature_c -= getTemperature_c_INCREMENT();
            }
        }
        return temperature_c;
    }

    public void setTemperature_c(Double temperature_c) {
        this.temperature_c = temperature_c;
    }

    public TemperatureDriftType getTemperature_c_CONTROL() {
        return temperature_c_CONTROL;
    }

    public void setTemperature_c_CONTROL(TemperatureDriftType temperature_c_CONTROL) {
        this.temperature_c_CONTROL = temperature_c_CONTROL;
    }

    public Double getTemperature_c_INCREMENT() {
        return temperature_c_INCREMENT;
    }

    public void setTemperature_c_INCREMENT(Double temperature_c_INCREMENT) {
        this.temperature_c_INCREMENT = temperature_c_INCREMENT;
    }

    public Double getVoltage() {
        return voltage;
    }

    public void setVoltage(Double voltage) {
        this.voltage = voltage;
    }

    public Integer getCurrent_ma() {
        return current_ma;
    }

    public void setCurrent_ma(Integer current_ma) {
        this.current_ma = current_ma;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public Integer getRemaining_capacity_mah() {
        return remaining_capacity_mah;
    }

    public void setRemaining_capacity_mah(Integer remaining_capacity_mah) {
        this.remaining_capacity_mah = remaining_capacity_mah;
    }

    public Long getRemaining_energy_nwh() {
        return remaining_energy_nwh;
    }

    public void setRemaining_energy_nwh(Long remaining_energy_nwh) {
        this.remaining_energy_nwh = remaining_energy_nwh;
    }

    public Integer getRevision_number() {
        return revision_number;
    }

    public void setRevision_number(Integer revision_number) {
        this.revision_number = revision_number;
    }

    public String getSec_health() {
        return sec_health;
    }

    public void setSec_health(String sec_health) {
        this.sec_health = sec_health;
    }

    public Integer getSec_level() {
        return sec_level;
    }

    public void setSec_level(Integer sec_level) {
        this.sec_level = sec_level;
    }

    public Integer getSec_cycle_counter() {
        return sec_cycle_counter;
    }

    public void setSec_cycle_counter(Integer sec_cycle_counter) {
        this.sec_cycle_counter = sec_cycle_counter;
    }

    public Double getSec_voltage() {
        return sec_voltage;
    }

    public void setSec_voltage(Double sec_voltage) {
        this.sec_voltage = sec_voltage;
    }

    public Integer getSec_current_ma() {
        return sec_current_ma;
    }

    public void setSec_current_ma(Integer sec_current_ma) {
        this.sec_current_ma = sec_current_ma;
    }

    public Integer getSec_full_charge() {
        return sec_full_charge;
    }

    public void setSec_full_charge(Integer sec_full_charge) {
        this.sec_full_charge = sec_full_charge;
    }

    public Integer getSec_remaining_capacity_mah() {
        return sec_remaining_capacity_mah;
    }

    public void setSec_remaining_capacity_mah(Integer sec_remaining_capacity_mah) {
        this.sec_remaining_capacity_mah = sec_remaining_capacity_mah;
    }

    public Long getSec_remaining_energy_nwh() {
        return sec_remaining_energy_nwh;
    }

    public void setSec_remaining_energy_nwh(Long sec_remaining_energy_nwh) {
        this.sec_remaining_energy_nwh = sec_remaining_energy_nwh;
    }

    public List<Map<String, Object>> getTop_apps() {
        return top_apps;
    }

    public void setTop_apps(List<Map<String, Object>> top_apps) {
        this.top_apps = top_apps;
    }

    /**
     * Generates battery metrics JSON for the specified timestamp.
     */
    public ObjectNode getBattery(long timestamp) {
        if (getHousingPhone() != null) {
            ObjectNode metrics = mapper.createObjectNode();
            metrics.put("is_ac_powered", getIs_ac_powered());
            metrics.put("is_usb_powered", getIs_usb_powered());
            metrics.put("charge_state", getCharge_state());
            metrics.put("is_main_battery_present", getIs_main_battery_present());
            metrics.put("level", getLevel());
            metrics.put("degradation_pct", getDegradation_pct());
            metrics.put("full_charge", getFull_charge());
            metrics.put("battery_serial_num", getBattery_serial_num());
            metrics.put("cycle_counter", getCycle_counter());
            metrics.put("temperature_c", getTemperature_c());
            metrics.put("voltage", getVoltage());
            metrics.put("current_ma", getCurrent_ma());
            metrics.put("technology", getTechnology());
            metrics.put("health", getHealth());
            metrics.put("remaining_capacity_mah", getRemaining_capacity_mah());
            metrics.put("remaining_energy_nwh", getRemaining_energy_nwh());

            // Series-specific fields
            if (getHousingPhone().getDesignated_model().series() != 92) {
                metrics.put("sec_health", getSec_health());
                metrics.put("sec_level", getSec_level());
                metrics.put("sec_cycle_counter", getSec_cycle_counter());
                metrics.put("sec_voltage", getSec_voltage());
                metrics.put("sec_current_ma", getSec_current_ma());
                metrics.put("sec_full_charge", getSec_full_charge());
                metrics.put("sec_remaining_capacity_mah", getSec_remaining_capacity_mah());
                metrics.put("sec_remaining_energy_nwh", getSec_remaining_energy_nwh());
            }

            ArrayNode topApps = mapper.createArrayNode();
            if (!getTop_apps().isEmpty()) {
                topApps = mapper.valueToTree(getTop_apps());
            } else {
                Path apData = Paths.get(Environment.getTestDataDirectory(), "injection_data", "top_apps.json");
                try {
                    topApps = (ArrayNode) mapper.readTree(Files.readAllBytes(apData));
                    ObjectReader reader = mapper.readerFor(new TypeReference<List<Map<String, Object>>>() {});
                    setTop_apps(reader.readValue(topApps));
                } catch (IOException ioe) {
                    log.error("Could not load file in the path {}: {}", apData, ioe.getMessage());
                }
            }
            metrics.set("top_apps", topApps);
            metrics.put("timestamp", timestamp);
            metrics.put("type", "BATTERY_METRICS");
            return metrics;
        } else {
            log.error("No phone was set when requesting battery info");
            return null;
        }
    }

    public ObjectNode getUsage(boolean usage, long timestamp) {
        ObjectNode metrics = mapper.createObjectNode();
        metrics.put("device_is_in_use", usage);
        metrics.put("timestamp", timestamp);
        metrics.put("type", "DEVICE_METRICS");
        return metrics;
    }

    public ObjectNode getWifiScan(boolean scanning, long timestamp) {
        ObjectNode metrics = mapper.createObjectNode();
        if (scanning) {
            metrics.put("wifi_state", "scan_start");
        } else {
            metrics.put("wifi_state", "scan_complete");
        }
        metrics.put("timestamp", timestamp);
        metrics.put("type", "BATTERY_METRICS");
        return metrics;
    }

    public ObjectNode getBluetoothScan(boolean scanning, long timestamp) {
        ObjectNode metrics = mapper.createObjectNode();
        if (scanning) {
            metrics.put("bluetooth_state", "scan_start");
        } else {
            metrics.put("bluetooth_state", "scan_complete");
        }
        metrics.put("timestamp", timestamp);
        metrics.put("type", "BATTERY_METRICS");
        return metrics;
    }

    public ObjectNode getBarcodeIdle(long timestamp) {
        ObjectNode metrics = mapper.createObjectNode();
        metrics.put("barcode_state", "BARCODE_STATE_IDLE");
        metrics.put("timestamp", timestamp);
        metrics.put("type", "BATTERY_METRICS");
        return metrics;
    }

    public ObjectNode getBarcodeDecode(long timestamp) {
        ObjectNode metrics = mapper.createObjectNode();
        metrics.put("barcode_state", "BARCODE_STATE_DECODE");
        metrics.put("timestamp", timestamp);
        metrics.put("type", "BATTERY_METRICS");
        return metrics;
    }

    @Override
    public String toString() {
        String summary = "";
        summary += getHousingPhone() == null ? "housing_phone: none\n" : "housing_phone: " + housingPhone.getDevice_serial_number() + "\n";
        summary += "is_ac_powered: " + is_ac_powered + "\n" +
                "is_usb_powered: " + is_usb_powered + "\n" +
                "charge_state: " + charge_state + "\n" +
                "is_main_battery_present: " + is_main_battery_present + "\n" +
                "level: " + level + "\n" +
                "battery_serial_num: " + battery_serial_num + "\n" +
                "cycle_counter: " + cycle_counter + "\n" +
                "temperature_c: " + temperature_c + "\n" +
                "voltage: " + voltage + "\n" +
                "health: " + health;
        return summary;
    }
}
