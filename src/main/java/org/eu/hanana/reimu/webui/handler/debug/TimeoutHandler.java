package org.eu.hanana.reimu.webui.handler.debug;

import org.eu.hanana.reimu.webui.core.Util;
import org.eu.hanana.reimu.webui.handler.AbstractEasyPathHandler;
import org.eu.hanana.reimu.webui.handler.AbstractPathHandler;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

public class TimeoutHandler extends AbstractEasyPathHandler {
    @Override
    protected String getPath() {
        return "/debug/timeout";
    }

    @Override
    public Publisher<Void> process(HttpServerRequest request, HttpServerResponse response) {
        return response.sendString(Mono.create(stringMonoSink -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            stringMonoSink.success(String.format("Finished on %s",Thread.currentThread()));
        }));
    }
}
