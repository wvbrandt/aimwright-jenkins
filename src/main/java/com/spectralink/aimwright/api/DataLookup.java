package com.spectralink.aimwright.api;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spectralink.aimwright.common.Session;
import com.spectralink.aimwright.common.Settings;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.spectralink.aimwright.api.ApiClient.Method.GET;

public class DataLookup {

    private static final Logger log = (Logger) LoggerFactory.getLogger(DataLookup.class.getName());

    public static String getAccountId(String literalName) {
        // TODO: validate the functionality of getAccountId()
        ApiResponse accountRequest = Session.getAmieApiClient().sendGetRequest("accounts?sortField=accountName&sortOrder=ASC");
        ObjectNode json = accountRequest.getJsonObjectBody();
        JsonNode data = json.get("data");
        log.debug("account result = {}", json);
        String accountId = "";
        for (JsonNode node : data) {
            String accountName = node.get("accountName").asText();
            if (accountName.equalsIgnoreCase(literalName)) {
                accountId = node.get("accountId").asText().toLowerCase();
            }
        }
        if (accountId.isEmpty()) {
            log.error("No account ID was found for '{}'", literalName);
        }
        return accountId;
    }

    public static String getOrganizationId(String literalName) {
        String orgTarget = "";
        ArrayNode organizations = Session.getAmieApiClient().sendGetRequest("/organizations/options?accountId=").getJsonArrayBody();
        for (JsonNode entry : organizations) {
            if (entry.get("text").asText().equalsIgnoreCase(literalName)) orgTarget = entry.get("value").asText();
        }
        if (orgTarget.isEmpty()) log.error("could not find an org with : {}", literalName);
        return orgTarget;
    }

    public static String getLocationId(String literalName) {
        ApiResponse locationRequest = Session.getAmieApiClient().sendGetRequest("locations/options?organizationId=");
        log.trace("Found locations: {}", locationRequest.toString());
        ArrayNode json = locationRequest.getJsonArrayBody();
        String locationId = "";
        for (JsonNode node : json) {
            if (node.get("text").asText().equalsIgnoreCase(literalName)) {
                locationId = node.get("value").asText().toLowerCase();
            }
        }
        if (locationId.isEmpty()) {
            log.error("No location ID was found for '{}'", literalName);
        }
        return locationId;
    }

    public static String getLocationIds(List<String> literalLocations) {
        Session.setLocationIds("");
        ApiResponse locationRequest = Session.getAmieApiClient().sendGetRequest("locations/dropdown-list");
        ArrayNode json = locationRequest.getJsonArrayBody();
        List<String> locationIds = new ArrayList<>();
        for (String eachLocation : literalLocations) {
            for (JsonNode node : json) {
                if (node.get("text").asText().equalsIgnoreCase(eachLocation)) {
                    locationIds.add(node.get("value").asText().toLowerCase());
                }
            }
        }
        if (locationIds.isEmpty()) {
            log.error("No location ID's were found for '{}'", literalLocations);
        }
        return String.join(",", locationIds);
    }

    public static String getLocationIds() {
        if (!Session.isCredentialsSet()) {
            Session.setCredentials(Settings.get("target.user"), Settings.get("target.password"));
        }
        List<String> locationIds = new ArrayList<>();
        ArrayNode responseJsonArray = Session.getAmieApiClient().sendRequest(GET, "locations/dropdown-list").getJsonArrayBody();
        for (JsonNode entry : responseJsonArray) {
            locationIds.add(entry.get("value").asText().toLowerCase());
        }
        return String.join(",", locationIds);
    }

    public static List<String> getDeviceSerials() {
        ArrayList<String> deviceSerials = new ArrayList<>();
//        Session.setLocationIds(Settings.getLocationId());
        JsonNode data = Session.getAmieApiClient().sendRequest(GET, "devicemgt/?start=0&length=50&sortField=lastCheckInTime&sortOrder=DESC").getJsonObjectBody();
        for (JsonNode device : data.get("data")){
            deviceSerials.add(device.get("serial").asText());
        }
        return deviceSerials;
    }

