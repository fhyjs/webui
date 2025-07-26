package org.eu.hanana.reimu.webui.handler;

import org.eu.hanana.reimu.webui.core.Util;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class SWJsEntryHandler extends AbstractPathHandler{
    public static final List<String> SW_MODEL_PATH =  new ArrayList<>();
    @Override
    protected String getPath() {
        return "/data/sw/main.js";
    }

    @Override
    public Publisher<Void> handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return Util.autoContentType(httpServerResponse).status(200).sendString(Mono.create(stringMonoSink -> {
            var sb = new StringBuilder();
            for (String s : SW_MODEL_PATH) {
                sb.append("importScripts('").append(s).append("');\n");
            }
            stringMonoSink.success(sb.toString());
        }));
    }
}
