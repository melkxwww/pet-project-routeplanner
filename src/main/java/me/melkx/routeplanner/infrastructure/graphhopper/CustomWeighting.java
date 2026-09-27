package me.melkx.routeplanner.infrastructure.graphhopper;

import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;

public class CustomWeighting implements Weighting {
    @Override
    public double calcMinWeightPerDistance() {
        return 0;
    }

    @Override
    public double calcEdgeWeight(EdgeIteratorState edgeIteratorState, boolean b) {
        return 0;
    }

    @Override
    public long calcEdgeMillis(EdgeIteratorState edgeIteratorState, boolean b) {
        return 0;
    }

    @Override
    public double calcTurnWeight(int i, int i1, int i2) {
        return 0;
    }

    @Override
    public long calcTurnMillis(int i, int i1, int i2) {
        return 0;
    }

    @Override
    public boolean hasTurnCosts() {
        return false;
    }

    @Override
    public String getName() {
        return "";
    }
}
