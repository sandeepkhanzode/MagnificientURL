package com.saucelabs.magnificient.rest;

import com.saucelabs.magnificient.model.MonitoringStatus;
import com.saucelabs.magnificient.service.MonitoringClientService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class MagnificientMonitoringController {

    private static final Logger logger = LogManager.getLogger(MagnificientMonitoringController.class);

    @Autowired
    private MonitoringClientService monitoringClientService;

    @GetMapping("/status")
    public Mono<MonitoringStatus> status() {
        return monitoringClientService.fetchStatus();
    }
}
