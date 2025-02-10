package org.eu.hanana.reimu.webui.handler;

import com.google.gson.Gson;
import org.eu.hanana.reimu.webui.WebUi;
import org.eu.hanana.reimu.webui.session.User;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HomeRedirect implements IRequestHandler{
    @Override
    public int handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse, AtomicReference<Publisher<Void>> out) {
        String path = httpServerRequest.fullPath();
        var code = 307;
        if (!path.equals("/"))
            return -1;
        out.set(httpServerResponse.addHeader("Location","/static/cp/webui/index.html").status(307).send());
        return code;
    }

    @Override
    public void setWebUi(WebUi webUi) {

    }
}
