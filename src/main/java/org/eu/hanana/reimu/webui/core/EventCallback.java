package org.eu.hanana.reimu.webui.core;

import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Mono;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;

public abstract class EventCallback implements IEventCallback{
    @Override
    public void onWsConnected(WebsocketInbound websocketInbound, WebsocketOutbound outbound) {
    }

    @Override
    public void onWsReceived(WebsocketInbound websocketInbound, ByteBuf buf, WebsocketOutbound outbound) {

    }
}
