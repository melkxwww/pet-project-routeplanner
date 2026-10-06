package me.melkx.routeplanner.core.property;

public record ScalarDouble(double value) implements ScalarValue {
    public static final double MIN = 0.0;
    public static final double MAX = 1.0;

    public ScalarDouble {
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException(
                    "scalar value must be between " + MIN + " and " + MAX + " but was " + value);
        }
    }

    @Override public String describe() { return String.valueOf(value); }
}
