package com.spectralink.aimwright.api;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spectralink.aimwright.common.Session;
import com.spectralink.aimwright.common.Settings;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.spectralink.aimwright.api.ApiClient.Method.GET;

public class AmieApiClient {
    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    public Map<String, String> getDefaultHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, "application/json, text/plain, */*");
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        headers.put(HttpHeaders.COOKIE, Session.getCookie());
        return headers;
    }

    private String endpointValidation(String endpoint){
        String url;
        if (endpoint.startsWith("http")) {
            // the endpoint provided is a full url, using it as provided
            url = endpoint;
        } else {
            url = Settings.get("instance.api");
            if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
            if (endpoint.startsWith("/api")) {
                url += endpoint;
            } else if (endpoint.startsWith("api")) {
                url = url + "/" + endpoint;
            } else if (!endpoint.startsWith("/")) {
                url = url + "/api/" + endpoint;
            } else {
                url = url + "/api" + endpoint;
            }
        }
        return url;
    }

    public ApiResponse sendRequest(ApiClient.Method method, String endpoint, Map<String, String> headers, String payload) {
        return new ApiClient().sendRequest(method, endpointValidation(endpoint), headers, payload);
    }

    public ApiResponse sendRequest(ApiClient.Method method, String endpoint, Map<String, String> headers, JsonNode payload) {
        return sendRequest(method, endpoint, headers, payload.toString());
    }

    public ApiResponse sendRequest(ApiClient.Method method, String endpoint, Map<String, String> headers, ArrayNode payload) {
        return sendRequest(method, endpoint, headers, payload.toString());
    }

    public ApiResponse sendRequest(ApiClient.Method method, String endpoint, Map<String, String> headers) {
        return sendRequest(method, endpoint, headers, "");
    }

    public ApiResponse sendRequest(ApiClient.Method method, String endpoint, String payload) {
        return sendRequest(method, endpoint, getDefaultHeaders(), payload);
    }

    public ApiResponse sendRequest(ApiClient.Method method, String endpoint, ObjectNode payload) {
        return sendRequest(method, endpoint, getDefaultHeaders(), payload.toString());
    }

    public ApiResponse sendRequest(ApiClient.Method method, String endpoint, ArrayNode payload) {
        return sendRequest(method, endpoint, getDefaultHeaders(), payload.toString());
    }

    public ApiResponse sendRequest(ApiClient.Method method, String endpoint) {
        return sendRequest(method, endpoint, getDefaultHeaders(), "");
    }

    public ApiResponse sendGetRequest(String endpoint) {
        ApiResponse summary = sendRequest(GET, endpoint, getDefaultHeaders());
//        Integer response = summary.getResponseCode();
//        Assert.assertTrue(response < 300, "Unexpected Response Code " + response);
        return summary;
    }




}
