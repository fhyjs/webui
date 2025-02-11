package org.eu.hanana.reimu.webui.core.database;

import com.google.errorprone.annotations.MustBeClosed;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.eu.hanana.reimu.webui.core.database.control.TableData;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.eu.hanana.reimu.webui.core.Util.parseColumn;

public abstract class AbstractDatabase implements IDatabase {
    @Override
    public JsonArray query(String query) throws SQLException {
        try(var stm = getStatement()) {
            if (stm.execute(query)) {
                ResultSet rowsAffected = stm.getResultSet();
                return resultToJson(rowsAffected);
            }else {
                int updateCount = stm.getUpdateCount();
                var ja = new JsonArray();
                ja.add(updateCount);
                return ja;
            }
        }
    }

    @Override
    public JsonArray query(String query, Object... args) throws SQLException {
        // 创建 PreparedStatement 对象
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            // 设置插入的值
            for (int i = 0; i < args.length; i++) {
                stmt.setObject(i+1,args[i]);
            }
            if (stmt.execute()) {
                ResultSet rowsAffected = stmt.getResultSet();
                return resultToJson(rowsAffected);
            }else {
                int updateCount = stmt.getUpdateCount();
                var ja = new JsonArray();
                ja.add(updateCount);
                return ja;
            }
        }
    }
    public JsonArray resultToJson(ResultSet resultSet) throws SQLException {
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
    public List<String> getAllTables() throws SQLException {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = getConnection().getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (resultSet.next()) {
                tables.add(resultSet.getString("TABLE_NAME"));
            }
        }
        return tables;
    }
    public void createTableFromJson(TableData jsonConfig) throws SQLException {
        createTableFromJson(new Gson().toJsonTree(jsonConfig).getAsJsonObject());
    }
    public void createTableFromJson(JsonObject jsonConfig) throws SQLException {
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(jsonConfig, JsonObject.class);

        String tableName = root.get("table").getAsString();
        JsonArray columns = root.getAsJsonArray("columns");

        // 获取数据库类型
        DatabaseMetaData metaData = getConnection().getMetaData();
        String dbName = metaData.getDatabaseProductName().toLowerCase();

        // 解析列定义
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        for (int i = 0; i < columns.size(); i++) {
            JsonObject column = columns.get(i).getAsJsonObject();
            sql.append(parseColumn(column, dbName));
            if (i < columns.size() - 1) sql.append(", ");
        }
        sql.append(");");

        // 执行 SQL 语句
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate(sql.toString());
        }
    }
}
