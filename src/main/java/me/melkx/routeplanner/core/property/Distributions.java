package me.melkx.routeplanner.core.property;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Distributions {
    private final Map<DistributionProperties, List<String>> values;

    private Distributions(Map<DistributionProperties, List<String>> values) {
        this.values = Map.copyOf(values);
    }

    public static Distributions of(Map<DistributionProperties, List<String>> values) {
        if (values == null) {
            throw new IllegalArgumentException("distributions must not be null");
        }
        for (var entry : values.entrySet()) {
            DistributionProperties key = entry.getKey();
            List<String> list = entry.getValue();

            if (list == null) {
                throw new IllegalArgumentException("distributions." + key + " must not be null");
            }

            List<String> loweredNames = new ArrayList<>(key.getLoweredNames());
            for (String v : list) {
                if (v == null) {
                    throw new IllegalArgumentException(
                            "Invalid null value for " + key);
                }
                String needle = v.toLowerCase();
                if (loweredNames.stream().noneMatch(n -> n.contains(needle))) {
                    throw new IllegalArgumentException(
                            "Invalid value '" + v + "' for " + key);
                }
            }
        }

        Map<DistributionProperties, List<String>> copy = new LinkedHashMap<>();
        values.forEach((k, v) -> copy.put(k, List.copyOf(v)));
        return new Distributions(copy);
    }

    @JsonValue
    public Map<DistributionProperties, List<String>> values() {
        return values;
    }

    @JsonCreator
    public static Distributions fromJson(Map<DistributionProperties, List<String>> values) {
        return of(values);
    }

    public List<String> get(DistributionProperties key) {
        return values.getOrDefault(key, List.of());
    }
}
