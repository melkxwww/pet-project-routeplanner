package me.melkx.routeplanner.module.route;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;

import java.util.List;
import java.util.Map;

public record Constraints(@Null Map<DistributionAxis, List<String>> distributions, @Null Map<ScalarAxis, @Valid Range> scalars) {
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
