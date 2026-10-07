package me.melkx.routeplanner.core.property;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class PropertiesValueValidator {
    private static final double MIN_SCALAR_VALUE = 0.0;
    private static final double MAX_SCALAR_VALUE = 1.0;

    public static void validateScalarValue(double value) {
        if (value < MIN_SCALAR_VALUE || value > MAX_SCALAR_VALUE) {
            throw new IllegalArgumentException(
                    "scalar value must be between " + MIN_SCALAR_VALUE + " and " + MAX_SCALAR_VALUE + " but was " + value);
        }
    }

    public static void validateDistributionValue(DistributionProperties property, List<String> value) {
        for (String v : value)
            if (property.getLoweredNames().stream().noneMatch(n -> n.contains(v.toLowerCase()))) {
                throw new IllegalArgumentException(
                        "Invalid value '" + v + "' for " + property.name());
            }
    }

    public static void validateDistributionValues(Map<DistributionProperties, List<String>> values) {
        values.forEach(PropertiesValueValidator::validateDistributionValue);
    }

    public static void validateScalarValues(Collection<Double> values) {
        values.forEach(PropertiesValueValidator::validateScalarValue);
    }
}
