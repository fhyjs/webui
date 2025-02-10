package org.eu.hanana.reimu.webui.handler;

import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.eu.hanana.reimu.webui.WebUi;
import org.eu.hanana.reimu.webui.core.Util;
import org.eu.hanana.reimu.webui.session.User;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class UserDbgHandler implements IRequestHandler{
    private WebUi webUi;
    @Override
    public void setWebUi(WebUi webUi) {
        this.webUi=webUi;
    }

    @SneakyThrows
    @Override
    public int handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse, AtomicReference<Publisher<Void>> out) {
        String path = httpServerRequest.fullPath();
        var code = 200;
        if (!path.equals("/data/user_data.json"))
            return -1;
        out.set(Util.autoContentType(httpServerResponse).status(200).sendString(Mono.create(new Consumer<MonoSink<String>>() {
            @Override
            public void accept(MonoSink<String> stringMonoSink) {
                User user = webUi.getSessionManage().getUser(httpServerRequest);
                stringMonoSink.success(new Gson().toJson(user));
            }
        }), StandardCharsets.UTF_8));
        return code;
    }
}
