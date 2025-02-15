package org.eu.hanana.reimu.webui.session;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class LocalSessionManager extends MemorySessionManager{
    private static final Logger log = LogManager.getLogger(LocalSessionManager.class);
    protected final File file;
    @SneakyThrows
    public LocalSessionManager(){
        file = Path.of("session.json").toFile();
        if (!file.exists()){
            file.createNewFile();
        }
        if (file.isDirectory()){
            file.delete();
            file.createNewFile();
        }
        load();
    }
    @SneakyThrows
    public void load(){
        String s = Files.readString(file.toPath());
        List<User> users= new ArrayList<>();
        try {
            Object o = new Gson().fromJson(s, TypeToken.getParameterized(List.class, User.class));
            if (o instanceof List<?> list){
                users= (List<User>) list;
            }
            List<User> all = storage.getAll();
            for (User user : all) {
                storage.removeUser(user.uuid);
            }
            for (User user : users) {
                storage.addUser(user);
            }
        } catch (Exception e) {
            log.error("Can not read session storage {}.Auto deleted it!",file.toString());
            save();
        }
    }
    @SneakyThrows
    @Override
    public void save() {
        if (!storage.changed())return;
        super.save();
        Files.writeString(file.toPath(),new Gson().toJson(storage.getAll()));
    }
}
