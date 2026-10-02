package me.melkx.routeplanner.core;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record Preferences(@Nullable Map<ScalarProperties, @DecimalMin("0") @DecimalMax("1") Double> scalars,
                          @Nullable Map<DistributionProperties, List<@NotBlank String>> distributions) {
}
