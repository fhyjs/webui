package org.eu.hanana.reimu.webui.core;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

public interface IEventCallback {
    void onWsConnected(WebsocketInbound websocketInbound, WebsocketOutbound outbound);
    void onWsReceived(WebsocketInbound websocketInbound, ByteBuf buf, WebsocketOutbound outbound);
}
