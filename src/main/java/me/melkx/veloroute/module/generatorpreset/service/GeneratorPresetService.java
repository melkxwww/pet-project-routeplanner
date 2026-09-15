package me.melkx.veloroute.module.generatorpreset.service;

import me.melkx.veloroute.module.generatorpreset.db.repository.GeneratorPresetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeneratorPresetService {
    private final GeneratorPresetRepository presetRepository;

    @Autowired
    public GeneratorPresetService(GeneratorPresetRepository presetRepository) {
        this.presetRepository = presetRepository;
    }

    public long savePreset()
}
