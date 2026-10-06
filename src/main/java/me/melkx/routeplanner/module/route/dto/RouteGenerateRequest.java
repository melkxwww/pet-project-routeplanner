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

    private final Double min;

    private final Double max;

    private final Map<DistributionProperties, List<String>> distributions;

    public RouteGenerateRequest(Double min,
                                Double max,
                                Map<DistributionProperties, List<String>> distributions) {
        this.min = min;
        this.max = max;
        this.distributions = distributions;
    }

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

    public record Constraints(@Valid @Null PreProcessingConstraints preProcessing, @Valid @Null PostProcessingConstraints postProcessing) {
    }

    public record Point(
            @DecimalMin("-90") @DecimalMax("90") Double lat,
            @DecimalMin("-180") @DecimalMax("180") Double lon
    ) {
    }

    public record Preferences(@Nullable @Null Map<ScalarProperties, @DecimalMin("0") @DecimalMax("1") Double> scalars,
                              @Nullable @Null Map<DistributionProperties, List<@NotBlank String>> distributions) {
    }

    public record Range(@DecimalMin("0") Double min, @DecimalMax("1") Double max) {
        public Range {
            if (min > max)
                throw new IllegalArgumentException("Min '" + min + "' cannot be greater than Max '" + max + "'");
        }
    }

    public record PreProcessingConstraints(@Null @Nullable Map<ScalarProperties, @Valid Range> scalars,
                                           @Null @Nullable Map<DistributionProperties, List<@NotBlank String>> distributions) {
        public PreProcessingConstraints {
            if (distributions != null)
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
    }

    public record PostProcessingConstraints(@Null @Valid Range durationS, @Null @Valid Range distanceM) {
    }
}
