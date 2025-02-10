package org.eu.hanana.reimu.webui.handler;

import com.google.gson.Gson;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.ArrayList;

public class ListJsEntryHandler extends AbstractPathHandler{
    @Override
    protected String getPath() {
        return "/data/entries.json";
    }

    @Override
    public Publisher<Void> handle(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        return httpServerResponse.status(200).header("content-type","application/json").sendString(Mono.create(stringMonoSink -> {
            ArrayList<IRequestHandler> iRequestHandlers = new ArrayList<>(webUi.handlers);
            ArrayList<String> url = new ArrayList<>();
            for (IRequestHandler iRequestHandler : iRequestHandlers) {
                if (iRequestHandler instanceof AbstractModelJsEntryHandler abstractModelJsEntryHandler){
                    url.add(abstractModelJsEntryHandler.getName());
                }
            }
            stringMonoSink.success(new Gson().toJson(url));
        }));
    }
}
