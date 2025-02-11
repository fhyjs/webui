package org.eu.hanana.reimu.webui.core.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eu.hanana.reimu.webui.core.database.control.TableData;

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
    void createTableFromJson(TableData jsonConfig) throws SQLException;
    void createTableFromJson(JsonObject jsonConfig) throws SQLException;
    JsonArray query(String query,Object... args) throws SQLException;
}
