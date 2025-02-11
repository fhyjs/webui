package org.eu.hanana.reimu.webui.handler.settings;

import com.google.gson.*;
import org.eu.hanana.reimu.webui.core.Util;
import org.eu.hanana.reimu.webui.core.config.DatabaseConfig;
import org.eu.hanana.reimu.webui.handler.AbstractPathHandler;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends AbstractPathHandler {
    @Override
    protected String getPath() {
        return "/data/settings/database.json";
    }

    @Override
    public Publisher<Void> handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return Util.autoContentType(httpServerResponse).status(200).sendString(Mono.create(stringMonoSink -> {
            Util.getAllPostDataString(httpServerRequest).doOnError(stringMonoSink::error).doOnSuccess(s -> {
                JsonObject jo = JsonParser.parseString(s).getAsJsonObject();
                String action = jo.get("action").getAsString();
                var result = new JsonObject();
                if (action.equals("save")){
                    String sqlManagerClass = jo.get("sqlManagerClass").getAsString();
                    String constructorSignature = jo.get("constructorSignature").getAsString();
                    JsonArray argsJson = jo.get("args").getAsJsonArray();
                    List<String> args = new ArrayList<>();
                    for (JsonElement jsonElement : argsJson) {
                        var je=jsonElement.getAsString();
                        args.add(je);
                    }
                    var dbc = new DatabaseConfig(sqlManagerClass,constructorSignature,args);
                    var error=false;
                    try {
                        dbc.getDatabase();
                    }catch (Throwable throwable){
                        error=true;
                        result.add("status",new JsonPrimitive("error"));
                        result.add("msg",new JsonPrimitive("验证失败"+throwable.getCause()));
                        throwable.printStackTrace();
                    }
                    if (!error){
                        webUi.setDatabaseConfig(dbc);
                        result.add("status",new JsonPrimitive("success"));
                        result.add("msg",new JsonPrimitive("success"));
                        dbc.saveToFile(null);
                    }

                }else if(action.equals("get")) {
                    if (webUi.getDatabaseConfig()!=null){
                        result.add("status",new JsonPrimitive("success"));
                        result.add("sqlManagerClass",new JsonPrimitive(webUi.getDatabaseConfig().sqlManagerClass));
                        result.add("constructorSignature",new JsonPrimitive(webUi.getDatabaseConfig().constructorSignature));
                        result.add("constructorArgs",new Gson().toJsonTree(webUi.getDatabaseConfig().constructorArgs));
                    }else{
                        result.add("status",new JsonPrimitive("error"));
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
