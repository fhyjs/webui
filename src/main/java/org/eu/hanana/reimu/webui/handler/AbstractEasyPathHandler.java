package org.eu.hanana.reimu.webui.handler;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

public abstract class AbstractEasyPathHandler extends AbstractPathHandler{
    @Override
    public Publisher<Void> handle(HttpServerRequest request, HttpServerResponse response) {
        return Mono.defer(() -> Mono.from(this.process(request, response)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public abstract Publisher<Void> process(HttpServerRequest request, HttpServerResponse response);
}
