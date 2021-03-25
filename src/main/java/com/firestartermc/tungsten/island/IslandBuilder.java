package com.firestartermc.tungsten.island;

import com.firestartermc.tungsten.Tungsten;
import com.firestartermc.tungsten.data.SqlStatements;
import com.firestartermc.tungsten.team.Team;
import com.firestartermc.tungsten.util.ConcurrentUtils;
import com.firestartermc.tungsten.util.RegionPos;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.sponge.SpongeWorldEdit;
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

    private final Team team;
    private final RegionPos regionPos;

    public IslandBuilder(@NotNull Team team, @NotNull RegionPos regionPos) {
        this.team = team;
        this.regionPos = regionPos;
    }

    /**
     * Pastes the island schematic in the given regionPos and
     * returns an object of the island after it has been inserted
     * into the database.
     *
     * @return The island object.
     */
    @NotNull
    public CompletableFuture<Island> build() {
        Location<World> spawn = regionPos.getCenterBlock();

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

        // Claim the full regionPos automatically
        for (int x = regionPos.getMinChunk().getX(); x < regionPos.getMaxChunk().getX(); x++) {
            for (int z = regionPos.getMinChunk().getZ(); z < regionPos.getMaxChunk().getZ(); z++) {
                team.claimChunk(x, z);
            }
        }

        // TOOD send fake worldborders

        // Insert object into database
        return ConcurrentUtils.callAsync(() -> {
            try (Connection connection = Tungsten.INSTANCE.getDataStore().getConnection()) {
                PreparedStatement statement = connection.prepareStatement(SqlStatements.CREATE_ISLAND);
                statement.setShort(1, team.getId());
                statement.setInt(2, regionPos.getX());
                statement.setInt(3, regionPos.getZ());
                statement.setInt(4, spawn.getBlockX());
                statement.setInt(5, spawn.getBlockY());
                statement.setInt(6, spawn.getBlockZ());
                statement.executeUpdate();

                statement = connection.prepareStatement(SqlStatements.SELECT_LAST_ID);
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    return new Island(team, regionPos, spawn);
                }
            }

            throw new IllegalStateException("Failed to save island to database- a unique id was not returned.");
        });
    }
}
