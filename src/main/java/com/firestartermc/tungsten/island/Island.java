package com.firestartermc.tungsten.island;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.firestartermc.tungsten.Tungsten;
import com.firestartermc.tungsten.team.Team;
import com.firestartermc.tungsten.util.Region;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Island {

    private final Team team;
    private final Region region;
    private final Location<World> spawnLocation;

    public Island(@NotNull Team team, @NotNull Region region, @NotNull Location<World> spawnLocation) {
        this.team = team;
        this.region = region;
        this.spawnLocation = spawnLocation;
    }

    @NotNull
    public static Island fromResultSet(@NotNull ResultSet result) throws SQLException {
        return new Island(
                new Team(result.getShort(1)),
                new Region(result.getInt(2), result.getInt(3)),
                Tungsten.INSTANCE.getIslandWorld().getLocation(
                        result.getInt(4),
                        result.getInt(5),
                        result.getInt(6)
                )
        );
    }

    @NotNull
    public Team getTeam() {
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
