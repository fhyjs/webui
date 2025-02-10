package org.eu.hanana.reimu.webui.handler;

import org.eu.hanana.reimu.webui.WebUi;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.concurrent.atomic.AtomicReference;

public interface IRequestHandler{
    Mono<Void> EMPTY_MONO_VOID = Mono.<Void>create(MonoSink::success);
    int handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse, AtomicReference<Publisher<Void>> out);
    void setWebUi(WebUi webUi);
}
