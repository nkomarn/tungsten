package com.firestartermc.tungsten.command;

import com.firestartermc.tungsten.Tungsten;
import com.firestartermc.tungsten.command.island.SetSpawnCommand;
import com.firestartermc.tungsten.data.SqlStatements;
import com.firestartermc.tungsten.island.IslandBuilder;
import com.firestartermc.tungsten.team.Team;
import com.firestartermc.tungsten.util.ConcurrentUtils;
import com.firestartermc.tungsten.util.Region;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.title.Title;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class IslandCommand implements CommandExecutor {

    private final Tungsten plugin;

    public IslandCommand(@NotNull Tungsten plugin) {
        this.plugin = plugin;

        CommandSpec setSpawnSpec = CommandSpec.builder()
                .executor(new SetSpawnCommand())
                .build();

        CommandSpec spec = CommandSpec.builder()
                .executor(this)
                .child(setSpawnSpec, "setspawn")
                .build();

        Sponge.getCommandManager().register(plugin, spec, "is", "island");
    }

    @Override
    @NotNull
    public CommandResult execute(@NotNull CommandSource src, @NotNull CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            return CommandResult.empty();
        }

        Player player = (Player) src;
        Optional<Team> team = Team.ofPlayer(player);

        if (!team.isPresent()) {
            throw new CommandException(Text.of("You must be in a team to use this!"));
        }

        plugin.getIslandGrid().getIslandByTeam(team.get()).thenAccept(island -> {
            if (island.isPresent()) {
                ConcurrentUtils.ensureMain(() -> player.setLocation(island.get().getSpawnLocation()));
                return;
            }

            createIsland(player, team.get());
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });

        return CommandResult.success();
    }

    private void createIsland(@NotNull Player player, @NotNull Team team) {
        CompletableFuture<Region> future = new CompletableFuture<>();
        findEmptyRegion(future);

        future.thenCompose(region -> new IslandBuilder().team(team).region(region).build()).thenAccept(island -> {
            ConcurrentUtils.ensureMain(() -> {
                player.setLocation(island.getSpawnLocation());

                Title title = Title.builder()
                        .title(Text.builder("WELCOME HOME").style(TextStyles.BOLD).build())
                        .subtitle(Text.of("Your new humble abode"))
                        .build();

                player.sendTitle(title);
                // TODO some epic sound here uwu
                player.sendMessage(Text.of(TextColors.GOLD, "Welcome to your new island! Reference your quest book for starting steps and survival tips."));
            });
        }).exceptionally(e -> {
            e.printStackTrace();
            player.sendMessage(Text.of(TextColors.RED, "Failed to create your island; contact staff."));
            return null;
        });
    }

    private void findEmptyRegion(CompletableFuture<Region> future) {
        int x = ThreadLocalRandom.current().nextInt(0, 30000000);
        int z = ThreadLocalRandom.current().nextInt(0, 30000000);
        Region region = Region.fromBlock(x, z);

        ConcurrentUtils.callAsync(() -> {
            try (Connection connection = plugin.getDataStore().getConnection()) {
                PreparedStatement statement = connection.prepareStatement(SqlStatements.SELECT_BY_REGION);
                statement.setInt(1, region.getX());
                statement.setInt(2, region.getZ());
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    findEmptyRegion(future);
                    return;
                }

                future.complete(region);
            }
        }).exceptionally(e -> {
            future.completeExceptionally(e);
            return null;
        });
    }
}
