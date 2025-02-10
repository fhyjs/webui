package org.eu.hanana.reimu.webui.core.database;

import com.google.errorprone.annotations.MustBeClosed;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractDatabase implements IDatabase {
    @Override
    public JsonArray query(String query) throws SQLException {
        try(var stm = getStatement()) {
            ResultSet resultSet = stm.executeQuery(query);
            JsonArray jsonArray = new JsonArray();
            // 遍历结果集
            // 获取列名
            int columnCount = resultSet.getMetaData().getColumnCount();
            Gson gson = new Gson();
            while (resultSet.next()) {
                // 创建一个 JSON 对象来存储当前行数据
                JsonObject jsonObject = new JsonObject();

                for (int i = 1; i <= columnCount; i++) {
                    // 获取列名
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    // 获取列值
                    Object columnValue = resultSet.getObject(i);
                    // 将列名和列值添加到 JSON 对象中
                    jsonObject.add(columnName, gson.toJsonTree(columnValue));
                }

                // 将当前 JSON 对象添加到 JSON 数组中
                jsonArray.add(jsonObject);
            }
            return jsonArray;
        }
    }

    @Override
    public boolean isClose() throws SQLException {
        return getConnection().isClosed()||getStatement().isClosed();
    }
    @MustBeClosed
    @Override
    public Statement getStatement() throws SQLException {
        return getConnection().createStatement();
    }

    @SneakyThrows
    @Override
    public void close() throws IOException {
        getStatement().close();
        getConnection().close();
    }

    @Override
    public boolean hasTable(String name) {
        try {
            query("SELECT 1 FROM " + name + " LIMIT 1");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
