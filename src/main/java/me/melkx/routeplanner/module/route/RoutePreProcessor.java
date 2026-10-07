package me.melkx.routeplanner.module.route;

import com.graphhopper.GraphHopper;
import me.melkx.routeplanner.module.route.dto.GenerateCandidatesCommand;
import org.springframework.stereotype.Service;

@Service
public class RoutePreProcessor {
    private final GraphHopper graphHopper;

    public void generateCandidates(GenerateCandidatesCommand command) {

    }
}
