package org.eu.hanana.reimu.webui.core.database;

import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.*;

@Getter
public class Mysql extends AbstractDatabase {
    public Connection connection;
    private final String password;
    private final String username;
    private final String url;

    @SneakyThrows
    public Mysql(String url, String username, String password){
        this.url=url;
        this.username=username;
        this.password=password;
        open();
    }

    @Override
    public void open() throws SQLException {
        try {
            connection = DriverManager.getConnection(url, username, password);
        }finally {
            startProtector();
        }
    }
}
