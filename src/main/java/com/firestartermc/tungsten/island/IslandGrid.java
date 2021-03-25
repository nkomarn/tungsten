package com.firestartermc.tungsten.island;

import com.firestartermc.tungsten.Tungsten;
import com.firestartermc.tungsten.data.SqlStatements;
import com.firestartermc.tungsten.team.Team;
import com.firestartermc.tungsten.util.ConcurrentUtils;
import com.firestartermc.tungsten.util.RegionPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the collection of {@link RegionPos}s in a {@link World} which
 * make up the skyblock world.
 *
 * @since 1.0
 */
public class IslandGrid {

    private final Tungsten plugin;
    private final Map<Team, Island> islands;
    private final Map<RegionPos, Island> islandRegions;

    public IslandGrid(@NotNull Tungsten plugin) {
        this.plugin = plugin;
        this.islands = new ConcurrentHashMap<>();
        this.islandRegions = new ConcurrentHashMap<>();
    }

    /**
     * Returns an island that may be owned by the given {@link Team}.
     *
     * @param team The team.
     * @return Future of an optional of an island.
     */
    @NotNull
    public CompletableFuture<Optional<Island>> getIslandByTeam(@NotNull Team team) {
        if (islands.containsKey(team)) {
            return CompletableFuture.completedFuture(Optional.ofNullable(islands.get(team)));
        }

        return ConcurrentUtils.callAsync(() -> {
            try (Connection connection = plugin.getDataStore().getConnection()) {
                PreparedStatement statement = connection.prepareStatement(SqlStatements.SELECT_BY_ID);
                statement.setShort(1, team.getId());
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    Island island = Island.fromResultSet(result);
                    islands.put(island.getTeam(), island);
                    islandRegions.put(island.getRegion(), island);
                    return Optional.of(island);
                }

                return Optional.empty();
            }
        });
    }

    /**
     * Returns an island that may be located at the given regionPos.
     *
     * @param regionPos The regionPos.
     * @return Future of an optional of an island.
     */
    @NotNull
    public CompletableFuture<Optional<Island>> getIslandByRegion(@NotNull RegionPos regionPos) {
        if (islandRegions.containsKey(regionPos)) {
            return CompletableFuture.completedFuture(Optional.ofNullable(islandRegions.get(regionPos)));
        }

        return ConcurrentUtils.callAsync(() -> {
            try (Connection connection = plugin.getDataStore().getConnection()) {
                PreparedStatement statement = connection.prepareStatement(SqlStatements.SELECT_BY_REGION);
                statement.setInt(1, regionPos.getX());
                statement.setInt(2, regionPos.getZ());
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    Island island = Island.fromResultSet(result);
                    islands.put(island.getTeam(), island);
                    islandRegions.put(regionPos, island);
                    return Optional.of(island);
                }

                return Optional.empty();
            }
        });
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        Optional<Team> team = Team.ofPlayer(player);

        if (!team.isPresent()) {
            return;
        }

        if (!islands.containsKey(team.get())) {
            return;
        }

        getIslandByTeam(team.get()).thenAccept(island -> {
            player.sendMessage(Text.of(TextColors.GREEN, "Loaded your island."));
        }).exceptionally(e -> {
            e.printStackTrace();
            player.sendMessage(Text.of(TextColors.RED, "Failed to load your island; notify staff."));
            return null;
        });
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        // Check if the team has active players left; otherwise unload the island
        Player player = event.getTargetEntity();
        Optional<Team> team = Team.ofPlayer(player);

        if (!team.isPresent()) {
            return;
        }

        if (!islands.containsKey(team.get())) {
            return;
        }

        boolean isVacant = team.get()
                .getMembers()
                .noneMatch(Player::isOnline);

        if (isVacant) {
            Island island = islands.remove(team.get());
            islandRegions.remove(island.getRegion());
            System.out.println("Unloaded island of team '" + team.get().getName() + "'.");
        }
    }

    // TODO nuke island when team is disbanded completely since team ids might be reused
}
