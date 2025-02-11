package org.eu.hanana.reimu.webui.core.config;

import com.google.gson.Gson;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.eu.hanana.reimu.webui.core.Util;
import org.eu.hanana.reimu.webui.core.database.IDatabase;
import org.eu.hanana.reimu.webui.core.database.Sqlite;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
public class DatabaseConfig {
    public String sqlManagerClass= Sqlite.class.getName();
    public String constructorSignature = Util.getDescriptor(Sqlite.class.getConstructors()[0]);
    public List<String> constructorArgs = Collections.singletonList("jdbc:sqlite:./webui.db");
    protected IDatabase database=null;
    public DatabaseConfig(String sqlManagerClass,String constructorSignature,List<String> constructorArgs){
        this.sqlManagerClass=sqlManagerClass;
        this.constructorArgs=constructorArgs;
        this.constructorSignature=constructorSignature;
    }
    @SneakyThrows
    public IDatabase getDatabase(){
        if (database==null){
            Class<?> smc = Class.forName(sqlManagerClass);
            var constructor = Util.findConstructor(smc,constructorSignature);
            database = (IDatabase) constructor.newInstance(constructorArgs.toArray());
        }
        return database;
    }
    @SneakyThrows
    public void saveToFile(File file){
        if (file==null) file=new File("sql_config.json");
        var pth = file.toPath();
        if (!Files.exists(pth)) Files.createFile(pth);
        if (file.isDirectory()){
            file.delete();
            Files.createFile(pth);
        }
        var gson = new Gson();
        Files.writeString(pth,gson.toJson(this));
    }
    public static DatabaseConfig loadFromFile(File file) throws IOException {
        if (file==null) file=new File("sql_config.json");
        String s = Files.readString(file.toPath());
        return new Gson().fromJson(s, DatabaseConfig.class);
    }
}