    public static List<String> getDeviceSerialsForSpecificLocation(String location) {
        ArrayList<String> deviceSerials = new ArrayList<>();
        JsonNode data = Session.getAmieApiClient().sendRequest(GET, "devicemgt/?start=0&length=50&sortField=lastCheckInTime&sortOrder=DESC").getJsonObjectBody();

        for (JsonNode device : data.get("data")) {
            if (device.get("location").asText().equals(location))
                deviceSerials.add(device.get("serial").asText());
        }
        return deviceSerials;
    }

    public static List<String> getDeviceCallIds(String deviceSerial) {
        ArrayList<String> callIds = new ArrayList<>();
        JsonNode data = Session.getAmieApiClient().sendRequest(GET, "/devices/" + deviceSerial + "/call-histories?start=0&length=50&sortField=&sortOrder=").getJsonObjectBody();
        for (JsonNode calls : data.get("data")) {
            callIds.add(calls.get("id").asText());
        }
        return callIds;
    }

    public static JsonNode getDeviceCalls(String deviceSerial) {
        String endPoint = "devicemgt/call/history/" + deviceSerial + "?start=0&length=50";
        ApiResponse calls = Session.getAmieApiClient().sendGetRequest(endPoint);
        ObjectNode summary = calls.getJsonObjectBody();
        return summary.get("data");
    }

    public static ObjectNode getCallPerformance() {
        ApiResponse summary = Session.getAmieApiClient().sendGetRequest("performance/call-performance");
        return summary.getJsonObjectBody();
    }

    public ArrayNode getLocations() {
        ApiResponse locationRequest = Session.getAmieApiClient().sendGetRequest("locations/dropdown-list");
        return locationRequest.getJsonArrayBody();
    }

    public List<String> getBatterySerials() {
        ArrayList<String> batterySerials = new ArrayList<>();
        JsonNode data = Session.getAmieApiClient().sendRequest(GET, "batteries/?start=0&length=50&sortField=lastCheckInTime&sortOrder=DESC").getJsonObjectBody();
        for (JsonNode battery : data.get("data")) {
            batterySerials.add(battery.get("batterySerial").asText());
        }
        return batterySerials;
    }

    private static void setApiParameters(String organization, String location) {
        String targetOrganization = DataLookup.getOrganizationId(organization);
        Session.setOrganization(targetOrganization);
        String targetLocation = DataLookup.getLocationId(location);
        Session.setLocationIds(targetLocation);
    }

    public static JsonNode getGatewaySummary(String organization, String location, String gatewayName) {
        setApiParameters(organization, location);
        ApiResponse gatewayQuery = Session.getAmieApiClient().sendRequest(GET, "/api/administration/locations/gateway-summary?start=0&length=50&sortField=gateway_name&sortOrder=ASC");
        JsonNode foundGateway = null;
        if (gatewayQuery.getResponseCode() == 200) {
            ArrayNode gatewayList = (ArrayNode) gatewayQuery.getJsonObjectBody().get("data");
            log.trace(gatewayList.toPrettyString());
            for (JsonNode eachGateway : gatewayList) {
                if (eachGateway.get("gateway_name").asText().contentEquals(gatewayName)) {
                    foundGateway = eachGateway;
                    break;
                }
            }
        } else {
            log.error("Gateway summary for '{}' could not be obtained {}: {}", gatewayName, gatewayQuery.getResponseCode(), gatewayQuery.getJsonObjectBody().get("message"));
        }
        return foundGateway;
    }

    public static JsonNode getGatewayDetail(String organization, String location, String gatewayName) {
        setApiParameters(organization, location);
        JsonNode specificGatewayInfo = null;
        JsonNode targetGateway = getGatewaySummary(organization, location, gatewayName);
        if (targetGateway != null) {
            ApiResponse gatewayInfo = Session.getAmieApiClient().sendRequest(GET, "api/administration/locations/gateway/" + targetGateway.get("gateway_id").asText());
            if (gatewayInfo.getResponseCode() == 200) {
                specificGatewayInfo = gatewayInfo.getJsonObjectBody();
                log.trace(specificGatewayInfo.toPrettyString());
            } else {
                log.error("Gateway detail for '{}' could not be obtained {}: {}", gatewayName, gatewayInfo.getResponseCode(), gatewayInfo.getJsonObjectBody().get("message"));
            }
        } else {
            log.error("Cannot get detail for '{}'", gatewayName);
        }
        return specificGatewayInfo;
    }
}
