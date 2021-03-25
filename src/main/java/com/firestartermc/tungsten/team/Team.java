package com.firestartermc.tungsten.team;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import com.feed_the_beast.ftbutilities.data.FTBUtilitiesTeamData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a player team, where multiple players
 * have access to team resources (such as islands).
 */
public class Team {

    private final ForgeTeam forgeTeam;

    public Team(short id) {
        this.forgeTeam = Universe.get().getTeam(id);
    }

    public Team(ForgeTeam forgeTeam) {
        this.forgeTeam = forgeTeam;
    }

    @NotNull
    public static Optional<Team> ofPlayer(@NotNull Player player) {
        return Optional.ofNullable(Universe.get().getPlayer(player.getUniqueId()))
                .map(forgePlayer -> {
                    if (!forgePlayer.hasTeam()) {
                        return null;
                    }

                    return new Team(forgePlayer.team);
                });
    }

    public short getId() {
        return forgeTeam.getUID();
    }

    @NotNull
    public String getName() {
        return forgeTeam.getDesc();
    }

    @NotNull
    public Optional<Player> getLeader() {
        return Optional.ofNullable(forgeTeam.getOwner())
                .map(ForgePlayer::getId)
                .flatMap(uuid -> Sponge.getServer().getPlayer(uuid));
    }

    @NotNull
    public Stream<Player> getMembers() {
        return forgeTeam.getMembers().stream()
                .map(ForgePlayer::getId)
                .filter(uuid -> Sponge.getServer().getPlayer(uuid).isPresent())
                .map(uuid -> Sponge.getServer().getPlayer(uuid).get());
    }

    public void claimChunk(int x, int z) {
        ClaimedChunk chunk = new ClaimedChunk(new ChunkDimPos(x, z, 0), FTBUtilitiesTeamData.get(forgeTeam));
        ClaimedChunks.instance.addChunk(chunk);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(forgeTeam, team.forgeTeam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(forgeTeam);
    }
}
