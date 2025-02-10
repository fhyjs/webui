package org.eu.hanana.reimu.webui.authentication;

import reactor.netty.http.server.HttpServerRequest;
import reactor.util.function.Tuple2;

import java.util.function.Function;

public interface IAuthenticator {
    int getUserLevel(HttpServerRequest request);
    boolean isPass(HttpServerRequest request);
    void setChecker(Function<Tuple2<HttpServerRequest,Integer>,Boolean> checker);
}
