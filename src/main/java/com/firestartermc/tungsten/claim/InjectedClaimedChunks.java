package com.firestartermc.tungsten.claim;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimResult;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.OptionalInt;
import java.util.Set;

public class InjectedClaimedChunks extends ClaimedChunks {

    private final ClaimedChunks wrapped;

    private InjectedClaimedChunks(@NotNull ClaimedChunks wrapped) {
        super(Universe.get());
        this.wrapped = wrapped;
    }

    public static void inject() {
        ClaimedChunks.instance = new InjectedClaimedChunks(ClaimedChunks.instance);
    }

    @Nullable
    @Override
    public ForgeTeam getChunkTeam(@NotNull ChunkDimPos pos) {
        return wrapped.getChunkTeam(pos);
    }

    @Override
    public void markDirty() {
        wrapped.markDirty();
    }

    @Override
    public void clear() {
        wrapped.clear();
    }

    @Override
    public void processQueue() {
        wrapped.processQueue();
    }

    @Override
    public void update(@NotNull Universe universe, long now) {
        wrapped.update(universe, now);
    }

    @Nullable
    @Override
    public ClaimedChunk getChunk(@NotNull ChunkDimPos pos) {
        return wrapped.getChunk(pos);
    }

    @Override
    public void removeChunk(@NotNull ChunkDimPos pos) {
        wrapped.removeChunk(pos);
    }

    @Override
    public void addChunk(@NotNull ClaimedChunk chunk) {
        wrapped.addChunk(chunk);
    }

    @Override
    @NotNull
    public Collection<ClaimedChunk> getAllChunks() {
        return wrapped.getAllChunks();
    }

    @Override
    @NotNull
    public Set<ClaimedChunk> getTeamChunks(@Nullable ForgeTeam team, @NotNull OptionalInt dimension) {
        return wrapped.getTeamChunks(team, dimension);
    }

    @Override
    @NotNull
    public Set<ClaimedChunk> getTeamChunks(@Nullable ForgeTeam team, @NotNull OptionalInt dimension, boolean includePending) {
        return wrapped.getTeamChunks(team, dimension, includePending);
    }

    @Override
    public boolean canPlayerModify(@NotNull ForgePlayer player, @NotNull ChunkDimPos pos, @NotNull String perm) {
        return wrapped.canPlayerModify(player, pos, perm);
    }

    @Override
    @NotNull
    public ClaimResult claimChunk(@NotNull ForgePlayer player, @NotNull ChunkDimPos pos) {
        return wrapped.claimChunk(player, pos);
    }

    @Override
    @NotNull
    public ClaimResult claimChunk(@NotNull ForgePlayer player, @NotNull ChunkDimPos pos, boolean checkLimits) {
        return wrapped.claimChunk(player, pos, checkLimits);
    }

    @Override
    public boolean unclaimChunk(@Nullable ForgePlayer player, @NotNull ChunkDimPos pos) {
        if (player != null) {
            return false;
        }

        return wrapped.unclaimChunk(player, pos);
    }

    @Override
    public void unclaimAllChunks(@Nullable ForgePlayer player, @NotNull ForgeTeam team, @NotNull OptionalInt dim) {
        if (player != null) {
            return;
        }

        wrapped.unclaimAllChunks(player, team, dim);
    }

    @Override
    public boolean loadChunk(@Nullable ForgePlayer player, @NotNull ForgeTeam team, @NotNull ChunkDimPos pos) {
        if (player != null) {
            return false;
        }

        return wrapped.loadChunk(player, team, pos);
    }

    @Override
    public boolean unloadChunk(@Nullable ForgePlayer player, @NotNull ChunkDimPos pos) {
        if (player != null) {
            return false;
        }

        return wrapped.unclaimChunk(player, pos);
    }
}
