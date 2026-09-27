package me.melkx.routeplanner.module.route;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;

import java.util.List;

public record ManualRoute(RouteType type, @Valid Point start, @Valid Point end, @Null List<@Valid Waypoint> waypoints) implements Route {
    @Override
    public EndpointMode mode() {
        return EndpointMode.MANUAL;
    }
}
