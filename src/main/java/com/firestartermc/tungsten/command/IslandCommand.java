package com.firestartermc.tungsten.command;

import com.firestartermc.tungsten.Tungsten;
import com.firestartermc.tungsten.island.Island;
import com.firestartermc.tungsten.team.TeamManager;
import com.firestartermc.tungsten.util.ConcurrentUtils;
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

import java.util.Optional;

import static com.firestartermc.tungsten.team.TeamManager.hasTeam;

public class IslandCommand implements CommandExecutor {

    private final Tungsten plugin;

    public IslandCommand(@NotNull Tungsten plugin) {
        this.plugin = plugin;
        CommandSpec spec = CommandSpec.builder()
                .executor(this)
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

        if (!hasTeam(player)) {
            throw new CommandException(Text.of("You must be in a team to use this!"));
        }

        Optional<Island> optional = plugin.getIslandGrid().getIsland(player);

        if (optional.isPresent()) {
            player.setLocation(optional.get().getSpawnLocation());
            return CommandResult.success();
        }

        // Create a new island
        plugin.getIslandGrid().createIsland(player).thenAccept(island -> {
            ConcurrentUtils.ensureMain(() -> player.setLocation(island.getSpawnLocation()));
        }).exceptionally(e -> {
            e.printStackTrace();
            player.sendMessage(Text.of(TextColors.RED, "Failed to create your island; contact staff."));
            return null;
        });

        return CommandResult.success();
    }
}
