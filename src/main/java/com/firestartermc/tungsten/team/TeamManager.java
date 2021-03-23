package com.firestartermc.tungsten.team;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

public class TeamManager {


    @NotNull
    public static Optional<ForgeTeam> getTeam(@NotNull Player player) {
        if (!hasTeam(player)) {
            return Optional.empty();
        }

        return Optional.ofNullable(getForgePlayer(player).team);
    }

    public static boolean hasTeam(@NotNull Player player) {
        return getForgePlayer(player).hasTeam();
    }

    private static ForgePlayer getForgePlayer(@NotNull Player player) {
        return Universe.get().getPlayer(player.getUniqueId());
    }
}
