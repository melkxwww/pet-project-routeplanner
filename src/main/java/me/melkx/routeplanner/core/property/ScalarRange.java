package me.melkx.routeplanner.core.property;

public record ScalarRange(double min, double max) implements ScalarValue {
    public static final double MIN_VALUE = 0.0;
    public static final double MAX_VALUE = 1.0;

    public ScalarRange {
        if (min < MIN_VALUE || max > MAX_VALUE) {
            throw new IllegalArgumentException(
                    "range must be within [" + MIN_VALUE + ", " + MAX_VALUE + "] but was ["
                            + min + ", " + max + "]");
        }
        if (min > max) {
            throw new IllegalArgumentException(
                    "range min must be <= max but was [" + min + ", " + max + "]");
        }
    }

    @Override public String describe() { return "[" + min + ", " + max + "]"; }
}