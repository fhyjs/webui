package org.eu.hanana.reimu.webui.handler.settings;

import com.google.gson.*;
import org.eu.hanana.reimu.webui.core.Util;
import org.eu.hanana.reimu.webui.core.config.DatabaseConfig;
import org.eu.hanana.reimu.webui.core.database.IDatabase;
import org.eu.hanana.reimu.webui.handler.AbstractPathHandler;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserHandler extends AbstractPathHandler {
    @Override
    protected String getPath() {
        return "/data/settings/user.json";
    }

    @Override
    public Publisher<Void> handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return Util.autoContentType(httpServerResponse).status(200).sendString(Mono.create(stringMonoSink -> {
            Util.getAllPostDataString(httpServerRequest).doOnError(stringMonoSink::error).doOnSuccess(s -> {
                JsonObject jo = JsonParser.parseString(s).getAsJsonObject();
                String action = jo.get("action").getAsString();
                var result = new JsonObject();
                if (action.equals("add")){
                    if (webUi.getDatabaseConfig()!=null){
                        result.add("status",new JsonPrimitive("success"));
                        IDatabase database = webUi.getDatabaseConfig().getDatabase();
                        try{
                            result.add("data",database.query("INSERT INTO `users` (`id`, `name`, `password`, `nickname`, `permission`) VALUES (?, ?, ?, ?, ?);",
                                    null,
                                    jo.get("username").getAsString(),
                                    jo.get("password").getAsString(),
                                    jo.get("nickname").getAsString(),
                                    jo.get("permission").getAsInt()
                                    )
                            );
                            result.add("status",new JsonPrimitive("success"));
                        }catch (Throwable throwable){
                            result.add("status",new JsonPrimitive("error"));
                            result.add("msg",new JsonPrimitive(throwable.toString()));
                        }
                    }else{
                        result.add("status",new JsonPrimitive("error"));
                        result.add("msg",new JsonPrimitive("数据库未配置"));
                    }
                }else if(action.equals("get")) {
                    if (webUi.getDatabaseConfig()!=null){
                        result.add("status",new JsonPrimitive("success"));
                        IDatabase database = webUi.getDatabaseConfig().getDatabase();
                        try{
                            result.add("data",database.query("SELECT * FROM `users`"));
                            result.add("status",new JsonPrimitive("success"));
                        }catch (Throwable throwable){
                            result.add("status",new JsonPrimitive("error"));
                            result.add("msg",new JsonPrimitive(throwable.toString()));
                        }
                    }else{
                        result.add("status",new JsonPrimitive("error"));
                        result.add("msg",new JsonPrimitive("数据库未配置"));
                    }

                }else{
                    result.add("status",new JsonPrimitive("error"));
                    result.add("msg",new JsonPrimitive("未指定操作"));
                }
                stringMonoSink.success(new Gson().toJson(result));
            }).subscribe();
        }));
    }
}
