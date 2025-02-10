package org.eu.hanana.reimu.webui.handler;

import org.eu.hanana.reimu.webui.WebUi;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractPathHandler implements IRequestHandler{
    protected abstract String getPath();
    protected WebUi webUi;
    @Override
    public final int handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse, AtomicReference<Publisher<Void>> out) {
        String path = httpServerRequest.fullPath();
        var code = 307;
        if (!path.equals(getPath()))
            return -1;
        out.set(handle(httpServerRequest,httpServerResponse));
        return code;
    }
    public abstract Publisher<Void> handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse);
    @Override
    public void setWebUi(WebUi webUi) {
        this.webUi=webUi;
    }
}
