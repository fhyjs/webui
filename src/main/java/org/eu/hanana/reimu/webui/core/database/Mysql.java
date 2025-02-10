package org.eu.hanana.reimu.webui.core.database;

import com.google.gson.*;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.sql.*;

@Getter
public class Mysql extends AbstractDatabase {
    public final Connection connection;

    @SneakyThrows
    public Mysql(String url, String username, String password){
        connection = DriverManager.getConnection(url, username, password);
    }
}
