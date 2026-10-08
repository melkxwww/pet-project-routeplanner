package me.melkx.routeplanner.module.route.dto;

import me.melkx.routeplanner.core.Point;
import me.melkx.routeplanner.core.BakedRouteParameterConstraints;
import me.melkx.routeplanner.core.BakedRouteParameters;
import me.melkx.routeplanner.core.RouteType;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record GenerateCandidatesCommand(RouteType routeType,
                                        Point start,
                                        Point end,
                                        List<Point> waypoints,
                                        BakedRouteParameters routeParameters,
                                        @Nullable BakedRouteParameterConstraints routeParameterConstraints,
                                        int candidateCount) {
}
