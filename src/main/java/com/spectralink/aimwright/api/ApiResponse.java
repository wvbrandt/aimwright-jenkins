package com.spectralink.aimwright.api;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ApiResponse {
    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass());
    private final Header[] responseHeaders;
    private final Integer responseCode;
    private final String responseBody;

    ApiResponse(ClassicHttpResponse response) {
        responseBody = entityToString(response.getEntity());
        responseCode = response.getCode();
        responseHeaders = response.getHeaders();
    }

    ApiResponse() {
        responseBody = "";
        responseCode = 0;
        responseHeaders = new Header[]{};
    }

    private String entityToString(HttpEntity entity) {
        try {
            return EntityUtils.toString(entity);
        } catch (ParseException | IOException e){
            log.error("error parsing the http entity as string");
            log.error(e.getMessage(), e);
        }
        return "";
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public Header[] getResponseHeaders() {
        return responseHeaders;
    }

    public String getStringBody() {
        return responseBody;
    }

    public ObjectNode getJsonObjectBody() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            if (jsonNode.isObject()) {
                objectNode = (ObjectNode) jsonNode;
            } else if (jsonNode.isArray()) {
                log.error("Response body is an array, not an object : {}", responseBody);
            } else {
                log.error("Response body is not a valid json object : {}", responseBody);
            }
        } catch (IOException e){
            log.error("error parsing the response body as json");
            log.error(e.getMessage(), e);
        }
        return objectNode;
    }

    public ArrayNode getJsonArrayBody() {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        try {
            JsonNode jsonNode = objectMapper.readTree(getStringBody());
            if (jsonNode.isArray()) {
                arrayNode = (ArrayNode) jsonNode;
            } else if (jsonNode.isObject()) {
                log.error("Response body is an object, not an array : {}", responseBody);
            } else {
                log.error("response body is not a valid json array : {}", responseBody);
            }
        } catch (IOException e){
            log.error("error parsing the response body as json array");
            log.error(e.getMessage(), e);
        }
        return arrayNode;
    }

    public String toString(){
        return String.format("response code : %s - response body: %s", responseCode, responseBody);
    }
}
