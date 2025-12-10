package com.spectralink.aimwright.injection;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;

import static com.spectralink.aimwright.injection.SimulatedPhone.ConnectionState.*;

/**
 * Simulates a WiFi access point for test data injection.
 * Used to generate network metrics and test roaming scenarios.
 */
public class SimulatedAP {

    public enum RssiDriftType {
        INCREASING(-80, -30),
        RANDOM(-75, -35),
        DECREASING(-80, -30),
        STABLE(-34, -34);

        final Integer lowerLimit;
        final Integer upperLimit;

        RssiDriftType(Integer lowerLimit, Integer upperLimit) {
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

    public enum WifiBandsChannels {
        CHANNEL1_24GHZ(2412, 1),
        CHANNEL2_24GHZ(2417, 2),
        CHANNEL3_24GHZ(2422, 3),
        CHANNEL4_24GHZ(2427, 4),
        CHANNEL5_24GHZ(2432, 5),
        CHANNEL6_24GHZ(2437, 6),
        CHANNEL7_24GHZ(2442, 7),
        CHANNEL8_24GHZ(2447, 8),
        CHANNEL9_24GHZ(2452, 9),
        CHANNEL10_24GHZ(2457, 10),
        CHANNEL11_24GHZ(2462, 11),
        CHANNEL12_24GHZ(2467, 12),
        CHANNEL13_24GHZ(2472, 13),

        CHANNEL36_50GHZ(5180, 36),
        CHANNEL40_50GHZ(5200, 40),
        CHANNEL44_50GHZ(5220, 44),
        CHANNEL48_50GHZ(5240, 48),
        CHANNEL52_50GHZ(5260, 52),
        CHANNEL56_50GHZ(5280, 56),
        CHANNEL60_50GHZ(5300, 60),
        CHANNEL64_50GHZ(5320, 64),
        CHANNEL100_50GHZ(5500, 100),
        CHANNEL104_50GHZ(5520, 104),
        CHANNEL108_50GHZ(5540, 108),
        CHANNEL112_50GHZ(5560, 112),
        CHANNEL116_50GHZ(5580, 116),
        CHANNEL120_50GHZ(5600, 120),
        CHANNEL124_50GHZ(5620, 124),
        CHANNEL128_50GHZ(5640, 128),
        CHANNEL132_50GHZ(5660, 132),
        CHANNEL136_50GHZ(5680, 136),
        CHANNEL140_50GHZ(5700, 140),
        CHANNEL144_50GHZ(5720, 144),
        CHANNEL149_50GHZ(5745, 149),
        CHANNEL153_50GHZ(5765, 153),
        CHANNEL157_50GHZ(5785, 157),
        CHANNEL161_50GHZ(5805, 161),
        CHANNEL165_50GHZ(5825, 165),

        CHANNEL1_60GHZ(5955, 1),
        CHANNEL5_60GHZ(5975, 5),
        CHANNEL9_60GHZ(5995, 9),
        CHANNEL13_60GHZ(6015, 13),
        CHANNEL17_60GHZ(6035, 17),
        CHANNEL21_60GHZ(6055, 21),
        CHANNEL25_60GHZ(6075, 25),
        CHANNEL29_60GHZ(6095, 29),
        CHANNEL33_60GHZ(6115, 33),
        CHANNEL37_60GHZ(6135, 37),
        CHANNEL41_60GHZ(6155, 41),
        CHANNEL45_60GHZ(6175, 45),
        CHANNEL49_60GHZ(6195, 49),
        CHANNEL53_60GHZ(6215, 53),
        CHANNEL57_60GHZ(6235, 57),
        CHANNEL61_60GHZ(6255, 61),
        CHANNEL65_60GHZ(6275, 65),
        CHANNEL69_60GHZ(6295, 69),
        CHANNEL73_60GHZ(6315, 73),
        CHANNEL77_60GHZ(6355, 77),
        CHANNEL81_60GHZ(6355, 81),
        CHANNEL85_60GHZ(6375, 85),
        CHANNEL89_60GHZ(6395, 89),
        CHANNEL93_60GHZ(6415, 93),
        CHANNEL97_60GHZ(6435, 97),
        CHANNEL101_60GHZ(6455, 101),
        CHANNEL105_60GHZ(6475, 105),
        CHANNEL109_60GHZ(6495, 109),
        CHANNEL113_60GHZ(6515, 113),
        CHANNEL117_60GHZ(6535, 117),
        CHANNEL121_60GHZ(6555, 121),
        CHANNEL125_60GHZ(6575, 125),
        CHANNEL129_60GHZ(6595, 129),
        CHANNEL133_60GHZ(6615, 133),
        CHANNEL137_60GHZ(6635, 137),
        CHANNEL141_60GHZ(6655, 141),
        CHANNEL145_60GHZ(6675, 145),
        CHANNEL149_60GHZ(6695, 149),
        CHANNEL153_60GHZ(6715, 153),
        CHANNEL157_60GHZ(6735, 157),
        CHANNEL161_60GHZ(6755, 161),
        CHANNEL165_60GHZ(6775, 165),
        CHANNEL169_60GHZ(6795, 169),
        CHANNEL173_60GHZ(6815, 173),
        CHANNEL177_60GHZ(6835, 177),
        CHANNEL181_60GHZ(6855, 181),
        CHANNEL185_60GHZ(6875, 185),
        CHANNEL189_60GHZ(6895, 189),
        CHANNEL193_60GHZ(6915, 193),
        CHANNEL197_60GHZ(6935, 197),
        CHANNEL201_60GHZ(6955, 201),
        CHANNEL205_60GHZ(6975, 205),
        CHANNEL209_60GHZ(6995, 209),
        CHANNEL213_60GHZ(7015, 213),
        CHANNEL217_60GHZ(7035, 217),
        CHANNEL221_60GHZ(7055, 221),
        CHANNEL225_60GHZ(7075, 225),
        CHANNEL229_60GHZ(7095, 229),
        CHANNEL233_60GHZ(7115, 233);

        final Integer band;
        final Integer channel;

        WifiBandsChannels(Integer band, Integer channel) {
            this.band = band;
            this.channel = channel;
        }

        public Integer band() {
            return this.band;
        }

        public Integer channel() {
            return this.channel;
        }
    }

    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());
    private static final ObjectMapper mapper = new ObjectMapper();

