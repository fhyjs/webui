package org.eu.hanana.reimu.webui.core;

import io.netty.buffer.ByteBuf;
import org.eu.hanana.reimu.webui.core.config.DatabaseConfig;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

public interface IEventCallback {
    void onWsConnected(WebsocketInbound websocketInbound, WebsocketOutbound outbound);
    void onWsReceived(WebsocketInbound websocketInbound, ByteBuf buf, WebsocketOutbound outbound);
    void onSetDatabaseConfig(DatabaseConfig databaseConfig);
}
