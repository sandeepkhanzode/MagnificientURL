package com.saucelabs.magnificient.service.client;

import com.saucelabs.magnificient.model.MonitoringResponseHandler;
import com.saucelabs.magnificient.model.MonitoringStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Timer;

@Component
public class MagnificientServiceClient {
    private static final Logger logger = LogManager.getLogger(MagnificientServiceClient.class);

    private MonitoringStatus monitoringStatus;

    @Autowired
    private MonitoringResponseHandler monitoringResponseHandler;

    @Autowired
    private MagnificientTimerTask magnificientTimerTask;

    @PostConstruct
    public void init() {
        new Timer().scheduleAtFixedRate(magnificientTimerTask, 0, 6000);
    }

    /**
     *
     * @return MonitoringStatus
     */
    public Mono<MonitoringStatus> getStatus() {
        return Mono.just(monitoringResponseHandler.getMonitoringStatus());
    }
}
