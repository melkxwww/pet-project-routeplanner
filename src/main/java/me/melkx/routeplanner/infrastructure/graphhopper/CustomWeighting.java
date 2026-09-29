package me.melkx.routeplanner.infrastructure.graphhopper;

import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import me.melkx.routeplanner.module.route.DistributionAxis;
import me.melkx.routeplanner.module.route.ScalarAxis;
import me.melkx.routeplanner.module.route.SurfaceType;

import java.util.*;
import java.util.stream.Collectors;

public class CustomWeighting implements Weighting {
    public static final String NAME = "custom_weighting";

    private static final double MIN_MULTIPLIER = 1;
    private static final double DISTRIBUTION_PENALTY = 1;

    private final List<ScalarEntry> scalars;
    private final List<DistributionEntry> distributions;

    public CustomWeighting(
            Map<ScalarAxis, DecimalEncodedValue> scalarEncodedValues,
            Map<DistributionAxis, EnumEncodedValue<?>> distributionEncodedValues,
            Map<ScalarAxis, Double> scalarPreferences,
            Map<DistributionAxis, List<String>> distributionPreferences) {

        this.scalars = scalarEncodedValues.entrySet().stream()
                .map(e -> new ScalarEntry(
                        e.getValue(),
                        require(scalarPreferences, e.getKey(), "scalarPreferences")))
                .toList();

        this.distributions = distributionEncodedValues.entrySet().stream()
                .map(e -> new DistributionEntry(
                        e.getValue(),
                        resolvePreferences(e.getValue(), require(distributionPreferences, e.getKey(), "distributionPreferences"))))
                .toList();
    }

    @Override
    public double calcMinWeightPerDistance() {
        return MIN_MULTIPLIER;
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edge, boolean reverse) {
        double multiplier = MIN_MULTIPLIER
                + calcScalarParams(edge)
                + calcDistributionParams(edge);
        return edge.getDistance() * multiplier;
    }

    private double calcScalarParams(EdgeIteratorState edge) {
        double result = 0;
        for (ScalarEntry entry : scalars) {
            result += Math.abs(edge.get(entry.encodedValue()) - entry.preference());
        }
        return result;
    }

    private double calcDistributionParams(EdgeIteratorState edge) {
        double result = 0;
        for (DistributionEntry entry : distributions) {
            if (!entry.preferences().contains(edge.get(entry.encodedValue()))) {
                result += DISTRIBUTION_PENALTY;
            }
        }
        SurfaceType.
        return result;
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

    // --- helpers ---

    private static <K, V> V require(Map<K, V> map, K key, String name) {
        V value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException(name + " is missing key: " + key);
        }
        return value;
    }

    /**
     * Превращает список строк из preferences в Set<Enum<?>>, сравнивая с реальными константами
     * переданного EnumEncodedValue. Один раз при создании — потом в горячем пути только Enum.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Set<Enum<?>> resolvePreferences(EnumEncodedValue<?> encodedValue, List<String> rawPreferences) {
        if (rawPreferences == null || rawPreferences.isEmpty()) {
            throw new IllegalArgumentException("distributionPreferences must not be empty");
        }

        Enum<?>[] constants = encodedValue.getValues();
        Map<String, Enum<?>> byName = new EnumMap(constants[0].getDeclaringClass());
        for (Enum<?> c : constants) {
            byName.put(c.name().toLowerCase(Locale.ROOT), c);
        }

        return rawPreferences.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .map(s -> {
                    Enum<?> e = byName.get(s);
                    if (e == null) {
                        throw new IllegalArgumentException(
                                "Unknown value '" + s + "' for " + encodedValue.getName()
                                        + ". Allowed: " + byName.keySet());
                    }
                    return e;
                })
                .collect(Collectors.toUnmodifiableSet());
    }

    private record ScalarEntry(DecimalEncodedValue encodedValue, double preference) {
    }

    private record DistributionEntry(EnumEncodedValue<?> encodedValue, Set<Enum<?>> preferences) {
    }
}