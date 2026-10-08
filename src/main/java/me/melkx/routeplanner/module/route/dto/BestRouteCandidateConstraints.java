package me.melkx.routeplanner.module.route.dto;

public record BestRouteCandidateConstraints(Range distanceM, Range durationS) {
    public record Range(Double min, Double max) {
        public Range {
            if(min < max)
                throw new IllegalArgumentException("range min must be <= max but was [" + min + ", " + max + "]");
        }
    }
}
