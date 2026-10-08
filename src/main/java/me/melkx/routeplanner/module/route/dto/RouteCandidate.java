package me.melkx.routeplanner.module.route.dto;

import me.melkx.routeplanner.core.Point;
import me.melkx.routeplanner.core.property.DistributionProperties;
import me.melkx.routeplanner.core.property.ScalarProperties;

import java.util.List;
import java.util.Map;

public record RouteCandidate(List<Point> path,
                             double distanceM,
                             double durationS,
                             Map<ScalarProperties, Double> matchedScalars,
                             Map<DistributionProperties, Double> matchedDistributions) {
}
