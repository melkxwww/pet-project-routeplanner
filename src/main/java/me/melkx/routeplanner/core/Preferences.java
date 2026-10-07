package me.melkx.routeplanner.core;

import me.melkx.routeplanner.core.property.DistributionProperties;
import me.melkx.routeplanner.core.property.PropertiesValueValidator;
import me.melkx.routeplanner.core.property.ScalarProperties;

import java.util.List;
import java.util.Map;

public record Preferences(Map<ScalarProperties, Double> scalars,
                          Map<DistributionProperties, List<String>> distributions) {
    public Preferences {
        PropertiesValueValidator.validateScalarValues(scalars.values());
        PropertiesValueValidator.validateDistributionValues(distributions);
    }
}
