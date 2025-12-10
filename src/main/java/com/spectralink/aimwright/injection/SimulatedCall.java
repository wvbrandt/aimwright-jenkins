package com.spectralink.aimwright.injection;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;

/**
 * Simulates a phone call for test data injection.
 * Generates call metrics including packet loss, jitter, and codec information.
 */
public class SimulatedCall {

    public enum MissedPacketsDriftType {
        INCREASING(0, 80),
        DECREASING(0, 80),
        RANDOM(1, 50),
        STABLE(2, 2);

        final Integer lowerLimit;
        final Integer upperLimit;

        MissedPacketsDriftType(Integer lowerLimit, Integer upperLimit) {
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

    public enum DroppedPacketsDriftType {
        INCREASING(1, 150),
        DECREASING(0, 150),
        RANDOM(1, 50),
        STABLE(2, 2);

        final Integer lowerLimit;
        final Integer upperLimit;

        DroppedPacketsDriftType(Integer lowerLimit, Integer upperLimit) {
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

    public enum JitterDriftType {
        INCREASING(1, 150),
        DECREASING(1, 150),
        RANDOM(1, 40),
        STABLE(2, 2);

        final Integer lowerLimit;
        final Integer upperLimit;

        JitterDriftType(Integer lowerLimit, Integer upperLimit) {
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

    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());
    private static final ObjectMapper mapper = new ObjectMapper();

    private SimulatedPhone calling_phone;
    private String call_id;
    private SimulatedPhone.CallDirection call_type = SimulatedPhone.CallDirection.OUTGOING;
    private Integer packets_missed = 3;
    private MissedPacketsDriftType packets_missed_CONTROL = MissedPacketsDriftType.STABLE;
    private Integer packets_missed_INCREMENT = 5;
    private Integer packets_missed_RATE = 0;
    private Double packets_missed_pct = 0.0;
    private Integer packets_dropped = 1;
    private DroppedPacketsDriftType packets_dropped_CONTROL = DroppedPacketsDriftType.STABLE;
    private Integer packets_dropped_INCREMENT = 5;
    private Integer packets_dropped_RATE = 0;
    private Double packets_dropped_pct = 0.0;
    private Integer jitter_ms = 6;
    private JitterDriftType jitter_ms_CONTROL = JitterDriftType.STABLE;
    private Integer jitter_ms_INCREMENT = 3;
    private String codec = "PCMU";
    private Integer burst_rate = 3;
    private String extension = "7152";
    private Boolean call_dropped = false;
    private Integer metricsInterval = 5;

    public SimulatedCall(SimulatedPhone phone) {
        calling_phone = phone;
        setRandomCallId();
    }

    public SimulatedCall(SimulatedPhone phone, Map<String, Object> settings) {
        calling_phone = phone;
        setRandomCallId();
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            switch(entry.getKey()) {
                case "call_type":
                    call_type = (SimulatedPhone.CallDirection) entry.getValue();
                    break;
                case "packets_missed":
                    packets_missed = (Integer) entry.getValue();
                    break;
                case "packets_missed_CONTROL":
                    packets_missed_CONTROL = (MissedPacketsDriftType) entry.getValue();
                    break;
                case "packets_missed_INCREMENT":
                    packets_missed_INCREMENT = (Integer) entry.getValue();
                    break;
                case "packets_missed_pct":
                    packets_missed_pct = (Double) entry.getValue();
                    break;
                case "packets_dropped":
                    packets_dropped = (Integer) entry.getValue();
                    break;
                case "packets_dropped_CONTROL":
                    packets_dropped_CONTROL = (DroppedPacketsDriftType) entry.getValue();
                    break;
                case "packets_dropped_INCREMENT":
                    packets_dropped_INCREMENT = (Integer) entry.getValue();
                    break;
                case "jitter_ms":
                    jitter_ms = (Integer) entry.getValue();
                    break;
                case "jitter_ms_CONTROL":
                    jitter_ms_CONTROL = (JitterDriftType) entry.getValue();
                    break;
                case "jitter_ms_INCREMENT":
                    jitter_ms_INCREMENT = (Integer) entry.getValue();
                    break;
                default:
                    log.error("Invalid key sent to SimulatedCall: {}", entry.getKey());
                    break;
            }
        }
    }

    public String getCall_id() {
        return call_id;
    }

    public void setCall_id(String call_id) {
        this.call_id = call_id;
    }

    private String getRandomHexString(int stringLength) {
        Random generator = new Random();
        StringBuilder randomString = new StringBuilder();
        while(randomString.length() < stringLength) {
            randomString.append(Integer.toHexString(generator.nextInt(16)));
        }
        return randomString.toString();
    }

    public void setRandomCallId() {
        String callId = getRandomHexString(8) + "-" +
                getRandomHexString(4) + "-" +
                getRandomHexString(4) + "-" +
                getRandomHexString(4) + "-" +
                getRandomHexString(12);
        call_id = callId;
    }

    public SimulatedPhone getCalling_phone() {
        return calling_phone;
    }

    public void setCalling_phone(SimulatedPhone calling_phone) {
        this.calling_phone = calling_phone;
    }

    public SimulatedPhone.CallDirection getCall_type() {
        return call_type;
    }

    public void setCall_type(SimulatedPhone.CallDirection call_type) {
        this.call_type = call_type;
    }

    public Integer getPackets_missed() {
        int intervalPacketsMissed = getPackets_missed_RATE();
        if (getPackets_missed_CONTROL().equals(MissedPacketsDriftType.INCREASING)) {
            if (intervalPacketsMissed + getPackets_missed_INCREMENT() > MissedPacketsDriftType.INCREASING.upperLimit()) {
                intervalPacketsMissed = MissedPacketsDriftType.INCREASING.upperLimit();
            } else {
                intervalPacketsMissed += getPackets_missed_INCREMENT();
            }
        } else if (getPackets_missed_CONTROL().equals(MissedPacketsDriftType.DECREASING)) {
            if (intervalPacketsMissed - getPackets_missed_INCREMENT() < MissedPacketsDriftType.DECREASING.lowerLimit()) {
                intervalPacketsMissed = MissedPacketsDriftType.DECREASING.lowerLimit();
            } else {
                intervalPacketsMissed -= getPackets_missed_INCREMENT();
            }
        } else if (getPackets_missed_CONTROL().equals(MissedPacketsDriftType.RANDOM)) {
            intervalPacketsMissed = MissedPacketsDriftType.RANDOM.lowerLimit() + new Random().nextInt(MissedPacketsDriftType.RANDOM.upperLimit() - MissedPacketsDriftType.RANDOM.lowerLimit() + 1);
        }
        updatePackets_missed_pct(intervalPacketsMissed);
        setPackets_missed_RATE(intervalPacketsMissed);
        packets_missed += intervalPacketsMissed;
        return packets_missed;
    }

    public void setPackets_missed(Integer packets_missed) {
        this.packets_missed = packets_missed;
    }

    public MissedPacketsDriftType getPackets_missed_CONTROL() {
        return packets_missed_CONTROL;
    }

    public void setPackets_missed_CONTROL(MissedPacketsDriftType packets_missed_CONTROL) {
        this.packets_missed_CONTROL = packets_missed_CONTROL;
    }

    public Integer getPackets_missed_INCREMENT() {
        return packets_missed_INCREMENT;
    }

    public void setPackets_missed_INCREMENT(Integer packets_missed_INCREMENT) {
        this.packets_missed_INCREMENT = packets_missed_INCREMENT;
    }

    public Integer getPackets_missed_RATE() {
        return packets_missed_RATE;
    }

    public void setPackets_missed_RATE(Integer packets_missed_RATE) {
        this.packets_missed_RATE = packets_missed_RATE;
    }

    public Double getPackets_missed_pct() {
        return packets_missed_pct;
    }

    private void setPackets_missed_pct(Double packets_missed_pct) {
        this.packets_missed_pct = packets_missed_pct;
    }

    private void updatePackets_missed_pct(int packetsMissed) {
        this.packets_missed_pct = packetsMissed / (double) (50 * getMetricsInterval());
    }

    public Integer getPackets_dropped() {
        int intervalPacketsDropped = getPackets_dropped_RATE();
        if (getPackets_dropped_CONTROL().equals(DroppedPacketsDriftType.INCREASING)) {
            if (getPackets_dropped_RATE() + getPackets_dropped_INCREMENT() > DroppedPacketsDriftType.INCREASING.upperLimit()) {
                intervalPacketsDropped = DroppedPacketsDriftType.INCREASING.upperLimit();
            } else {
                intervalPacketsDropped += getPackets_dropped_INCREMENT();
            }
        } else if (getPackets_dropped_CONTROL().equals(DroppedPacketsDriftType.DECREASING)) {
            if (intervalPacketsDropped - getPackets_dropped_INCREMENT() < DroppedPacketsDriftType.DECREASING.lowerLimit()) {
                intervalPacketsDropped = DroppedPacketsDriftType.DECREASING.lowerLimit();
            } else {
                intervalPacketsDropped -= getPackets_dropped_INCREMENT();
            }
        } else if (getPackets_dropped_CONTROL().equals(DroppedPacketsDriftType.RANDOM)) {
            intervalPacketsDropped += DroppedPacketsDriftType.RANDOM.lowerLimit() + new Random().nextInt(DroppedPacketsDriftType.RANDOM.upperLimit() - DroppedPacketsDriftType.RANDOM.lowerLimit() + 1);
        }
        updatePackets_dropped_pct(intervalPacketsDropped);
        setPackets_dropped_RATE(intervalPacketsDropped);
        packets_dropped += intervalPacketsDropped;
        return packets_dropped;
    }

    public void setPackets_dropped(Integer packets_dropped) {
        this.packets_dropped = packets_dropped;
    }

    public DroppedPacketsDriftType getPackets_dropped_CONTROL() {
        return packets_dropped_CONTROL;
    }

    public void setPackets_dropped_CONTROL(DroppedPacketsDriftType packets_dropped_CONTROL) {
        this.packets_dropped_CONTROL = packets_dropped_CONTROL;
    }

    public Integer getPackets_dropped_INCREMENT() {
        return packets_dropped_INCREMENT;
    }

    public void setPackets_dropped_INCREMENT(Integer packets_dropped_INCREMENT) {
        this.packets_dropped_INCREMENT = packets_dropped_INCREMENT;
    }

    public Integer getPackets_dropped_RATE() {
        return packets_dropped_RATE;
    }

    public void setPackets_dropped_RATE(Integer packets_dropped_RATE) {
        this.packets_dropped_RATE = packets_dropped_RATE;
    }

    public Double getPackets_dropped_pct() {
        return packets_dropped_pct;
    }

    private void setPackets_dropped_pct(Double packets_dropped_pct) {
        this.packets_dropped_pct = packets_dropped_pct;
    }

    private void updatePackets_dropped_pct(int packetsDropped) {
        this.packets_dropped_pct = packetsDropped / (double) (50 * getMetricsInterval());
    }

    public Integer getJitter_ms() {
        if (getJitter_ms_CONTROL().equals(JitterDriftType.INCREASING)) {
            if (jitter_ms + getJitter_ms_INCREMENT() > JitterDriftType.INCREASING.upperLimit()) {
                jitter_ms = JitterDriftType.INCREASING.upperLimit();
            } else {
                jitter_ms += getJitter_ms_INCREMENT();
            }
        } else if (getJitter_ms_CONTROL().equals(JitterDriftType.DECREASING)) {
            if (jitter_ms - getJitter_ms_INCREMENT() < JitterDriftType.DECREASING.lowerLimit()) {
                jitter_ms = JitterDriftType.DECREASING.lowerLimit();
            } else {
                jitter_ms -= getJitter_ms_INCREMENT();
            }
        } else if (getJitter_ms_CONTROL().equals(JitterDriftType.RANDOM)) {
            jitter_ms = JitterDriftType.RANDOM.lowerLimit() + new Random().nextInt(JitterDriftType.RANDOM.upperLimit() - JitterDriftType.RANDOM.lowerLimit() + 1);
        }
        return jitter_ms;
    }

    public void setJitter_ms(Integer jitter_ms) {
        this.jitter_ms = jitter_ms;
    }

    public JitterDriftType getJitter_ms_CONTROL() {
        return jitter_ms_CONTROL;
    }

    public void setJitter_ms_CONTROL(JitterDriftType jitter_ms_CONTROL) {
        this.jitter_ms_CONTROL = jitter_ms_CONTROL;
    }

    public Integer getJitter_ms_INCREMENT() {
        return jitter_ms_INCREMENT;
    }

    public void setJitter_ms_INCREMENT(Integer jitter_ms_INCREMENT) {
        this.jitter_ms_INCREMENT = jitter_ms_INCREMENT;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public Integer getBurst_rate() {
        return burst_rate;
    }

    public void setBurst_rate(Integer burst_rate) {
        this.burst_rate = burst_rate;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Boolean getCall_dropped() {
        return call_dropped;
    }

    public void setCall_dropped(Boolean call_dropped) {
        this.call_dropped = call_dropped;
    }

    public Integer getMetricsInterval() {
        return metricsInterval;
    }

    public void setMetricsInterval(Integer metricsInterval) {
        this.metricsInterval = metricsInterval;
    }

    public ObjectNode getCallBegin(long timestamp) {
        ObjectNode metrics = mapper.createObjectNode();
        metrics.put("call_id", getCall_id());
        metrics.put("event", getCall_type().event());
        metrics.put("timestamp", timestamp);
        metrics.put("type", "CALL_METRICS");
        metrics.put("ap_rssi", getCalling_phone().getCurrent_ap().getAp_rssi());
        return metrics;
    }

    public ObjectNode getCallConfirmed(long timestamp) {
        ObjectNode metrics = mapper.createObjectNode();
        metrics.put("call_id", getCall_id());
        metrics.put("event", "CALL_STATE_CONFIRMED");
        metrics.put("timestamp", timestamp);
        metrics.put("type", "CALL_METRICS");
        metrics.put("ap_rssi", getCalling_phone().getCurrent_ap().getAp_rssi());
        return metrics;
    }

    public ObjectNode getCallDetails(long timestamp) {
        ObjectNode metrics = mapper.createObjectNode();
        metrics.put("call_id", getCall_id());
        metrics.put("call_type", getCall_type().direction());
        metrics.put("packets_missed", getPackets_missed());
        metrics.put("packets_missed_pct", getPackets_missed_pct());
        metrics.put("packets_dropped", getPackets_dropped());
        metrics.put("packets_dropped_pct", getPackets_dropped_pct());
        metrics.put("jitter_ms", getJitter_ms());
        metrics.put("codec", getCodec());
        metrics.put("burst_rate", getBurst_rate());
        metrics.put("extension", getExtension());
        metrics.put("timestamp", timestamp);
        metrics.put("type", "CALL_METRICS");
        metrics.put("ap_rssi", getCalling_phone().getCurrent_ap().getAp_rssi());
        return metrics;
    }

    public ObjectNode getCallEnd(long timestamp) {
        ObjectNode metrics = mapper.createObjectNode();
        metrics.put("call_id", getCall_id());
        metrics.put("event", "CALL_STATE_DISCONNECTED");
        metrics.put("call_dropped", getCall_dropped());
        metrics.put("timestamp", timestamp);
        metrics.put("type", "CALL_METRICS");
        metrics.put("ap_rssi", getCalling_phone().getCurrent_ap().getAp_rssi());
        return metrics;
    }

    @Override
    public String toString() {
        return "calling_phone: " + calling_phone.getDevice_serial_number() + "\n" +
                "call_id: " + call_id + "\n" +
                "call_type: " + call_type + "\n" +
                "packets_missed: " + packets_missed + "\n" +
                "packets_dropped: " + packets_dropped + "\n" +
                "jitter_ms: " + jitter_ms + "\n" +
                "codec: " + codec + "\n" +
                "extension: " + extension + "\n" +
                "call_dropped: " + call_dropped;
    }
}
