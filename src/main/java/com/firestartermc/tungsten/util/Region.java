package com.firestartermc.tungsten.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a Minecraft region.
 */
public class Region {

    private final int x;
    private final int z;

    public Region(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @NotNull
    public static Region fromChunk(int x, int z) {
        return new Region(x >> 5, z >> 5);
    }

    @NotNull
    public static Region fromBlock(int x, int z) {
        return fromChunk(MathUtils.floorInt(x) / 16, MathUtils.floorInt(z) / 16);
    }

    @NotNull
    public static Region fromString(@NotNull String region) {
        String[] sections = region.split(".");
        return new Region(Integer.parseInt(sections[1]), Integer.parseInt(sections[2]));
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        return String.format("r.%s.%s", getX(), getZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region region = (Region) o;
        return x == region.x && z == region.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
