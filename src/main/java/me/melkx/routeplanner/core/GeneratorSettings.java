package me.melkx.routeplanner.core;

import org.jspecify.annotations.Nullable;

import java.util.Random;

public record GeneratorSettings(BakedRouteParameters routeParameters, @Nullable BakedRouteParameterConstraints routeParameterConstraints, int seed) {
}
