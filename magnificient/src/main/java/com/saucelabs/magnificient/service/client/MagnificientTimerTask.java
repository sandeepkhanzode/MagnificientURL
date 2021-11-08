package com.saucelabs.magnificient.service.client;

import com.saucelabs.magnificient.model.MonitoringResponseHandler;
import com.saucelabs.magnificient.model.MonitoringStatus;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class MagnificientTimerTask extends TimerTask {
    private static final Logger logger = LogManager.getLogger(MagnificientTimerTask.class);
    public static final double THRESHOLD = 0.1;

    private final String magnificientUrlPath;
    private final int magnificientUrlBacklog;
    private WebClient client;
    private final Queue<MonitoringStatus> queue;

    @Autowired
    private MonitoringResponseHandler monitoringResponseHandler;

    public MagnificientTimerTask(@Value("${magnificient.url.path}") String magnificientUrlPath,
                 @Value("${magnificient.url.backlog}") int magnificientUrlBacklog) {
        this.magnificientUrlPath = magnificientUrlPath;
        this.magnificientUrlBacklog = magnificientUrlBacklog;
        this.queue = new ArrayBlockingQueue<MonitoringStatus>(magnificientUrlBacklog);

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000))
                .doOnConnected(connection ->
                        connection
                                .addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

        client = WebClient.builder()
                .baseUrl(magnificientUrlPath)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public void run() {
        MonitoringStatus monitoringStatus = invoke();
        logger.atInfo().log("From Timer: " + monitoringStatus);
        monitoringResponseHandler.setMonitoringStatus(computeStatus(monitoringStatus));
    }

    private MonitoringStatus computeStatus(MonitoringStatus monitoringStatus) {
        if(!queue.offer(monitoringStatus)) {
            queue.remove();
        }
        if(queue.size() >= magnificientUrlBacklog) {
            queue.remove();
        }
        logger.atInfo().log("Queue Size: " + queue.size());

        AtomicInteger flakyCount = new AtomicInteger(0);
        AtomicInteger healthyCount = new AtomicInteger(0);

        queue.forEach(status -> {
            logger.atInfo().log("Queue Status: " + status);
            switch (status) {
                case FLAKY:
                    flakyCount.getAndIncrement(); break;
                case HEALTHY:
                    healthyCount.getAndIncrement(); break;
            }
        });

        logger.atInfo().log("flakyCount.get(): " + flakyCount.get() + ", magnificientUrlBacklog * 0.1: " + (int) magnificientUrlBacklog * THRESHOLD);
        return flakyCount.get() >= (int) magnificientUrlBacklog * THRESHOLD ? MonitoringStatus.FLAKY : MonitoringStatus.HEALTHY;
    }

    public MonitoringStatus invoke() {
        return client.get().exchangeToMono(response -> {
            if (response.statusCode().equals(HttpStatus.OK)) {
                return Mono.just(MonitoringStatus.HEALTHY);
            } else if (response.statusCode().is5xxServerError()) {
                return Mono.just(MonitoringStatus.FLAKY);
            } else {
                return Mono.just(MonitoringStatus.UNRESPONSIVE);
            }
        }).block();
    }

}
