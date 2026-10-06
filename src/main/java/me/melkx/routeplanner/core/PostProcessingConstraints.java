package me.melkx.routeplanner.core;

public record PostProcessingConstraints(Range durationS, Range distanceM) {
    public record Range(Double min, Double max) {
        public Range {
            if(min < max)
                throw new IllegalArgumentException("range min must be <= max but was [" + min + ", " + max + "]");
        }
    }
}
