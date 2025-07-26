package org.eu.hanana.reimu.webui.session;

import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

public interface ISessionManager extends Closeable {

    /**
     * @param request
     * @param response
     * @return If create new user.
     */
    boolean checkSession(HttpServerRequest request, HttpServerResponse response);
    User getUser(HttpServerRequest httpServerRequest);
    void save();
    void setExpire(long second);
    ISessionStorage getStorage();
    User newUser();
    boolean requireSession(HttpServerRequest request, List<String> sessionFreeRules);
}
