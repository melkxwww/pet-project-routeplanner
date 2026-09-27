package me.melkx.routeplanner.module.route;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record Range(@DecimalMin("0") Double min, @DecimalMax("1") Double max) {
    public Range {
        if(min > max)
            throw new IllegalArgumentException("Min '" + min + "' cannot be greater than Max '" + max  + "'");
    }
}