    private WifiBandsChannels ap_band = WifiBandsChannels.CHANNEL40_50GHZ;
    private WifiBandsChannels ap_channel = ap_band;
    private String ap_bssid = "5c:0e:8b:c9:1b:00";
    private String ap_ssid = "ICU Unit";
    private RssiDriftType ap_rssi_CONTROL = RssiDriftType.STABLE;
    private Integer ap_rssi = (ap_rssi_CONTROL.lowerLimit() + ap_rssi_CONTROL.upperLimit()) / 2;
    private SimulatedPhone.ConnectionState connection_state = DISCONNECTED;
    private Integer rssiIncrement = 4;
    private Boolean handOff = false;

    public SimulatedAP() {
    }

    public SimulatedAP(String ap_ssid) {
        this.ap_ssid = ap_ssid;
    }

    public SimulatedAP(Map<String, Object> settings) {
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            switch(entry.getKey()) {
                case "ap_band":
                    ap_band = (WifiBandsChannels) entry.getValue();
                    break;
                case "ap_channel":
                    ap_channel = (WifiBandsChannels) entry.getValue();
                    break;
                case "ap_bssid":
                    ap_bssid = (String) entry.getValue();
                    break;
                case "ap_ssid":
                    ap_ssid = (String) entry.getValue();
                    break;
                case "ap_rssi_CONTROL":
                    ap_rssi_CONTROL = (RssiDriftType) entry.getValue();
                    break;
                case "ap_rssi":
                    ap_rssi = (Integer) entry.getValue();
                    break;
                default:
                    log.error("Invalid key sent to SimulatedAP: {}", entry.getKey());
                    break;
            }
        }
    }

    public String getAp_ssid() {
        return ap_ssid;
    }

    public void setAp_ssid(String ap_ssid) {
        this.ap_ssid = ap_ssid;
    }

    public String getAp_bssid() {
        return ap_bssid;
    }

    public void setAp_bssid(String ap_bssid) {
        this.ap_bssid = ap_bssid;
    }

    public Integer getAp_band() {
        return ap_band.band();
    }

