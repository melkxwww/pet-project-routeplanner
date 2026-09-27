package me.melkx.routeplanner.module.route;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;

import java.util.List;

public record AutoRoute(RouteType type, @Valid Point start, @Valid Target target, @Null List<@Valid Waypoint> waypoints) implements Route {
    @Override
    public EndpointMode mode() {
        return EndpointMode.AUTO;
    }
}
