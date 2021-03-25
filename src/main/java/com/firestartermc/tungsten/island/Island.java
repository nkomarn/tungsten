package com.firestartermc.tungsten.island;

import com.firestartermc.tungsten.Tungsten;
import com.firestartermc.tungsten.data.SqlStatements;
import com.firestartermc.tungsten.team.Team;
import com.firestartermc.tungsten.util.ConcurrentUtils;
import com.firestartermc.tungsten.util.RegionPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Island {

    private final Team team;
    private final RegionPos regionPos;
    private Location<World> spawnLocation;

    public Island(@NotNull Team team, @NotNull RegionPos regionPos, @NotNull Location<World> spawnLocation) {
        this.team = team;
        this.regionPos = regionPos;
        this.spawnLocation = spawnLocation;
    }

    @NotNull
    public static Island fromResultSet(@NotNull ResultSet result) throws SQLException {
        return new Island(
                new Team(result.getShort(1)),
                new RegionPos(result.getInt(2), result.getInt(3)),
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
    public RegionPos getRegion() {
        return regionPos;
    }

    @NotNull
    public Location<World> getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(@NotNull Location<World> location) {
        this.spawnLocation = location;

        ConcurrentUtils.callAsync(() -> {
            try (Connection connection = Tungsten.INSTANCE.getDataStore().getConnection()) {
                PreparedStatement statement = connection.prepareStatement(SqlStatements.UDPATE_SPAWN);
                statement.setInt(1, location.getBlockX());
                statement.setInt(2, location.getBlockY());
                statement.setInt(3, location.getBlockZ());
                statement.setShort(4, getTeam().getId());
                statement.executeUpdate();
            }
        });
    }
}
