package me.melkx.routeplanner.core;

public record Point(Double lat, Double lon) {
    public Point {
        if(lat < -90 || lat > 90)
            throw new IllegalArgumentException("lat must be between -90 and 90");

        if(lon < -180 || lon > 180)
            throw new IllegalArgumentException("lon must be between -180 and 180");
    }
}
