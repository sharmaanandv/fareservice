package com.sma.fareservice.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties ()
@Getter
@Setter
public class PeakHoursConfig {

    private Map<String, List<String>> peakHours;

}
