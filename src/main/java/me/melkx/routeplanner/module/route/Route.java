package me.melkx.routeplanner.module.route;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;

import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "mode",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AutoRoute.class, name = "AUTO"),
        @JsonSubTypes.Type(value = ManualRoute.class, name = "MANUAL")
})
public sealed interface Route permits AutoRoute, ManualRoute {
    EndpointMode mode();
    RouteType type();
    Point start();
    List<Waypoint> waypoints();
}
