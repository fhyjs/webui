package org.eu.hanana.reimu.webui.session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MemorySessionStorage implements ISessionStorage {
    protected final Map<UUID, User> users = new ConcurrentHashMap<>();

    @Override
    public User getUser(UUID uuid) {
        return users.get(uuid);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void removeUser(UUID uuid) {
        users.remove(uuid);
    }

    @Override
    public void addUser(User user) {
        users.put(user.uuid, user);
    }

    @Override
    public boolean hasUser(UUID uuid) {
        return users.containsKey(uuid);
    }

    @Override
    public void save() {
        for (User user : users.values()) {
            user.dirty = false;
        }
    }

    @Override
    public boolean changed() {
        var f = false;
        for (User user : users.values()) {
            if (user.dirty) f=true;
        }
        return f;
    }
}
