package me.melkx.routeplanner.module.route;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Null;
import me.melkx.routeplanner.core.*;

import java.util.List;

public record RouteGenerateRequest(@Valid Route route, @Valid Preferences preferences,
                                   @Valid Constraints constraints) {
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "mode",
            visible = true
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = AutoRoute.class, name = "AUTO"),
            @JsonSubTypes.Type(value = ManualRoute.class, name = "MANUAL")
    })
    public sealed interface Route permits AutoRoute, ManualRoute {
        EndpointMode mode();

        RouteType type();

        Point start();

        List<Waypoint> waypoints();
    }

    public record AutoRoute(RouteType type, @Valid Point start, @Valid Target target,
                            @Null List<@Valid Waypoint> waypoints) implements Route {
        @Override
        public EndpointMode mode() {
            return EndpointMode.AUTO;
        }
    }

    public record ManualRoute(RouteType type, @Valid Point start, @Valid Point end,
                              @Null List<@Valid Waypoint> waypoints) implements Route {
        @Override
        public EndpointMode mode() {
            return EndpointMode.MANUAL;
        }
    }

    public record Target(TargetMode mode, Double value, @Min(0) @Max(100) Integer tolerancePct) {
    }

    public record Waypoint(@Valid Point point, Boolean mustVisit) {
    }
}
