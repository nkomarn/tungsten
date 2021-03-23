package com.firestartermc.tungsten.island;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.firestartermc.tungsten.Tungsten;
import com.firestartermc.tungsten.data.SqlStatements;
import com.firestartermc.tungsten.util.ConcurrentUtils;
import com.firestartermc.tungsten.util.Region;
import net.minecraft.world.chunk.storage.RegionFile;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.firestartermc.tungsten.team.TeamManager.getTeam;
import static com.firestartermc.tungsten.team.TeamManager.hasTeam;

/**
 * Represents the collection of {@link Region}s in a {@link World} which
 * make up the skyblock world.
 *
 * @since 1.0
 */
public class IslandGrid {

    private final Tungsten plugin;
    private final World world;
    private final Map<Short, Island> islands;
    private final Map<RegionFile, Island> islandRegions;

    public IslandGrid(@NotNull Tungsten plugin) {
        this.plugin = plugin;
        this.world = Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get();
        this.islands = new ConcurrentHashMap<>();
        this.islandRegions = new ConcurrentHashMap<>();
    }

    /**
     * Returns the {@link World} within which this island grid is housed.
     *
     * @return The world.
     */
    @NotNull
    public World getWorld() {
        return world;
    }

    /*
    @NotNull
    public CompletableFuture<Island> createIsland(@NotNull Player player) {
        if (!hasTeam(player)) {
            throw new CompletionException(new IllegalStateException("Player must be in a team to create an island."));
        }

        // Remove existing tag in case we already have one so Botania doesn't teleport us back
        // This uses NMS to remove tags, so it's quite hacky but it's fineeeeeeeeeee (:
        player.toContainer().remove(DataQuery.of("Botania-HasOwnIsland"));
        player.toContainer().remove(DataQuery.of("Botania-MadeIsland"));

        // Run the Botania Garden of Glass spread command for this player to create a new island
        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "botania-skyblock-spread " + player.getName() + " 50000");

        // Once they're on the island, let's set their new island spawn and create the island object
        ForgeTeam team = getTeam(player).get();
        Location<World> location = player.getLocation();

        System.out.println("inserting new island data");
        return ConcurrentUtils.callAsync(() -> {
            try (Connection connection = plugin.getDataStore().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(SqlStatements.CREATE_ISLAND)) {
                    statement.setShort(1, team.getUID());
                    statement.setInt(2, location.getBlockX());
                    statement.setInt(3, location.getBlockY());
                    statement.setInt(4, location.getBlockZ());
                    statement.execute();
                }

                try (PreparedStatement statement = connection.prepareStatement(SqlStatements.SELECT_LAST_ID)) {
                    try (ResultSet result = statement.executeQuery()) {
                        result.next();
                        return new Island(result.getInt(1), team, location);
                    }
                }
            }
        });
    }
     */

    /*
    @NotNull
    public Optional<Island> getIsland(int uid) {
        return Optional.ofNullable(islands.get(uid));
    }

    @NotNull
    public Optional<Island> getIsland(@NotNull Player player) {
        return getTeam(player)
                .map(team -> islands.get(team.getUID()));

    }

    @NotNull
    public Optional<Island> getIslandByTeam(@NotNull ForgeTeam team) {
        return getIsland(team.getUID());
    }
     */

    /**
     * Returns an island that may be located at the given region.
     *
     * @param region The region.
     * @return Future of an optional of an island.
     */
    @NotNull
    public CompletableFuture<Optional<Island>> getIslandByRegion(@NotNull RegionFile region) {
        if (islandRegions.containsKey(region)) {
            return CompletableFuture.completedFuture(Optional.ofNullable(islandRegions.get(region)));
        }

        return ConcurrentUtils.callAsync(() -> {
            try (Connection connection = plugin.getDataStore().getConnection()) {
                PreparedStatement statement = connection.prepareStatement(SqlStatements.SELECT_BY_REGION);
                statement.setString(1, region.toString());
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    Island island = Island.fromResultSet(result);
                    islandRegions.put(region, island);
                    return Optional.of(island);
                }

                return Optional.empty();
            }
        });
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();

        if (!hasTeam(player)) {
            return;
        }

        // Check if this island is already loaded
        Optional<ForgeTeam> team = getTeam(player);

        if (team.flatMap(this::getIslandByTeam).isPresent()) {
            return;
        }

        // Otherwise, load it into the grid
        ConcurrentUtils.callAsync(() -> {
            try (Connection connection = plugin.getDataStore().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(SqlStatements.SELECT_ISLAND)) {
                    statement.setShort(1, team.get().getUID());

                    try (ResultSet result = statement.executeQuery()) {
                        while (result.next()) {
                            int islandId = result.getInt(1);
                            Location<World> location = getWorld().getLocation(
                                    result.getInt(3),
                                    result.getInt(4),
                                    result.getInt(5)
                            );

                            islands.putIfAbsent(team.get().getUID(), new Island(islandId, team.get(), location));
                        }
                    }
                }
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            player.sendMessage(Text.of(TextColors.RED, "Failed to load your island; notify staff."));
            return null;
        });
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        getTeam(event.getTargetEntity()).ifPresent(team -> islands.remove(team.getUID()));
    }
}
