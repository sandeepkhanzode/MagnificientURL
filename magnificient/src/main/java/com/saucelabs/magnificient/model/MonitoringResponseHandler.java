package com.saucelabs.magnificient.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public class MonitoringResponseHandler {

    private MonitoringStatus monitoringStatus;

    public void setMonitoringStatus(MonitoringStatus monitoringStatus) {
        this.monitoringStatus = monitoringStatus;
    }

    public MonitoringStatus getMonitoringStatus() {
        return monitoringStatus;
    }
}
