package org.eu.hanana.reimu.webui.core;

import io.netty.buffer.ByteBuf;
import org.eu.hanana.reimu.webui.core.config.DatabaseConfig;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

public abstract class EventCallback implements IEventCallback{
    @Override
    public void onWsConnected(WebsocketInbound websocketInbound, WebsocketOutbound outbound) {
    }

    @Override
    public void onWsReceived(WebsocketInbound websocketInbound, ByteBuf buf, WebsocketOutbound outbound) {

    }

    @Override
    public void onSetDatabaseConfig(DatabaseConfig databaseConfig) {

    }

    @Override
    public void onConnection(Connection connection) {

    }
}
