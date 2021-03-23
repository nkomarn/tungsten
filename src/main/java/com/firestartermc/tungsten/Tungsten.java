package com.firestartermc.tungsten;

import com.firestartermc.tungsten.command.IslandCommand;
import com.firestartermc.tungsten.command.VoteCommand;
import com.firestartermc.tungsten.data.SqlStatements;
import com.firestartermc.tungsten.data.SqliteStore;
import com.firestartermc.tungsten.island.IslandGrid;
import com.firestartermc.tungsten.util.ConcurrentUtils;
import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;

@Plugin(id = "tungsten", name = "Tungsten", version = "1.0")
public class Tungsten {

    public static Tungsten INSTANCE;

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File dataDirectory;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private CommentedConfigurationNode config;
    private SqliteStore dataStore;
    private IslandGrid islandGrid;
    
    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        INSTANCE = this;

        // Load and/or copy config into data directory
        // loadConfig();

        // Register data stores
        dataStore = new SqliteStore(dataDirectory, "islands");
        islandGrid = new IslandGrid(this);

        // Create DB for island grid storage
        createTables();

        // Register commands
        new IslandCommand(this);
        new VoteCommand(this);

        Sponge.getEventManager().registerListeners(this, islandGrid);
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

    private void createTables() {
        ConcurrentUtils.callAsync(() -> {
            try (Connection connection = getDataStore().getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement(SqlStatements.CREATE_TABLE)) {
                    statement.executeUpdate();
                }
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }
}
