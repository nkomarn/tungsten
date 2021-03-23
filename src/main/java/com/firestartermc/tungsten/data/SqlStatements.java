package com.firestartermc.tungsten.data;

public class SqlStatements {

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS islands(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, team_id INTEGER NOT NULL, region TEXT NOT NULL, spawn_x INTEGER NOT NULL, spawn_y INTEGER NOT NULL, spawn_z INTEGER NOT NULL);";
    public static final String SELECT_ISLAND = "SELECT * FROM islands WHERE team_id = ?;";
    public static final String CREATE_ISLAND = "INSERT INTO islands (team_id, region, spawn_x, spawn_y, spawn_z) VALUES (?, ?, ?, ?, ?);";
    public static final String SELECT_LAST_ID = "SELECT last_insert_rowid();";
    public static final String SELECT_BY_REGION = "SELECT * FROM islands WHERE region = ?;";
}
