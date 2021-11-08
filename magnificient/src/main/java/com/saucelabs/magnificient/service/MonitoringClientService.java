package com.saucelabs.magnificient.service;

import com.saucelabs.magnificient.model.MonitoringStatus;
import com.saucelabs.magnificient.service.client.MagnificientServiceClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MonitoringClientService {

    private static final Logger logger = LogManager.getLogger(MonitoringClientService.class);

    @Autowired
    private MagnificientServiceClient magnificientServiceClient;

    /**
     *
     * @return MagnificientMonitoringResponse
     */
    public Mono<MonitoringStatus> fetchStatus() {
        return magnificientServiceClient.getStatus();
    }
}
