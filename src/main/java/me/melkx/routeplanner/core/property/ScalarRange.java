package me.melkx.routeplanner.core.property;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record ScalarRange(double min, double max) {
    @JsonCreator
    public ScalarRange {
        PropertiesValueValidator.validateScalarValue(min);
        PropertiesValueValidator.validateScalarValue(max);

        if (min > max) {
            throw new IllegalArgumentException(
                    "range min must be <= max but was [" + min + ", " + max + "]");
        }
    }

    @Override
    @JsonValue
    public String toString() {
        return "[" + min + "," + max + "]";
    }
}
