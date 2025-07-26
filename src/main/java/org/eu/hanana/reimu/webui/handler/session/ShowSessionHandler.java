package org.eu.hanana.reimu.webui.handler.session;

import org.eu.hanana.reimu.webui.handler.AbstractEasyPathHandler;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

public class ShowSessionHandler extends AbstractEasyPathHandler {
    @Override
    public Publisher<Void> process(HttpServerRequest request, HttpServerResponse response) {
        return response.sendString(Mono.create(stringMonoSink -> {
            stringMonoSink.success(webUi.getSessionManage().getUser(request).uuid.toString());
        }));
    }

    @Override
    protected String getPath() {
        return "/debug/session/show_id";
    }
}
