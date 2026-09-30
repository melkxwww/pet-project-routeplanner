package me.melkx.routeplanner.core;

import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum DistributionAxis {
    SURFACE_TYPE(SurfaceType.class);

    private final Set<String> loweredNames;

    DistributionAxis(Class<? extends Enum<?>> variants) {
        this.loweredNames = Arrays.stream(variants.getEnumConstants())
                .map(e -> e.name().toLowerCase())
                .collect(Collectors.toUnmodifiableSet());
    }
}