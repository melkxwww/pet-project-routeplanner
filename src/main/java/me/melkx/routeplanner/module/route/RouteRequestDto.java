package me.melkx.routeplanner.module.route;

import jakarta.validation.Valid;

public record RouteRequestDto(@Valid Route route, @Valid Constraints constraints) {
}
