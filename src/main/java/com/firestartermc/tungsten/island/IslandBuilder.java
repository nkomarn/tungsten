package com.firestartermc.tungsten.island;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.firestartermc.tungsten.Tungsten;
import com.firestartermc.tungsten.data.SqlStatements;
import com.firestartermc.tungsten.util.ConcurrentUtils;
import com.firestartermc.tungsten.util.Region;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;

/**
 * Constructs an island with the given build parameters.
 */
public class IslandBuilder {

    private ForgeTeam team;
    private Region region;

    @NotNull
    public IslandBuilder team(@NotNull ForgeTeam team) {
        this.team = team;
        return this;
    }

    @NotNull
    public IslandBuilder region(@NotNull Region region) {
        this.region = region;
        return this;
    }

    /**
     * Pastes the island schematic in the given region and
     * returns an object of the island after it has been inserted
     * into the database.
     *
     * @return The island object.
     */
    @NotNull
    public CompletableFuture<Island> build() {
        // Find next empty region file todo

        // Locate center and mark as paste location + spawn location
        Location<World> spawn = Tungsten.INSTANCE.getIslandGrid().getWorld().getLocation(0, 0, 0); // TODO placeholder

        // Do pasting (must be done sync) todo

        // Insert object into database
        return ConcurrentUtils.callAsync(() -> {
            try (Connection connection = Tungsten.INSTANCE.getDataStore().getConnection()) {
                PreparedStatement statement = connection.prepareStatement(SqlStatements.CREATE_ISLAND);
                statement.setShort(1, team.getUID());
                statement.setString(2, region.toString());
                statement.setInt(3, spawn.getBlockX());
                statement.setInt(4, spawn.getBlockY());
                statement.setInt(5, spawn.getBlockZ());
                statement.executeUpdate();

                statement = connection.prepareStatement(SqlStatements.SELECT_LAST_ID);
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    return new Island(result.getInt(1), team, region, spawn);
                }
            }

            throw new IllegalStateException("Failed to save island to database- a unique id was not returned.");
        });
    }
}
