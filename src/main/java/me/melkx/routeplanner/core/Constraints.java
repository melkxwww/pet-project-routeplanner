package me.melkx.routeplanner.core;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record Constraints(@Nullable Map<ScalarProperties, @Valid Range> scalars,
                          @Nullable Map<DistributionProperties, List<@NotBlank String>> distributions) {
    public Constraints {
        for (var entry : distributions.entrySet()) {
            var names = entry.getKey().getLoweredNames();
            for (String v : entry.getValue()) {
                String needle = v.toLowerCase();
                if (names.stream().noneMatch(n -> n.contains(needle))) {
                    throw new IllegalArgumentException(
                            "Invalid value '" + v + "' for " + entry.getKey());
                }
            }
        }
    }
}
