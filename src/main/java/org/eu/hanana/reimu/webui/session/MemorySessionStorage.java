package org.eu.hanana.reimu.webui.session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MemorySessionStorage implements ISessionStorage {
    protected final Map<UUID, User> users = new ConcurrentHashMap<>();
    protected boolean dirty = false;
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
        dirty=true;
    }

    @Override
    public void addUser(User user) {
        users.put(user.uuid, user);
        dirty=true;
    }

    @Override
    public boolean hasUser(UUID uuid) {
        return users.containsKey(uuid);
    }

    @Override
    public void save() {
        dirty=false;
        for (User user : users.values()) {
            user.dirty = false;
        }
    }

    @Override
    public boolean changed() {
        var f = false;
        for (User user : users.values()) {
            if (user.dirty) {
                f = true;
                break;
            }
        }
        return dirty||f;
    }
}
