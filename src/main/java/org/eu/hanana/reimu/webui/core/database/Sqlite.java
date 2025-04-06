package org.eu.hanana.reimu.webui.core.database;

import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.*;

@Getter
public class Sqlite extends AbstractDatabase {
    private final String url;
    public Connection connection;

    /**
     * @param url like"jdbc:sqlite:./a.db"
     */
    @SneakyThrows
    public Sqlite(String url){
        this.url=url;
    }

    @Override
    public void open() throws SQLException {
        try {
            connection = DriverManager.getConnection(url);
        }finally {
            startProtector();
        }


    }
}
