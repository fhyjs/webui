package org.eu.hanana.reimu.webui.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import org.eu.hanana.reimu.webui.WebUi;
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
}
