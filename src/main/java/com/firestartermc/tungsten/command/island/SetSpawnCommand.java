package com.firestartermc.tungsten.command.island;

import com.firestartermc.tungsten.Tungsten;
import com.firestartermc.tungsten.team.Team;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class SetSpawnCommand implements CommandExecutor {

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

        // TODO check if the player is within their islands reagion

        Tungsten.INSTANCE.getIslandGrid().getIslandByTeam(team.get()).thenAccept(optional -> {
            if (optional.isPresent()) {
                optional.get().setSpawnLocation(player.getLocation());
                player.sendMessage(Text.of(TextColors.GREEN, "Set island spawn location!"));
                return;
            }

            // ....
        });

        return CommandResult.empty();
    }
}
