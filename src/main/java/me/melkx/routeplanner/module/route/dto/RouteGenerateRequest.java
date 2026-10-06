package me.melkx.routeplanner.module.route.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import me.melkx.routeplanner.core.*;
import me.melkx.routeplanner.core.property.DistributionProperties;
import me.melkx.routeplanner.core.property.ScalarProperties;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record RouteGenerateRequest(@Valid Route route, @Null @Valid Preferences preferences,
                                   @Null @Valid Constraints constraints) {
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

        List<Point> waypoints();
    }

    public record AutoRoute(RouteType type, @Valid Point start, Double targetDistance,
                            @Null List<@Valid Point> waypoints) implements Route {
        @Override
        public EndpointMode mode() {
            return EndpointMode.AUTO;
        }
    }

    public record ManualRoute(RouteType type, @Valid Point start, @Valid Point end,
                              @Null List<@Valid Point> waypoints) implements Route {
        @Override
        public EndpointMode mode() {
            return EndpointMode.MANUAL;
        }
    }

    public record Constraints(@Null PreProcessingConstraints preProcessing,
                              @Null PostProcessingConstraints postProcessing) {
    }
}
