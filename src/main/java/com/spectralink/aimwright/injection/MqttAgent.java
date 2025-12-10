package com.spectralink.aimwright.injection;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.LoggerFactory;
import com.spectralink.aimwright.common.Settings;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * MQTT client agent for sending simulated device metrics to a gateway broker.
 * Used for test data injection and device simulation.
 */
public class MqttAgent {
    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());
    private String brokerAddress;
    private final MqttConnectOptions options = new MqttConnectOptions();
    private IMqttClient agent;
    private final ConcurrentLinkedDeque<MqttMessage> inboundMessages = new ConcurrentLinkedDeque<>();
    private String topic;
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates an MQTT agent for the specified device serial number.
     *
     * @param serial Device serial number (used for topic)
     * @param customPort Optional custom MQTT port (null for default 1883)
     */
    public MqttAgent(String serial, String customPort) {
        try {
            if (serial != null && !serial.isEmpty()) {
                topic = "devices/spectralink/" + serial;
            } else {
                throw new Exception("No serial number was specified for MQTT agent");
            }
            String port = "1883";
            if (customPort != null && !customPort.isEmpty()) {
                port = customPort;
            }
            brokerAddress = Settings.getGatewayAddress();
            String gatewayMqttUri = "";
            if (brokerAddress == null || brokerAddress.isEmpty()) {
                throw new Exception("Could not find gateway address for current context");
            } else {
                gatewayMqttUri = "tcp://" + brokerAddress + ":" + port;
            }

            String publisherId = UUID.randomUUID().toString();
            agent = new MqttClient(gatewayMqttUri, publisherId);

            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(30);
            options.setKeepAliveInterval(60);
        } catch (Exception e) {
            log.error("Failed to configure new MQTT session: {}", e.getMessage());
        }
    }

    /**
     * Connects to the MQTT broker.
     */
    public void connect() {
        try {
            if (!agent.isConnected()) {
                agent.connect(options);
                log.debug("Connected to MQTT broker {}", brokerAddress);
            } else {
                log.debug("Agent was already connected to MQTT broker {}", brokerAddress);
            }
        } catch (MqttSecurityException mse) {
            log.error("Cannot authenticate to MQTT server {}: {}", brokerAddress, mse.getMessage());
        } catch (MqttException me) {
            log.error("Failed to connect to MQTT server {}: {}", brokerAddress, me.getMessage());
        }
    }

    /**
     * Sends a JSON message to the MQTT topic.
     *
     * @param json The JSON payload to send
     */
    public void sendMessage(ObjectNode json) {
        try {
            if (!agent.isConnected()) {
                connect();
            }
            ObjectWriter writer = mapper.writer();
            byte[] payload = writer.writeValueAsBytes(json);
            agent.publish(topic, payload, 2, false);
            String message = new String(payload, StandardCharsets.UTF_8);
            logPrettyJson(json);
        } catch (MqttSecurityException mse) {
            log.error("Cannot authenticate to MQTT server {}: {}", brokerAddress, mse.getMessage());
        } catch (MqttException me) {
            log.error("Failed to connect to MQTT server {}: {}", brokerAddress, me.getMessage());
        } catch (JsonProcessingException jpe) {
            log.error("Could not process json object: {}", jpe.getMessage());
        }
    }

    /**
     * Subscribes to receive messages on the topic.
     */
    public void receiveMessages() {
        try {
            if (!agent.isConnected()) {
                connect();
            }
            agent.setCallback(new MqttCallback() {
                public void connectionLost(Throwable cause) {}

                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    inboundMessages.add(message);
                }

                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            agent.subscribe(topic);
        } catch (MqttException me) {
            log.error("Failed to connect to MQTT server {}: {}", brokerAddress, me.getMessage());
        }
    }

    /**
     * Unsubscribes from the topic.
     */
    public void stopReceivingMessages() {
        try {
            if (agent.isConnected()) {
                agent.unsubscribe(topic);
            }
        } catch (MqttException me) {
            log.error("Failed to unsubscribe for topic '{}' from MQTT server {}: {}", topic, brokerAddress, me.getMessage());
        }
    }

    /**
     * Disconnects from the MQTT broker.
     */
    public void disconnect() {
        try {
            if (agent.isConnected()) {
                agent.disconnect();
                log.debug("Disconnected from MQTT broker {}", brokerAddress);
            } else {
                log.debug("Agent was already disconnected from MQTT broker {}", brokerAddress);
            }
        } catch (MqttException me) {
            log.error("Failed to disconnect from MQTT server {}: {}", brokerAddress, me.getMessage());
        }
    }

    /**
     * Retrieves and clears all received messages.
     *
     * @return List of message payloads
     */
    public List<byte[]> getMessages() {
        List<byte[]> messages = new ArrayList<>();
        Iterator<MqttMessage> messageIndex = inboundMessages.iterator();
        while (messageIndex.hasNext()) {
            MqttMessage messageObject = messageIndex.next();
            byte[] messagePayload = messageObject.getPayload();
            messages.add(messagePayload);
            log.debug("Removing message '{}'", messagePayload);
        }
        inboundMessages.clear();
        return messages;
    }

    private void logPrettyJson(ObjectNode json) {
        log.debug("Metrics sent:");
        String jsonOutput = json.toPrettyString();
        for (String line: jsonOutput.split("\n")) log.debug(line);
    }
}
