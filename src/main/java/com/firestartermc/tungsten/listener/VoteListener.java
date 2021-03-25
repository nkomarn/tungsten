package com.firestartermc.tungsten.listener;

import com.vexsoftware.votifier.sponge.event.VotifierEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class VoteListener {

    private final ItemStack reward;

    public VoteListener() {
        ItemType type = Sponge.getGame().getRegistry().getType(ItemType.class, "contenttweaker:rak_coin")
                .orElse(ItemTypes.STONE);
        this.reward = ItemStack.of(type, 10);
    }

    @Listener
    public void onVote(VotifierEvent event) {
        Sponge.getServer().getPlayer(event.getVote().getUsername()).ifPresent(this::award);
    }

    private void award(@NotNull Player player) {
        player.getInventory().offer(reward.copy()).getRejectedItems()
                .forEach(snapshot -> dropItem(player.getLocation(), snapshot));
    }

    private void dropItem(@NotNull Location<World> location, @NotNull ItemStackSnapshot snapshot) {
        Entity item = location.getExtent().createEntity(EntityTypes.ITEM, location.getPosition());
        item.offer(Keys.REPRESENTED_ITEM, snapshot);
        location.getExtent().spawnEntity(item);
    }
}
