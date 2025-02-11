package org.eu.hanana.reimu.webui.core;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eu.hanana.reimu.webui.WebUi;
import org.eu.hanana.reimu.webui.core.config.DatabaseConfig;
import org.eu.hanana.reimu.webui.core.database.control.ColumnData;
import org.eu.hanana.reimu.webui.core.database.control.TableData;
import org.eu.hanana.reimu.webui.core.ws.SendMessageData;
import org.eu.hanana.reimu.webui.core.ws.WsData;
import reactor.core.publisher.Mono;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

@RequiredArgsConstructor
public class WebuiEventCallback extends EventCallback{
    protected final WebUi webUi;
    @Override
    public void onWsConnected(WebsocketInbound websocketInbound, WebsocketOutbound outbound) {
        super.onWsConnected(websocketInbound, outbound);
        Gson gson = new Gson();
        if (webUi.getDatabaseConfig()==null){
            outbound.sendString(Mono.create(stringMonoSink -> {
                stringMonoSink.success(gson.toJson(new WsData("sendMessage",new SendMessageData("警告","未设置数据库后端配置,使用任何用户登录后均为管理员,请立即在设置页面配置数据库后端!"))));
            })).then().subscribe();
        }
    }

    @Override
    public void onWsReceived(WebsocketInbound websocketInbound, ByteBuf buf, WebsocketOutbound outbound) {
        super.onWsReceived(websocketInbound, buf, outbound);
    }

    @SneakyThrows
    @Override
    public void onSetDatabaseConfig(DatabaseConfig databaseConfig) {
        super.onSetDatabaseConfig(databaseConfig);
        var db = databaseConfig.getDatabase();
        if (!db.hasTable("users")){
            db.createTableFromJson(new TableData("users").addColumns(
                    new ColumnData("id","int").setAutoIncrement(true).setNotNull(true).setPrimaryKey(true).setUnique(true),
                    new ColumnData("name","VARCHAR(255)").setNotNull(true).setUnique(true),
                    new ColumnData("password","VARCHAR(255)").setNotNull(true),
                    new ColumnData("nickname","VARCHAR(255)"),
                    new ColumnData("permission","int").setNotNull(true)
            ));
        }
    }
}
