package me.melkx.routeplanner.infrastructure.graphhopper;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "veloroute.graphhopper")
public record GHConfigurationProperties(String osmFilePath, String graphCacheDir) {
}
