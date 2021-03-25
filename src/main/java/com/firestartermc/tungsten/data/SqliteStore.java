package com.firestartermc.tungsten.data;

import com.firestartermc.tungsten.util.ConcurrentUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A utility for easily creating SQLite databases which allow plugins to store data locally.
 * This class should be instantiated per database file.
 */
public class SqliteStore {

    private final String name;
    private final String location;

    public SqliteStore(@NotNull File directory, String name) {
        this.name = name;
        this.location = String.format("%s/dbs/%s.db", directory.toString(), name);

        try {
            Files.createDirectories(directory.toPath().resolve("dbs"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Connection connection = getConnection()) {
            // Do nothing - connection is successful
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create local database.", e);
        }

        createTables();
    }

    /**
     * Returns the name of this local storage database.
     *
     * @return The name of this local storage.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Returns a new connection to the local database.
     *
     * @return A new connection to the local database.
     * @throws SQLException Exception that may occur while creating a new connection.
     */
    @NotNull
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + location);
    }

    private void createTables() {
        ConcurrentUtils.callAsync(() -> {
            try (Connection connection = getConnection()) {
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