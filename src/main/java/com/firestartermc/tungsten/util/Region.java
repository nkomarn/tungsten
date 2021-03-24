package com.firestartermc.tungsten.util;

import com.firestartermc.tungsten.Tungsten;
import com.firestartermc.tungsten.util.model.Pair;
import com.flowpowered.math.vector.Vector3i;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

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
     * Returns the center block of this region.
     * Okay, technically regions don't have a center block since
     * they're even. But this is a good enough approximation so
     * whatever, fuck off.
     *
     * @return The center block location.
     */
    @NotNull
    public Location<World> getCenterBlock() {
        World world = Tungsten.INSTANCE.getIslandWorld();
        Sponge.getServer().getBroadcastChannel().send(Text.of(getMinChunk().toString()));
        Sponge.getServer().getBroadcastChannel().send(Text.of(getMaxChunk().toString()));

        int centerX = getMinChunk().getX() + 16;
        int centerZ = getMinChunk().getZ() + 16;

        Sponge.getServer().getBroadcastChannel().send(Text.of(centerX + ":" + centerZ));
        Sponge.getServer().getBroadcastChannel().send(Text.of(toString()));

        return world.getLocation(centerX * 16, 86, centerZ * 16);
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
