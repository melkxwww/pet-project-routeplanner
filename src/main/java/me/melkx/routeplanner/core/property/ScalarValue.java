package me.melkx.routeplanner.core.property;

public sealed interface ScalarValue permits ScalarDouble, ScalarRange {
    String describe();
}
