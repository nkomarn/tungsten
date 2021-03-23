package com.firestartermc.tungsten.island;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.firestartermc.tungsten.Tungsten;
import com.firestartermc.tungsten.util.Region;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Island {

    private final int id;
    private final ForgeTeam team;
    private final Region region;
    private final Location<World> spawnLocation;

    public Island(int id, @NotNull ForgeTeam team, @NotNull Region region, @NotNull Location<World> spawnLocation) {
        this.id = id;
        this.team = team;
        this.region = region;
        this.spawnLocation = spawnLocation;
    }

    @NotNull
    public static Island fromResultSet(@NotNull ResultSet result) throws SQLException {
        return new Island(
                result.getInt(1),
                Universe.get().getTeam(result.getShort(2)),
                Region.fromString(result.getString(3)),
                Tungsten.INSTANCE.getIslandGrid().getWorld().getLocation(
                        result.getInt(4),
                        result.getInt(5),
                        result.getInt(6)
                )
        );
    }

    public int getId() {
        return id;
    }

    @NotNull
    public ForgeTeam getTeam() {
        return team;
    }

    @NotNull
    public Region getRegion() {
        return region;
    }

    @NotNull
    public Location<World> getSpawnLocation() {
        return spawnLocation;
    }
}
