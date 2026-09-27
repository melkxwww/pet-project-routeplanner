package me.melkx.routeplanner.module.route;

import jakarta.validation.Valid;

public record Waypoint(@Valid Point point, Boolean mustVisit) {
}
