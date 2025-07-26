package org.eu.hanana.reimu.webui.handler.debug;

import org.eu.hanana.reimu.webui.handler.AbstractEasyPathHandler;
import org.eu.hanana.reimu.webui.handler.AbstractPathHandler;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.function.Supplier;

public class ErrorHandler extends AbstractEasyPathHandler {
    @Override
    protected String getPath() {
        return "/debug/error";
    }

    @Override
    public Publisher<Void> process(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return Mono.defer(() -> {
            // 把可能抛出异常的逻辑包在 defer 中，避免在链外执行
            return httpServerResponse
                    .sendString(Mono.fromCallable(() -> {
                        throw new RuntimeException("TEST ERROR");
                    }))
                    .then();
        });
    }
}