    public void setAp_band(WifiBandsChannels ap_band) {
        this.ap_band = ap_band;
        ap_channel = ap_band;
    }

    public Integer getAp_channel() {
        return ap_channel.channel();
    }

    public void setAp_channel(WifiBandsChannels ap_channel) {
        this.ap_channel = ap_channel;
        ap_band = ap_channel;
    }

    public Integer getAp_rssi() {
        if (getAp_rssi_CONTROL().equals(RssiDriftType.INCREASING)) {
            if (ap_rssi - rssiIncrement < RssiDriftType.INCREASING.lowerLimit()) {
                ap_rssi = RssiDriftType.INCREASING.lowerLimit();
            } else {
                ap_rssi -= rssiIncrement;
            }
        } else if (getAp_rssi_CONTROL().equals(RssiDriftType.DECREASING)) {
            if (ap_rssi + rssiIncrement > RssiDriftType.DECREASING.upperLimit()) {
                ap_rssi = RssiDriftType.DECREASING.upperLimit();
            } else {
                ap_rssi += rssiIncrement;
            }
        } else if (getAp_rssi_CONTROL().equals(RssiDriftType.RANDOM)) {
            ap_rssi = RssiDriftType.RANDOM.lowerLimit() + new Random().nextInt(RssiDriftType.RANDOM.upperLimit() - RssiDriftType.RANDOM.lowerLimit() + 1);
        }
        return ap_rssi;
    }

    public void setAp_rssi(Integer ap_rssi) {
        this.ap_rssi = ap_rssi;
    }

    public RssiDriftType getAp_rssi_CONTROL() {
        return ap_rssi_CONTROL;
    }

    public void setAp_rssi_CONTROL(RssiDriftType ap_rssi_CONTROL) {
        this.ap_rssi_CONTROL = ap_rssi_CONTROL;
    }

    public void setRssiIncrement(Integer rssiIncrement) {
        this.rssiIncrement = rssiIncrement;
    }

    public Integer getRssiIncrement() {
        return rssiIncrement;
    }

    public SimulatedPhone.ConnectionState getConnection_state() {
        return connection_state;
    }

    public void setConnection_state(SimulatedPhone.ConnectionState connection_state) {
        this.connection_state = connection_state;
    }

    public Boolean getHandOff() {
        return handOff;
    }

    public void setHandOff(Boolean handOff) {
        this.handOff = handOff;
    }

    public ObjectNode getSelectedAP(ArrayNode candidates, long timestamp) {
        ObjectNode metrics = mapper.createObjectNode();
        metrics.set("network_candidate_aps", candidates);
        if (getHandOff()) {
            Random generator = new Random();
            int handoffMs = generator.nextInt(500);
            metrics.put("roam_handoff_ms", handoffMs);
        }
        metrics.put("ap_band", getAp_band());
        metrics.put("ap_channel", getAp_channel());
        metrics.put("ap_bssid", getAp_bssid());
        if (candidates.isEmpty()) {
            metrics.put("ap_ssid", "<unknown ssid>");
        } else {
            metrics.put("ap_ssid", getAp_ssid());
        }
        metrics.put("ap_rssi", getAp_rssi());
        if (getHandOff()) {
            metrics.put("network_type", "WIFI");
            metrics.put("connection_status", getConnection_state().state());
        }
        metrics.put("timestamp", timestamp);
        metrics.put("type", "NETWORK_METRICS");
        setHandOff(false);
        return metrics;
    }

    public ObjectNode getCandidateAP() {
        ObjectNode metrics = mapper.createObjectNode();
        metrics.put("bssid", getAp_bssid());
        metrics.put("rssi", getAp_rssi());
        metrics.put("ap_ssid", getAp_ssid());
        return metrics;
    }

    @Override
    public String toString() {
        return "ap_channel: " + ap_channel + "\n" +
                "ap_band: " + ap_band + "\n" +
                "ap_bssid: " + ap_bssid + "\n" +
                "ap_ssid: " + ap_ssid + "\n" +
                "ap_rssi_CONTROL: " + ap_rssi_CONTROL + "\n" +
                "ap_rssi: " + ap_rssi + "\n" +
                "rssiIncrement: " + rssiIncrement + "\n" +
                "handOff: " + handOff;
    }
}
