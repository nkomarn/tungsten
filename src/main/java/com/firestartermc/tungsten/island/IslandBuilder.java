package com.firestartermc.tungsten.island;

import com.firestartermc.tungsten.Tungsten;
import com.firestartermc.tungsten.data.SqlStatements;
import com.firestartermc.tungsten.team.Team;
import com.firestartermc.tungsten.util.ConcurrentUtils;
import com.firestartermc.tungsten.util.Region;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.world.Chunk;
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

    private Team team;
    private Region region;

    @NotNull
    public IslandBuilder team(@NotNull Team team) {
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
        Location<World> spawn = region.getCenterBlock();

        // Paste on main server thread
        ConcurrentUtils.ensureMain(() -> {
            com.sk89q.worldedit.world.World world = SpongeWorldEdit.inst().getWorld(Tungsten.INSTANCE.getIslandWorld());
            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
            Operation operation = new ClipboardHolder(Tungsten.INSTANCE.getIslandSchematic(), world.getWorldData())
                    .createPaste(editSession, world.getWorldData())
                    .to(new Vector(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()))
                    .ignoreAirBlocks(false)
                    .build();

            try {
                Operations.complete(operation);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        });

        // TODO Claim the island area as the team

        // TOOD send fake worldborders

        // Insert object into database
        return ConcurrentUtils.callAsync(() -> {
            try (Connection connection = Tungsten.INSTANCE.getDataStore().getConnection()) {
                PreparedStatement statement = connection.prepareStatement(SqlStatements.CREATE_ISLAND);
                statement.setShort(1, team.getId());
                statement.setInt(2, region.getX());
                statement.setInt(3, region.getZ());
                statement.setInt(4, spawn.getBlockX());
                statement.setInt(5, spawn.getBlockY());
                statement.setInt(6, spawn.getBlockZ());
                statement.executeUpdate();

                statement = connection.prepareStatement(SqlStatements.SELECT_LAST_ID);
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    return new Island(team, region, spawn);
                }
            }

            throw new IllegalStateException("Failed to save island to database- a unique id was not returned.");
        });
    }
}
