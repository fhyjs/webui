package org.eu.hanana.reimu.webui.core.ws;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import lombok.AllArgsConstructor;

import java.lang.reflect.Type;

@JsonAdapter(WsData.WsDataJsonAdapter.class)
public class WsData {
    @Expose
    public final String op;
    @Expose
    public final Object data;
    public final Class<?> dataClass;

    public WsData(String op, Object data) {
        this.op = op;
        this.data = data;
        this.dataClass=data.getClass();
    }
    public WsData(String json,Class<?> dataClass){
        JsonElement jsonElement = JsonParser.parseString(json);
        var op = jsonElement.getAsJsonObject().get("op").getAsString();
        var data = jsonElement.getAsJsonObject().get("data").getAsJsonObject();
        this.op=op;
        this.data=new Gson().fromJson(data,dataClass);
        this.dataClass=dataClass;
    }
    public static class WsDataJsonAdapter implements JsonSerializer<WsData> {

        @Override
        public JsonElement serialize(WsData src, Type typeOfSrc, JsonSerializationContext context) {
            var jo = new JsonObject();
            jo.addProperty("op",src.op);
            jo.add("data",context.serialize(src.data,src.dataClass));
            return jo;
        }
    }
}
