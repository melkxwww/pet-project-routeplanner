package me.melkx.veloroute.module.generatorpreset.db.repository;

import me.melkx.veloroute.module.generatorpreset.db.entity.GeneratorPresetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneratorPresetRepository extends JpaRepository<GeneratorPresetEntity, Long> {
}
