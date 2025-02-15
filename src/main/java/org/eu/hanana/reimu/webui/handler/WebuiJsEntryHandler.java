package org.eu.hanana.reimu.webui.handler;

import org.eu.hanana.reimu.webui.core.Util;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

public class WebuiJsEntryHandler extends AbstractModelJsEntryHandler{
    @Override
    public String getName() {
        return "webui";
    }

    @Override
    public Publisher<Void> handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return Util.sendRedirect(httpServerResponse,httpServerRequest.uri()+"../../../../static/cp/webui/assets/webui.js").send();
    }
}
