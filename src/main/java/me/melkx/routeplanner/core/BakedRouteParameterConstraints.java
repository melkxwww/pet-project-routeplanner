package me.melkx.routeplanner.core;

import me.melkx.routeplanner.core.property.DistributionProperties;
import me.melkx.routeplanner.core.property.PropertiesValueValidator;
import me.melkx.routeplanner.core.property.ScalarProperties;
import me.melkx.routeplanner.core.property.ScalarRange;

import java.util.List;
import java.util.Map;

public record BakedRouteParameterConstraints(Map<ScalarProperties, ScalarRange> scalars,
                                             Map<DistributionProperties, List<String>> distributions) {
    public BakedRouteParameterConstraints {
        PropertiesValueValidator.validateDistributionValues(distributions);
    }

}
