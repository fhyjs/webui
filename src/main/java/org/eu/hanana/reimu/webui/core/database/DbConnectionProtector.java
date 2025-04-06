package org.eu.hanana.reimu.webui.core.database;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.hanana.reimu.webui.core.ws.SendMessageData;
import org.eu.hanana.reimu.webui.core.ws.WsData;
import org.eu.hanana.reimu.webui.handler.WsHandler;
import reactor.core.publisher.Mono;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;
import reactor.util.function.Tuple2;

import java.sql.SQLException;

public class DbConnectionProtector extends Thread{
    private static final Logger log = LogManager.getLogger(DbConnectionProtector.class);
    private final IDatabase database;

    public DbConnectionProtector(IDatabase database){
        this.database=database;
    }
    @Override
    public void run() {
        while (!this.isInterrupted()){
            try {
                if (database.getConnection().isClosed()){
                    break;
                }
                Thread.sleep(200);
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            log.warn("Database disconnected!");
            for (Tuple2<WebsocketInbound, WebsocketOutbound> value : WsHandler.wsConnections.values()) {
                value.getT2().sendString(Mono.create(stringMonoSink -> {
                    stringMonoSink.success(new Gson().toJson(new WsData("sendMessage",new SendMessageData("错误","数据库连接故障断开"))));
                })).then().subscribe();
            }
            database.open();
            for (Tuple2<WebsocketInbound, WebsocketOutbound> value : WsHandler.wsConnections.values()) {
                value.getT2().sendString(Mono.create(stringMonoSink -> {
                    stringMonoSink.success(new Gson().toJson(new WsData("sendMessage",new SendMessageData("信息","数据库连接重连完成"))));
                })).then().subscribe();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
