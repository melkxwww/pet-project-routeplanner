package me.melkx.routeplanner.core;

import me.melkx.routeplanner.core.property.Distributions;
import me.melkx.routeplanner.core.property.ScalarDouble;
import me.melkx.routeplanner.core.property.Scalars;

public record Preferences(Scalars<ScalarDouble> scalars,
                          Distributions distributions) {
}
