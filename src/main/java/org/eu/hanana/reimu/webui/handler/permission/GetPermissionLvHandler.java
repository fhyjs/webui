package org.eu.hanana.reimu.webui.handler.permission;

import org.eu.hanana.reimu.webui.core.Util;
import org.eu.hanana.reimu.webui.handler.AbstractPathHandler;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

public class GetPermissionLvHandler extends AbstractPathHandler {
    @Override
    protected String getPath() {
        return "/data/page_permission.txt";
    }

    @Override
    public Publisher<Void> handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return Util.autoContentType(httpServerResponse).status(200).sendString(Mono.create(stringMonoSink -> {
            Util.getAllPostDataString(httpServerRequest).doOnSuccess(s -> {
                int requiredPermission = webUi.getRequiredPermission(s);
                stringMonoSink.success(String.valueOf(requiredPermission));
            }).doOnError(stringMonoSink::error).subscribe();
        }));
    }
}
