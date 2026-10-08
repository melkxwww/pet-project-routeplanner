package me.melkx.routeplanner.module.route;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import me.melkx.routeplanner.core.*;
import me.melkx.routeplanner.infrastructure.graphhopper.CustomGraphHopper;
import me.melkx.routeplanner.infrastructure.graphhopper.GraphHopperConfig;
import me.melkx.routeplanner.module.route.dto.GenerateCandidateCommand;
import me.melkx.routeplanner.module.route.dto.GenerateCandidatesCommand;
import me.melkx.routeplanner.module.route.dto.RouteCandidate;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
public class RouteCandidateGenerator {
    private final GraphHopper graphHopper;

    public RouteCandidateGenerator(GraphHopper graphHopper) {
        this.graphHopper = graphHopper;
    }

    public List<RouteCandidate> generateCandidates(GenerateCandidatesCommand command) {
        Random rand = new Random();
        List<RouteCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < command.candidateCount(); i++)
            candidates.add(generateCandidate(command.routeType(),
                    command.start(),
                    command.end(),
                    command.waypoints(),
                    command.routeParameters(),
                    command.routeParameterConstraints(),
                    rand.nextInt()));
        return candidates;
    }

    public RouteCandidate generateConcreteCandidate(GenerateCandidateCommand command) {
        return generateCandidate(command.routeType(),
                command.start(),
                command.end(),
                command.waypoints(),
                command.routeParameters(),
                command.routeParameterConstraints(),
                command.seed());
    }

    private RouteCandidate generateCandidate(RouteType routeType,
                                             Point start,
                                             Point end,
                                             List<Point> waypoints,
                                             BakedRouteParameters routeParameters,
                                             @Nullable BakedRouteParameterConstraints routeParameterConstraints,
                                             @Nullable Integer seed) {
        GHRequest request = resolveRequestByRouteType(
                routeType,
                new GHPoint(start.lat(), start.lon()),
                new GHPoint(end.lat(), end.lon()),
                waypoints.stream().map(p -> new GHPoint(p.lat(), p.lon())).toList());

        request.setProfile(GraphHopperConfig.PROFILE_NAME);
        request.putHint(CustomGraphHopper.GENERATOR_SETTINGS_HINT_KEY, new GeneratorSettings(routeParameters,
                routeParameterConstraints,
                Objects.requireNonNullElseGet(seed, () -> new Random().nextInt())));

        GHResponse response = graphHopper.route(request);

        ResponsePath route = response.getBest();
        PointList pointList = route.getPoints();
        List<Point> path = new ArrayList<>(pointList.size());
        for (int i = 0; i < pointList.size(); i++) {
            path.add(new Point(
                    pointList.getLat(i),
                    pointList.getLon(i)
            ));
        }

        return new RouteCandidate(
                path,
                route.getDistance(),
                route.getTime(),

        );
    }

    private GHRequest resolveRequestByRouteType(RouteType routeType, GHPoint start, GHPoint end, List<GHPoint> waypoints) {
        GHRequest request;
        if (routeType == RouteType.LOOP) {
            request = new GHRequest();
            request.addPoint(start);
            request.addPoint(end);
            request.getPoints().addAll(waypoints);
            request.addPoint(start);
        } else {
            request = new GHRequest();
            request.addPoint(start);
            request.getPoints().addAll(waypoints);
            request.addPoint(end);
        }
        return request;
    }
}
