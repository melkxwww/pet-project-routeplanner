package me.melkx.routeplanner.module.route.dto;

import me.melkx.routeplanner.core.Point;
import me.melkx.routeplanner.core.PreProcessingConstraints;
import me.melkx.routeplanner.core.Preferences;
import me.melkx.routeplanner.core.RouteType;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record GenerateCandidatesCommand(RouteType type, Point start, Point end, @Nullable List<Point> waypoints, Preferences preferences, @Nullable PreProcessingConstraints constraints) {
}
