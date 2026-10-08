package me.melkx.routeplanner.infrastructure.graphhopper;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import me.melkx.routeplanner.core.GeneratorSettings;
import me.melkx.routeplanner.core.property.DistributionProperties;
import me.melkx.routeplanner.core.property.ScalarProperties;
import me.melkx.routeplanner.core.property.ScalarRange;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class CustomWeighting implements Weighting {
    public static final String NAME = "custom_weighting";

    private static final double MIN_MULTIPLIER = 1.0;
    private static final double DISTRIBUTION_PENALTY = 1.0;
    private static final double BLOCKED_WEIGHT = Double.POSITIVE_INFINITY;
    private static final double NOISE_AMPLITUDE = 0.2;
    private static final long SEED_SALT = 0x9E3779B97F4A7C15L;

    private final List<ScalarEntry> scalars;
    private final List<DistributionEntry> distributions;
    private final int seed;

    public CustomWeighting(BakedRouteParameterEvs routeParameterEvs,
                           GeneratorSettings settings) {
        this.scalars = new ArrayList<>();
        this.distributions = new ArrayList<>();
        this.seed = settings.seed();

        for (ScalarProperties key : routeParameterEvs.scalars().keySet()) {
            var ev = routeParameterEvs.scalars().get(key);
            var preference = Optional.of(settings.routeParameters().scalars().get(key))
                    .orElseThrow(() -> new IllegalArgumentException("routeParameters.scalars.value cannot be null"));
            var constraint = settings.routeParameterConstraints().scalars().get(key);

            scalars.add(new ScalarEntry(ev, preference, constraint));
        }

        for (DistributionProperties key : routeParameterEvs.distributions().keySet()) {
            var ev = routeParameterEvs.distributions().get(key);
            var preferences = Optional.ofNullable(settings.routeParameters().distributions().get(key))
                    .orElseThrow(() -> new IllegalArgumentException("routeParameters.distributions.value cannot be null"));
            var constraints = settings.routeParameterConstraints().distributions().get(key);

            Enum<?>[] linkedEnums = key.getLinkedClass().getEnumConstants();
            Set<Enum<?>> convertedPreferences = new HashSet<>();
            Set<Enum<?>> convertedConstraints = new HashSet<>();

            for (Enum<?> linkedEnum : linkedEnums) {
                if (preferences.contains(linkedEnum.name())) convertedPreferences.add(linkedEnum);
                if (constraints.contains(linkedEnum.name())) convertedConstraints.add(linkedEnum);
            }

            distributions.add(new DistributionEntry(ev, convertedPreferences, convertedConstraints));
        }
    }

    @Override
    public double calcMinWeightPerDistance() {
        return MIN_MULTIPLIER;
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edge, boolean reverse) {
        CalcResult scalarResult = calcScalarParams(edge);
        if (!scalarResult.isAccessible()) {
            return BLOCKED_WEIGHT;
        }

        CalcResult distributionResult = calcDistributionParams(edge);
        if (!distributionResult.isAccessible()) {
            return BLOCKED_WEIGHT;
        }

        double base = MIN_MULTIPLIER + scalarResult.weight() + distributionResult.weight();
        double noise = 1.0 + NOISE_AMPLITUDE * edgeNoise(edge);
        return edge.getDistance() * base * noise;
    }

    private CalcResult calcScalarParams(EdgeIteratorState edge) {
        double result = 0;
        for (ScalarEntry entry : scalars) {
            double currentValue = edge.get(entry.encodedValue());

            ScalarRange constraint = entry.constraint();
            if (constraint != null
                    && (currentValue > constraint.max() || currentValue < constraint.min())) {
                return CalcResult.blocked();
            }

            result += Math.abs(currentValue - entry.preference());
        }
        return CalcResult.accessible(result);
    }

    private CalcResult calcDistributionParams(EdgeIteratorState edge) {
        double result = 0;
        for (DistributionEntry entry : distributions) {
            Enum<?> currentValue = edge.get(entry.encodedValue());

            if (!entry.constraints().isEmpty() && entry.constraints().contains(currentValue)) {
                return CalcResult.blocked();
            }

            if (!entry.preferences().contains(currentValue)) {
                result += DISTRIBUTION_PENALTY;
            }
        }
        return CalcResult.accessible(result);
    }

    private double edgeNoise(EdgeIteratorState edge) {
        long key = edge.getEdge();
        long mixed = mix64(key ^ ((long) seed * SEED_SALT));
        return (mixed >>> 11) * 0x1.0p-53;
    }

    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    @Override
    public long calcEdgeMillis(EdgeIteratorState edge, boolean reverse) {
        return 0;
    }

    @Override
    public double calcTurnWeight(int inEdge, int viaNode, int outEdge) {
        return 0;
    }

    @Override
    public long calcTurnMillis(int inEdge, int viaNode, int outEdge) {
        return 0;
    }

    @Override
    public boolean hasTurnCosts() {
        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public record BakedRouteParameterEvs(Map<ScalarProperties, DecimalEncodedValue> scalars,
                                         Map<DistributionProperties, EnumEncodedValue<?>> distributions) {
    }

    private record CalcResult(boolean isAccessible, double weight) {
        public static CalcResult accessible(double weight) {
            return new CalcResult(true, weight);
        }

        public static CalcResult blocked() {
            return new CalcResult(false, BLOCKED_WEIGHT);
        }
    }

    private record ScalarEntry(DecimalEncodedValue encodedValue,
                               double preference,
                               @Nullable ScalarRange constraint) {
    }

    private record DistributionEntry(EnumEncodedValue<?> encodedValue,
                                     Set<Enum<?>> preferences,
                                     Set<Enum<?>> constraints) {
    }
}