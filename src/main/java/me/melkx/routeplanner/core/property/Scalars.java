package me.melkx.routeplanner.core.property;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;

public final class Scalars<T extends ScalarValue> {

    private final Map<ScalarProperties, T> values;

    private Scalars(Map<ScalarProperties, T> values) {
        this.values = Map.copyOf(values);
    }

    public static <T extends ScalarValue> Scalars<T> of(Map<ScalarProperties, T> values) {
        if (values == null) {
            throw new IllegalArgumentException("scalars must not be null");
        }
        for (var e : values.entrySet()) {
            T v = e.getValue();
            if (v == null) {
                throw new IllegalArgumentException("scalars." + e.getKey() + " must not be null");
            }
        }
        return new Scalars<>(values);
    }

    @JsonValue
    public Map<ScalarProperties, T> values() { return values; }

    @JsonCreator
    public static <T extends ScalarValue> Scalars<T> fromJson(Map<ScalarProperties, T> values) {
        return of(values);
    }

    public T get(ScalarProperties key) { return values.get(key); }
}