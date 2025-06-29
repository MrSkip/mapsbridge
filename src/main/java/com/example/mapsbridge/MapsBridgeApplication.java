package com.example.mapsbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties
public class MapsBridgeApplication {

    public static void main(String[] args) {
        // Set the default timezone to UTC for the entire application
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        SpringApplication.run(MapsBridgeApplication.class, args);
    }

}