package org.eu.hanana.reimu.webui.core.database;

import com.google.gson.JsonArray;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public interface IDatabase extends Closeable {
    JsonArray query(String query) throws SQLException;
    boolean isClose() throws SQLException;
    Connection getConnection();
    Statement getStatement() throws SQLException;
    boolean hasTable(String name);
}
