package me.melkx.routeplanner.infrastructure.graphhopper;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.WeightingFactory;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PMap;
import me.melkx.routeplanner.core.SurfaceType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomGraphHopper extends GraphHopper {
    public static final String GENERATOR_PREFERENCES_HINT_KEY = "generator_preferences";
    public static final String GENERATOR_WEIGHTS_HINT_KEY = "generator_weights";
    public static final String GENERATOR_BLOCKED_EDGES_HINT_KEY = "generator_blocked_edges";

    private static final String PICTURESQUENESS_KEY = "picturesqueness";
    private static final String SHADINESS_KEY = "shadiness";
    private static final String ROAD_QUALITY_KEY = "road_quality";
    private static final String TRAFFIC_STRESS_KEY = "traffic_stress";
    private static final String ILLUMINATION_STRESS_KEY = "illumination";
    private static final String SURFACE_TYPE_KEY = "surface_type";

    @Override
    protected WeightingFactory createWeightingFactory() {
        return (profile, pMap, b) -> {
            if (Objects.equals(profile.getWeighting(), CustomWeighting.NAME)) {

            }

            return CustomGraphHopper.super.createWeighting(profile, pMap, b);
        };
    }

    @Override
    protected EncodingManager buildEncodingManager(
            Map<String, PMap> encodedValuesWithProps,
            Map<String, ImportUnit> activeImportUnits,
            Map<String, List<String>> restrictionVehicleTypesByProfile) {
        EncodingManager.Builder builder = new EncodingManager.Builder();

        int bits = 5;
        double factor = 1.0 / ((1 << bits) - 1);

        builder.add(new DecimalEncodedValueImpl(PICTURESQUENESS_KEY, bits, factor, false));
        builder.add(new DecimalEncodedValueImpl(SHADINESS_KEY, bits, factor, false));
        builder.add(new DecimalEncodedValueImpl(ROAD_QUALITY_KEY, bits, factor, false));
        builder.add(new DecimalEncodedValueImpl(TRAFFIC_STRESS_KEY, bits, factor, false));
        builder.add(new DecimalEncodedValueImpl(ILLUMINATION_STRESS_KEY, bits, factor, false));
        builder.add(new EnumEncodedValue<>(SURFACE_TYPE_KEY, SurfaceType.class));
        builder.add(OSMWayID.create());

        EncodingManager defaultEm = super.buildEncodingManager(encodedValuesWithProps, activeImportUnits, restrictionVehicleTypesByProfile);

        for (EncodedValue ev : defaultEm.getEncodedValues()) {
            builder.add(ev);
        }

        return builder.build();
    }
}
