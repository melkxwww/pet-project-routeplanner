package me.melkx.routeplanner.module.route.dto;

import java.util.List;

public record SelectCandidateCommand(List<RouteCandidate> candidates, BestRouteCandidateConstraints candidateConstraints, ) {
}
