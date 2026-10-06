package me.melkx.routeplanner.infrastructure.graphhopper;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import me.melkx.routeplanner.core.property.DistributionProperties;
import me.melkx.routeplanner.core.property.ScalarProperties;
import me.melkx.routeplanner.module.route.PreProcessingConstraints;
import me.melkx.routeplanner.module.route.Preferences;
import me.melkx.routeplanner.module.route.Range;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CustomWeighting implements Weighting {
    public static final String NAME = "custom_weighting";

    private static final double MIN_MULTIPLIER = 1.0;
    private static final double DISTRIBUTION_PENALTY = 1.0;
    private static final double BLOCKED_WEIGHT = Double.POSITIVE_INFINITY;

    private final List<ScalarEntry> scalars;
    private final List<DistributionEntry> distributions;

    public CustomWeighting(EncodedValues encodedValues,
                           Preferences preferences,
                           PreProcessingConstraints constraints) {
        Map<ScalarProperties, Double> scalarPreferences = preferences.scalars();
        Map<DistributionProperties, List<String>> distributionPreferences = preferences.distributions();
        Map<ScalarProperties, Range> scalarConstraints = constraints.scalars();
        Map<DistributionProperties, List<String>> distributionConstraints = constraints.distributions();

        this.scalars = (scalarPreferences == null) ? List.of() :
                encodedValues.scalars().entrySet().stream()
                        .map(e -> new ScalarEntry(
                                e.getValue(),
                                require(scalarPreferences, e.getKey(), "preferences.scalars"),
                                getOrNull(scalarConstraints, e.getKey())
                        )).toList();

        this.distributions = (distributionPreferences == null) ? List.of() :
                encodedValues.distributions().entrySet().stream()
                        .map(e -> {
                            DistributionProperties key = e.getKey();
                            EnumEncodedValue<?> encodedValue = e.getValue();

                            List<String> rawConstraints = getOrNull(distributionConstraints, key);
                            Set<Enum<?>> resolvedConstraints =
                                    (rawConstraints == null || rawConstraints.isEmpty())
                                            ? Set.of()
                                            : resolveEnumSet(encodedValue, rawConstraints,
                                            "constraints.distributions[" + key + "]");

                            return new DistributionEntry(
                                    encodedValue,
                                    resolveEnumSet(
                                            encodedValue,
                                            require(distributionPreferences, key, "preferences.distributions"),
                                            "preferences.distributions[" + key + "]"),
                                    resolvedConstraints);
                        })
                        .toList();
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

        return edge.getDistance()
                * (MIN_MULTIPLIER + scalarResult.weight() + distributionResult.weight());
    }

    private CalcResult calcScalarParams(EdgeIteratorState edge) {
        double result = 0;
        for (ScalarEntry entry : scalars) {
            double currentValue = edge.get(entry.encodedValue());

            Range constraint = entry.constraint();
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

    private static <K, V> V require(Map<K, V> map, K key, String mapName) {
        V value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException(mapName + " is missing key: " + key);
        }
        return value;
    }

    @Nullable
    private static <K, V> V getOrNull(@Nullable Map<K, V> map, K key) {
        return map == null ? null : map.get(key);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Set<Enum<?>> resolveEnumSet(EnumEncodedValue<?> encodedValue,
                                               List<String> rawValues,
                                               String source) {
        if (rawValues.isEmpty()) {
            throw new IllegalArgumentException(source + " must not be empty");
        }

        Enum<?>[] constants = encodedValue.getValues();
        Map<String, Enum<?>> byName = new EnumMap(constants[0].getDeclaringClass());
        for (Enum<?> c : constants) {
            byName.put(c.name().toLowerCase(Locale.ROOT), c);
        }

        return rawValues.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .map(s -> {
                    Enum<?> e = byName.get(s);
                    if (e == null) {
                        throw new IllegalArgumentException(
                                "Unknown value '" + s + "' in " + source
                                        + " for " + encodedValue.getName()
                                        + ". Allowed: " + byName.keySet());
                    }
                    return e;
                })
                .collect(Collectors.toUnmodifiableSet());
    }

    public record EncodedValues(Map<ScalarProperties, DecimalEncodedValue> scalars,
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
                               @Nullable Range constraint) {
    }

    private record DistributionEntry(EnumEncodedValue<?> encodedValue,
                                     Set<Enum<?>> preferences,
                                     Set<Enum<?>> constraints) {
    }
}