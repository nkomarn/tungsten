package com.firestartermc.tungsten;

import com.firestartermc.tungsten.claim.InjectedClaimedChunks;
import com.firestartermc.tungsten.command.IslandCommand;
import com.firestartermc.tungsten.command.VoteCommand;
import com.firestartermc.tungsten.data.SqliteStore;
import com.firestartermc.tungsten.island.IslandGrid;
import com.firestartermc.tungsten.listener.VoteListener;
import com.google.inject.Inject;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id = "tungsten", name = "Tungsten", version = "1.0", dependencies = @Dependency(id = "worldedit"))
public class Tungsten implements Runnable {

    public static Tungsten INSTANCE;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File dataDirectory;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    private EventManager eventManager;

    private CommentedConfigurationNode config;
    private SqliteStore dataStore;
    private IslandGrid islandGrid;
    private World islandWorld;
    private Clipboard islandSchematic;
    
    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        INSTANCE = this;
        dataStore = new SqliteStore(dataDirectory, "islands");
        islandGrid = new IslandGrid(this);

        new IslandCommand(this);
        new VoteCommand(this);

        eventManager.registerListeners(this, islandGrid);
        eventManager.registerListeners(this, new VoteListener());

        // Run delayed startup tasks
        Task.builder()
                .delayTicks(20L)
                .execute(this)
                .submit(this);
    }

    @Override
    public void run() {
        islandWorld = Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get();

        // Load in the island schematic
        try {
            File schematic = new File(dataDirectory, "island.schematic");
            ClipboardReader reader = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(schematic));
            islandSchematic = reader.read(SpongeWorldEdit.inst().getAdapter().getWorld(islandWorld).getWorldData());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Inject custom FTB utils chunk manager
        InjectedClaimedChunks.inject();
    }

    @NotNull
    public CommentedConfigurationNode getConfig() {
        return config;
    }

    @NotNull
    public SqliteStore getDataStore() {
        return dataStore;
    }

    @NotNull
    public IslandGrid getIslandGrid() {
        return islandGrid;
    }

    @NotNull
    public World getIslandWorld() {
        return islandWorld;
    }

    @NotNull
    public Clipboard getIslandSchematic() {
        return islandSchematic;
    }

    private void loadConfig() {
        try {
            if (!Files.exists(defaultConfig)) {
                Sponge.getAssetManager().getAsset(this, "config.conf").get().copyToFile(defaultConfig);
            }

            config = configManager.load();
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }
}
