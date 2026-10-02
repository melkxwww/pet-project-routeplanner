package me.melkx.routeplanner.core;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record Point(
        @DecimalMin("-90") @DecimalMax("90") Double lat,
        @DecimalMin("-180") @DecimalMax("180") Double lon
) {
}
