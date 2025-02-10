package org.eu.hanana.reimu.webui.core.database;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.sql.*;

@Getter
public class Sqlite extends AbstractDatabase {
    public final Connection connection;

    /**
     * @param url like"jdbc:sqlite:./a.db"
     */
    @SneakyThrows
    public Sqlite(String url){
        connection = DriverManager.getConnection(url);
    }
}
