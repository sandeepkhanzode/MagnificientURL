package com.saucelabs.magnificient.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class MonitoringConfig {

    @Value("${magnificient.url.path}")
    private String magnificientUrlPath;
}
