package org.eu.hanana.reimu.webui.session;

import java.util.List;
import java.util.UUID;

public interface ISessionStorage {
    User getUser(UUID uuid);
    List<User> getAll();
    void removeUser(UUID uuid);
    void addUser(User user);
    boolean hasUser(UUID uuid);
    void save();
    public boolean changed();
}
