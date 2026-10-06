package me.melkx.routeplanner.core;

import me.melkx.routeplanner.core.property.Distributions;
import me.melkx.routeplanner.core.property.ScalarRange;
import me.melkx.routeplanner.core.property.Scalars;

public record PreProcessingConstraints(Scalars<ScalarRange> scalars,
                                       Distributions distributions) {
}
