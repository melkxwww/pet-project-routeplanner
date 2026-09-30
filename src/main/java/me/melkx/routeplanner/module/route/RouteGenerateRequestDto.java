package me.melkx.routeplanner.module.route;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import me.melkx.routeplanner.core.DistributionAxis;
import me.melkx.routeplanner.core.ScalarAxis;

import java.util.List;
import java.util.Map;

public record RouteGenerateRequestDto(@Valid Route route, @Valid Constraints constraints) {
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

    public record Constraints(@Null Map<DistributionAxis, List<String>> distributions,
                              @Null Map<ScalarAxis, @Valid Range> scalars) {
        public Constraints {
            for (var entry : distributions.entrySet()) {
                var names = entry.getKey().getLoweredNames();
                for (String v : entry.getValue()) {
                    String needle = v.toLowerCase();
                    if (names.stream().noneMatch(n -> n.contains(needle))) {
                        throw new IllegalArgumentException(
                                "Invalid value '" + v + "' for " + entry.getKey());
                    }
                }
            }
        }

        public record Range(@DecimalMin("0") Double min, @DecimalMax("1") Double max) {
            public Range {
                if (min > max)
                    throw new IllegalArgumentException("Min '" + min + "' cannot be greater than Max '" + max + "'");
            }
        }
    }

    public record Target(TargetMode mode, Double value, @Min(0) @Max(100) Integer tolerancePct) {
        public enum TargetMode {
            DISTANCE,
            DURATION
        }
    }

    public record Point(
            @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @DecimalMin("-180") @DecimalMax("180") Double longitude
    ) {
    }

    public record Waypoint(@Valid Point point, Boolean mustVisit) {
    }

    public enum RouteType {
        P2P,
        LOOP
    }

    public enum EndpointMode {
        AUTO,
        MANUAL
    }
}
