package org.eu.hanana.reimu.webui.handler.session;

import org.eu.hanana.reimu.webui.handler.AbstractEasyPathHandler;
import org.eu.hanana.reimu.webui.session.User;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

public class GenNewSessionHandler extends AbstractEasyPathHandler {
    @Override
    public Publisher<Void> process(HttpServerRequest request, HttpServerResponse response) {
        return response.sendString(Mono.create(stringMonoSink -> {
            User user = webUi.getSessionManage().newUser();
            stringMonoSink.success(user.uuid.toString());
        }));
    }

    @Override
    protected String getPath() {
        return "/debug/session/gen";
    }
}
