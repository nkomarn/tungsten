package com.firestartermc.tungsten.util;

import com.firestartermc.tungsten.Tungsten;
import com.flowpowered.math.vector.Vector3i;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Objects;

/**
 * Represents the X, Z coordinate position of a Minecraft
 * world save region.
 *
 * @since 1.0
 */
public class RegionPos {

    private final int x;
    private final int z;

    public RegionPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @NotNull
    public static RegionPos fromChunk(int x, int z) {
        return new RegionPos(x >> 5, z >> 5);
    }

    @NotNull
    public static RegionPos fromBlock(int x, int z) {
        return fromChunk(MathUtils.floorInt(x) / 16, MathUtils.floorInt(z) / 16);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @NotNull
    public Vector3i getMinChunk() {
        return new Vector3i(getX() << 5, 0, getZ() << 5);
    }

    @NotNull
    public Vector3i getMaxChunk() {
        return new Vector3i((getX() + 1) << 5, 0, (getZ() + 1) << 5);
    }

    /**
     * Returns the center block of this regionPos.
     * Okay, technically regions don't have a center block since
     * they're even. But this is a good enough approximation so
     * whatever, fuck off.
     *
     * @return The center block location.
     */
    @NotNull
    public Location<World> getCenterBlock() {
        int centerX = getMinChunk().getX() + 16;
        int centerZ = getMinChunk().getZ() + 16;
        return Tungsten.INSTANCE.getIslandWorld().getLocation(centerX * 16, 86, centerZ * 16);
    }

    @Override
    public String toString() {
        return String.format("r.%s.%s", getX(), getZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionPos regionPos = (RegionPos) o;
        return x == regionPos.x && z == regionPos.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
