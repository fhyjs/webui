package org.eu.hanana.reimu.webui.session;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.hanana.reimu.webui.core.Util;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemorySessionManager implements ISessionManager {
    private static final Logger log = LogManager.getLogger(MemorySessionManager.class);
    public String sessionFieldName = "webui_session";
    @Getter
    public ISessionStorage storage = new MemorySessionStorage();
    protected long expire=60*60;
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);
    public MemorySessionManager(){
        scheduler.scheduleAtFixedRate(this::save,0,30, TimeUnit.SECONDS);
    }
    @Override
    public void save(){
        for (User user : getStorage().getAll()) {
            if (user.data.get("expire_time").getAsBigInteger().longValue()<=Instant.now().getEpochSecond()){
                getStorage().removeUser(user.uuid);
            }
        }
        if (!storage.changed()) return;
        this.storage.save();
    }

    @Override
    public void setExpire(long second) {
        this.expire=second;
    }

    @Override
    public User newUser() {
        var user=new User();
        storage.addUser(user);
        log.info("new user:{}",user.uuid);
        user.data.addProperty("expire_time", Instant.now().plusSeconds(expire).getEpochSecond());
        user.markDirty();
        return user;
    }

    @Override
    public boolean requireSession(HttpServerRequest request, List<String> sessionFreeRules) {
        var fullPath = Paths.get(request.fullPath()).normalize().toString().replace("\\","/");
        for (String reg : sessionFreeRules) {
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(fullPath);
            if (matcher.matches()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkSession(HttpServerRequest request, HttpServerResponse response) {
        var hasUser = true;
        if (request.cookies().containsKey(sessionFieldName)){
            Cookie cookieValue = Util.getCookieValue(request, sessionFieldName);
            if (!storage.hasUser(UUID.fromString(cookieValue.value()))){
                hasUser=false;
            }
        }else{
            hasUser=false;
        }
        User user=null;
        if (!hasUser&&Util.parseQueryParams(request.uri()).get("session")!=null){
            hasUser=true;
            user=storage.getUser(UUID.fromString(Util.parseQueryParams(request.uri()).get("session").getFirst()));
        }
        if (!hasUser){
            user=new User();
            DefaultCookie defaultCookie = new DefaultCookie(sessionFieldName, user.uuid.toString());
            defaultCookie.setPath("/");
            response.addCookie(defaultCookie);
            storage.addUser(user);
            log.info("new user:{}",user.uuid);
            user.data.addProperty("expire_time", Instant.now().plusSeconds(expire).getEpochSecond());
            user.markDirty();
        }else{
            if (user==null) {
                user = storage.getUser(UUID.fromString(Util.getCookieValue(request, sessionFieldName).value()));
            }
            if (!user.data.has("expire_time")){
                user.data.addProperty("expire_time", Instant.now().plusSeconds(expire).getEpochSecond());
            }
            if (user.data.get("expire_time").getAsBigInteger().longValue()<=Instant.now().getEpochSecond()){
                getStorage().removeUser(user.uuid);
            }

        }
        return !hasUser;
    }

    public User getUser(HttpServerRequest httpServerRequest){
        var v = Util.getCookieValue(httpServerRequest, sessionFieldName);
        if (v==null) {
            if (Util.parseQueryParams(httpServerRequest.uri()).get("session")!=null) {
                v = new DefaultCookie("session", Util.parseQueryParams(httpServerRequest.uri()).get("session").getFirst());
            }
        }
        if (v==null) return null;
        return storage.getUser(UUID.fromString(v.value()));
    }

    @Override
    public void close() throws IOException {
        scheduler.close();
        save();
    }
}
