package org.eu.hanana.reimu.webui.handler;

import lombok.SneakyThrows;
import org.eu.hanana.reimu.webui.WebUi;
import org.eu.hanana.reimu.webui.core.Util;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

public class StaticHandler implements IRequestHandler{
    public WebUi webUi;
    @SneakyThrows
    @Override
    public int handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse, AtomicReference<Publisher<Void>> out) {
        String path = httpServerRequest.fullPath();
        var code = 403;
        if (!path.startsWith("/static"))
            return -1;
        if (path.startsWith("/static/cp/")){
            out.set(EMPTY_MONO_VOID);
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(path.substring(11));
            if (resourceAsStream!=null){
                out.set(httpServerResponse.addHeader("Content-Type", Util.getMimeType(path)).sendByteArray(Mono.create(monoSink -> {
                    try {
                        byte[] bytes = resourceAsStream.readAllBytes();
                        monoSink.success(bytes);
                    } catch (IOException e) {
                        monoSink.error(e);
                    }
                    try {
                        resourceAsStream.close();
                    } catch (IOException e) {
                        monoSink.error(e);
                    }
                })));
                code=200;
            }else {
                code=404;
            }
        }
        return code;
    }

    @Override
    public void setWebUi(WebUi webUi) {
        this.webUi=webUi;
    }
}
