package org.eu.hanana.reimu.webui.handler.user;

import com.google.gson.*;
import org.eu.hanana.reimu.webui.core.Util;
import org.eu.hanana.reimu.webui.core.database.Mysql;
import org.eu.hanana.reimu.webui.handler.AbstractPathHandler;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class LoginHandler extends AbstractPathHandler {
    @Override
    protected String getPath() {
        return "/data/user/login.json";
    }

    @Override
    public Publisher<Void> handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return Util.autoContentType(httpServerResponse).status(200).sendString(Mono.create(stringMonoSink -> {
            var user = webUi.getSessionManage().getUser(httpServerRequest);
            Mono<String> stringMono = httpServerRequest.receive()
                    .aggregate()
                    .asString(StandardCharsets.UTF_8);
            stringMono.doOnSuccess(s -> {
                Gson gson = new Gson();
                JsonObject jsonObject = JsonParser.parseString(s).getAsJsonObject();
                String action = jsonObject.get("action").getAsString();
                user.markDirty();
                if (action.equals("logout")){
                    Set<String> strings = new HashSet<>(user.data.keySet());
                    for (String string : strings) {
                        user.data.remove(string);
                    }
                    stringMonoSink.success("{\"status\":\"success\",\"msg\":\"已经退出\"}");
                }else if (action.equals("login")){
                    var username = jsonObject.get("username").getAsString();
                    var password = jsonObject.get("password").getAsString();
                    if (username.isEmpty()){
                        stringMonoSink.success("{\"status\":\"error\",\"msg\":\"用户名不能为空\"}");
                        return;
                    }
                    if (webUi.getDatabaseConfig()==null) {
                        user.data.add("username", new JsonPrimitive(username));
                        user.data.addProperty("permission",Integer.MAX_VALUE);
                        stringMonoSink.success("{\"status\":\"success\",\"msg\":\"未设置数据库!!!登录成功\"}");
                        return;
                    }else {

                    }
                }else{
                    stringMonoSink.success("{\"status\":\"error\",\"msg\":\"未指定操作\"}");
                }
            }).doOnError(stringMonoSink::error).subscribe();
        }));
    }
}
