package com.firestartermc.tungsten.data;

public class SqlStatements {

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS islands(id INTEGER PRIMARY KEY NOT NULL, region_x INTEGER NOT NULL, region_z INTEGER NOT NULL, spawn_x INTEGER NOT NULL, spawn_y INTEGER NOT NULL, spawn_z INTEGER NOT NULL);";

    public static final String CREATE_ISLAND = "INSERT INTO islands (id, region_x, region_z, spawn_x, spawn_y, spawn_z) VALUES (?, ?, ?, ?, ?, ?);";
    public static final String SELECT_LAST_ID = "SELECT last_insert_rowid();";

    public static final String SELECT_BY_ID = "SELECT * FROM islands WHERE id = ?;";
    public static final String SELECT_BY_REGION = "SELECT * FROM islands WHERE region_x = ? AND region_z = ?;";

    public static final String UDPATE_SPAWN = "UPDATE islands SET spawn_x = ?, spawn_y = ?, spawn_z = ? WHERE id = ?;";
}
