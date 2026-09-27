package me.melkx.routeplanner.module.route;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record Target(TargetMode mode, Double value, @Min(0) @Max(100) Integer tolerancePct) {
}
