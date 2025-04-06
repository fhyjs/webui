package org.eu.hanana.reimu.webui.handler;

import com.google.gson.Gson;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.eu.hanana.reimu.webui.core.ws.WsData;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.netty.http.websocket.WebsocketOutbound;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WsHandler extends AbstractPathHandler{
    public static final Map<HttpServerRequest,Tuple2<WebsocketInbound, WebsocketOutbound>> wsConnections = new HashMap<>();
    @Override
    protected String getPath() {
        return "/ws";
    }

    @Override
    public Publisher<Void> handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return httpServerResponse.sendWebsocket((websocketInbound, websocketOutbound) -> {
            return Mono.<Void>create(voidMonoSink -> {
                var gson = new Gson().newBuilder().excludeFieldsWithoutExposeAnnotation().create();
                websocketInbound.receive()  // 获取接收到的帧
                        .map(TextWebSocketFrame::new)  // 获取帧内容
                        .doOnNext(message -> {
                            websocketOutbound.sendString(Mono.just(message.text())).then().subscribe();
                            if (message.text().equals("exit")) voidMonoSink.success();
                        })
                        .doOnComplete(() -> {
                            // 接收流完成时（如连接关闭）确保完成 Mono
                            voidMonoSink.success();
                            wsConnections.remove(httpServerRequest);
                        })
                        .subscribe();
                websocketOutbound.sendString(Mono.just(gson.toJson(new WsData("connected","")))).then().subscribe();
                wsConnections.put(httpServerRequest,Tuples.of(websocketInbound,websocketOutbound));
                webUi.dispatchEvent("onWsConnected",websocketInbound,websocketOutbound);
            });
        });
    }
}
