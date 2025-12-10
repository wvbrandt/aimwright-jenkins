package com.spectralink.aimwright.api;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class ApiClient {

    private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

     public enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }

    private ApiResponse request(HttpUriRequest request, Map<String, String> headers, String payload) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.setHeader(header.getKey(), header.getValue());
            }
            log.trace("request headers : {}", headers);
            if (!payload.isEmpty()){
                log.trace("request data payload: {}", payload);
                request.setEntity(new StringEntity(payload));
            }
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                ApiResponse apiResponse = new ApiResponse(response);
                log.trace("{} - {} : {} : {}",
                        apiResponse.getResponseCode(), request.getMethod(), request.getRequestUri(),
                        (apiResponse.getStringBody().length() <= 300 ? apiResponse.getStringBody() : "response body in trace"));
                log.trace(apiResponse.toString());
                return apiResponse;
            } catch (IOException e) {
                log.error("request to {} could not be fulfilled", request.getRequestUri());
                log.error(e.getMessage(), e);
            }
        } catch (IOException e) {
            log.error("request to {} could not be fulfilled", request.getRequestUri());
            log.error(e.getMessage(), e);
        }
        return new ApiResponse();
    }

    public ApiResponse sendRequest(Method method, String url, Map<String, String> headers, String payload) {
        switch (method) {
            case GET:
                return request(new HttpGet(url), headers, payload);
            case POST:
                return request(new HttpPost(url), headers, payload);
            case PUT:
                return request(new HttpPut(url), headers, payload);
            case DELETE:
                return request(new HttpDelete(url), headers, payload);
        }
        return new ApiResponse();
    }

    public ApiResponse sendRequest(Method method, String url, Map<String, String> headers, ObjectNode payload) {
        return sendRequest(method, url, headers, payload.toString());
    }

    public ApiResponse sendRequest(Method method, String url, Map<String, String> headers) {
        return sendRequest(method, url, headers,"");
    }

}
