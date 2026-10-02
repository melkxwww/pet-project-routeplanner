package me.melkx.routeplanner.core;

import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum DistributionProperties {
    SURFACE_TYPE(SurfaceType.class);

    private final Class<? extends Enum<?>> linkedClass;
    private final Set<String> loweredNames;

    DistributionProperties(Class<? extends Enum<?>> linkedClass) {
        this.linkedClass = linkedClass;
        this.loweredNames = Arrays.stream(linkedClass.getEnumConstants())
                .map(e -> e.name().toLowerCase())
                .collect(Collectors.toUnmodifiableSet());
    }
}