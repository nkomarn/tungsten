package com.firestartermc.tungsten.command;

import com.firestartermc.tungsten.Tungsten;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyles;

import java.net.MalformedURLException;
import java.net.URL;

public class VoteCommand implements CommandExecutor {

    public VoteCommand(@NotNull Tungsten plugin) {
        CommandSpec spec = CommandSpec.builder()
                .executor(this)
                .build();

        Sponge.getCommandManager().register(plugin, spec, "vote");
    }

    @Override
    @NotNull
    public CommandResult execute(@NotNull CommandSource src, @NotNull CommandContext args) throws CommandException {
        try {
            LiteralText message = Text.builder(" ")
                    .append(Text.NEW_LINE)
                    .append(Text.NEW_LINE)
                    .append(Text.builder("Vote for RAK!").color(TextColors.RED).style(TextStyles.BOLD).build())
                    .append(Text.NEW_LINE)
                    .append(Text.of("RAK is a useful currency which can be used to purchase rewards from your quest book!"))
                    .append(Text.NEW_LINE)
                    .append(Text.of(TextColors.YELLOW, "Click here to vote!"))
                    .append(Text.NEW_LINE)
                    .append(Text.EMPTY)
                    .onClick(TextActions.openUrl(new URL("exziisdumb.com")))
                    .build();

            src.sendMessage(message);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return CommandResult.success();
    }
}
