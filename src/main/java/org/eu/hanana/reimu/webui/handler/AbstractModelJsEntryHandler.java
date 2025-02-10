package org.eu.hanana.reimu.webui.handler;

import org.eu.hanana.reimu.webui.WebUi;
import org.eu.hanana.reimu.webui.core.INamed;
import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractModelJsEntryHandler extends AbstractPathHandler implements INamed {
    @Override
    protected  String getPath(){
        return "/data/"+getName()+"/index.js";
    }
    public abstract Publisher<Void> handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse);
}
