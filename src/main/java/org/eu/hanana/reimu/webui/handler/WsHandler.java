package org.eu.hanana.reimu.webui.handler;

import com.google.gson.Gson;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.eu.hanana.reimu.webui.core.ws.WsData;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

public class WsHandler extends AbstractPathHandler{
    @Override
    protected String getPath() {
        return "/ws";
    }

    @Override
    public Publisher<Void> handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return httpServerResponse.sendWebsocket((websocketInbound, websocketOutbound) -> {
            return Mono.create(voidMonoSink -> {
                var gson = new Gson().newBuilder().excludeFieldsWithoutExposeAnnotation().create();
                websocketInbound.receive()  // 获取接收到的帧
                        .map(TextWebSocketFrame::new)  // 获取帧内容
                        .doOnNext(message -> {
                            websocketOutbound.sendString(Mono.just(message.text())).then().subscribe();
                            if (message.text().equals("exit")) voidMonoSink.success();
                        })
                        .subscribe();
                websocketOutbound.sendString(Mono.just(gson.toJson(new WsData("connected","")))).then().subscribe();
                webUi.dispatchEvent("onWsConnected",websocketInbound,websocketOutbound);
            });
        });
    }
}
