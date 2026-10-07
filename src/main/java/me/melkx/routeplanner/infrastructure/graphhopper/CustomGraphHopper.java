package me.melkx.routeplanner.infrastructure.graphhopper;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.WeightingFactory;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PMap;
import me.melkx.routeplanner.core.PreProcessingConstraints;
import me.melkx.routeplanner.core.Preferences;
import me.melkx.routeplanner.core.SurfaceType;
import me.melkx.routeplanner.core.property.DistributionProperties;
import me.melkx.routeplanner.core.property.ScalarProperties;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomGraphHopper extends GraphHopper {
    public static final String PREFERENCES_HINT_KEY = "preferences";
    public static final String CONSTRAINTS_HINT_KEY = "constraints";

    private static final String PICTURESQUENESS_KEY = "picturesqueness";
    private static final String SHADINESS_KEY = "shadiness";
    private static final String ROAD_QUALITY_KEY = "road_quality";
    private static final String TRAFFIC_STRESS_KEY = "traffic_stress";
    private static final String ILLUMINATION_KEY = "illumination";
    private static final String SURFACE_TYPE_KEY = "surface_type";

    @Override
    protected WeightingFactory createWeightingFactory() {
        return (profile, pMap, b) -> {
            if (Objects.equals(profile.getWeighting(), CustomWeighting.NAME)) {
                CustomWeighting.EncodedValues encodedValues = new CustomWeighting.EncodedValues(
                        Map.ofEntries(
                                Map.entry(ScalarProperties.PICTURESQUENESS, encodingManager.getDecimalEncodedValue(PICTURESQUENESS_KEY)),
                                Map.entry(ScalarProperties.SHADINESS, encodingManager.getDecimalEncodedValue(SHADINESS_KEY)),
                                Map.entry(ScalarProperties.ROAD_QUALITY, encodingManager.getDecimalEncodedValue(ROAD_QUALITY_KEY)),
                                Map.entry(ScalarProperties.TRAFFIC_STRESS, encodingManager.getDecimalEncodedValue(TRAFFIC_STRESS_KEY)),
                                Map.entry(ScalarProperties.ILLUMINATION, encodingManager.getDecimalEncodedValue(ILLUMINATION_KEY)),
                                Map.entry(ScalarProperties.AVERAGE_SLOPE, encodingManager.getDecimalEncodedValue(AverageSlope.KEY))
                        ),
                        Map.ofEntries(
                                Map.entry(
                                        DistributionProperties.SURFACE_TYPE,
                                        encodingManager.getEnumEncodedValue(SURFACE_TYPE_KEY, DistributionProperties.SURFACE_TYPE.getLinkedClass())
                                )
                        ));

                Preferences preferences = pMap.getObject(PREFERENCES_HINT_KEY, new Preferences(null, null));
                PreProcessingConstraints constraints = pMap.getObject(CONSTRAINTS_HINT_KEY, new PreProcessingConstraints(null, null));

                return new CustomWeighting(encodedValues, preferences, constraints);
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
        builder.add(new DecimalEncodedValueImpl(ILLUMINATION_KEY, bits, factor, false));
        builder.add(AverageSlope.create());
        builder.add(new EnumEncodedValue<>(SURFACE_TYPE_KEY, SurfaceType.class));
        builder.add(OSMWayID.create());

        EncodingManager defaultEm = super.buildEncodingManager(encodedValuesWithProps, activeImportUnits, restrictionVehicleTypesByProfile);

        for (EncodedValue ev : defaultEm.getEncodedValues()) {
            builder.add(ev);
        }

        return builder.build();
    }
}
