package me.melkx.veloroute.module.generatorpreset.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import me.melkx.veloroute.core.model.GeneratorPreset;

public record GeneratorPresetSavingRequestDto(
        @NotNull String name,
        @NotNull @Valid GeneratorPreset preset) {
}
