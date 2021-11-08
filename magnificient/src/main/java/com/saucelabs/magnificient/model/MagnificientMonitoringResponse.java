package com.saucelabs.magnificient.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MagnificientMonitoringResponse {
    private MonitoringStatus monitoringStatus;
    private JsonNode additionalDetails;
}
