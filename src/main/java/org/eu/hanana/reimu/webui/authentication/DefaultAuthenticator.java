package org.eu.hanana.reimu.webui.authentication;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import org.eu.hanana.reimu.webui.session.ISessionManager;
import org.eu.hanana.reimu.webui.session.User;
import reactor.netty.http.server.HttpServerRequest;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.function.Function;

@RequiredArgsConstructor
public class DefaultAuthenticator implements IAuthenticator{
    public final ISessionManager sessionManager;
    public Function<Tuple2<HttpServerRequest, Integer>, Boolean> checker;
    protected String permissionFieldId="permission";
    @Override
    public int getUserLevel(HttpServerRequest request) {
        User user = sessionManager.getUser(request);
        if (user==null) return 0;
        if (!user.data.has(permissionFieldId)) {
            user.data.add(permissionFieldId,new JsonPrimitive(0));
            user.markDirty();
        }
        return user.data.get(permissionFieldId).getAsInt();
    }

    @Override
    public boolean isPass(HttpServerRequest request) {
        if (checker!=null)
            return checker.apply(Tuples.of(request,getUserLevel(request)));
        return true;
    }

    @Override
    public void setChecker(Function<Tuple2<HttpServerRequest, Integer>, Boolean> checker) {
        this.checker=checker;
    }
}
