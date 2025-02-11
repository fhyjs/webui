package org.eu.hanana.reimu.webui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.eu.hanana.reimu.webui.authentication.DefaultAuthenticator;
import org.eu.hanana.reimu.webui.authentication.IAuthenticator;
import org.eu.hanana.reimu.webui.authentication.account.AccountManager;
import org.eu.hanana.reimu.webui.core.IEventCallback;
import org.eu.hanana.reimu.webui.core.INamed;
import org.eu.hanana.reimu.webui.core.Util;
import org.eu.hanana.reimu.webui.core.WebuiEventCallback;
import org.eu.hanana.reimu.webui.core.config.DatabaseConfig;
import org.eu.hanana.reimu.webui.handler.*;
import org.eu.hanana.reimu.webui.handler.permission.GetPermissionLvHandler;
import org.eu.hanana.reimu.webui.handler.settings.DatabaseHandler;
import org.eu.hanana.reimu.webui.handler.settings.UserHandler;
import org.eu.hanana.reimu.webui.handler.user.LoginHandler;
import org.eu.hanana.reimu.webui.session.LocalSessionManager;
import org.eu.hanana.reimu.webui.session.MemorySessionManager;
import org.eu.hanana.reimu.webui.session.ISessionManager;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@RequiredArgsConstructor
public class WebUi implements Closeable {
    private static final Mono<Void> EMPTY_MONO_VOID = IRequestHandler.EMPTY_MONO_VOID;
    public HttpServer httpServer;
    public DisposableServer disposableServer;
    public final String host;
    public final int port;
    private boolean firstOpen = true;
    @Getter
    protected DatabaseConfig databaseConfig=null;
    @Getter
    protected AccountManager accountManager;
    @Getter
    protected ISessionManager sessionManage = new LocalSessionManager();
    public IAuthenticator authenticator = new DefaultAuthenticator(sessionManage);
    protected final Map<String,Integer> pathPermission = new HashMap<>();
    protected final List<IEventCallback> eventCallbacks = new ArrayList<>();
    public void open(boolean sync){
        open(sync,firstOpen);
    }
    public void addPermissionRule(String regex,int permissionLv){
        pathPermission.put(regex,permissionLv);
    }
    public final List<IRequestHandler> handlers = new ArrayList<>(){
        @Override
        public void add(int index, IRequestHandler element) {
            element.setWebUi(WebUi.this);
            super.add(index, element);
        }

        @Override
        public boolean add(IRequestHandler iRequestHandler) {
            iRequestHandler.setWebUi(WebUi.this);
            return super.add(iRequestHandler);
        }

        @Override
        public boolean addAll(Collection<? extends IRequestHandler> c) {
            for (IRequestHandler iRequestHandler : c) {
                iRequestHandler.setWebUi(WebUi.this);
            }
            return super.addAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends IRequestHandler> c) {
            for (IRequestHandler iRequestHandler : c) {
                iRequestHandler.setWebUi(WebUi.this);
            }
            return super.addAll(index, c);
        }
    };

    @SneakyThrows
    public void setSessionManage(ISessionManager sessionManage) {
        if (this.sessionManage!=null)this.sessionManage.close();
        this.sessionManage = sessionManage;
    }

    @SneakyThrows
    public void open(boolean sync, boolean first){
        if (first){
            handlers.add(new StaticHandler());
            handlers.add(new UserDbgHandler());
            handlers.add(new HomeRedirect());
            handlers.add(new ListJsEntryHandler());
            handlers.add(new WebuiJsEntryHandler());
            handlers.add(new LoginHandler());
            handlers.add(new GetPermissionLvHandler());
            handlers.add(new WsHandler());
            handlers.add(new DatabaseHandler());
            handlers.add(new UserHandler());
            addPermissionRule("^/data/settings/.*",2);
            addPermissionRule("^/static/cp/webui/pages/settings.html",2);
            authenticator.setChecker(this::hasPermission);
            setAccountManager(new AccountManager());
            addEventCallback(new WebuiEventCallback(this));
            if (Files.exists(Path.of("sql_config.json"))) {
                setDatabaseConfig(DatabaseConfig.loadFromFile(null));
                log.info("database loaded!");
            }
            Files.writeString(Path.of("recovery_password.txt"), Util.generateRandomString(20));
        }
        firstOpen=false;
        log.info("Opening webui at {}:{},first:{},sync:{}",host,port,first,sync);
        AtomicBoolean finish = new AtomicBoolean(false);
        AtomicReference<Throwable> throwable = new AtomicReference<>(null);

        (httpServer = HttpServer.create().host(host).port(port).protocol(HttpProtocol.HTTP11,HttpProtocol.H2C)
                .handle(this::handle)).bind().doOnSuccess(disposableServer1 -> {
            log.info("Success!");
            this.disposableServer=disposableServer1;
            finish.set(true);
        }).doOnError(message -> {
            log.fatal(message);
            throwable.set(message);
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
        if (sync){
            while (!finish.get()){
                Thread.sleep(100);
            }
        }
        if (throwable.get()!=null) throw new RuntimeException(throwable.get());
    }
    @SneakyThrows
    public void dispatchEvent(String name, Object... args){
        var events = new ArrayList<>(eventCallbacks);
        for (IEventCallback event : events) {
            Method[] methods = event.getClass().getMethods();
            for (Method method : methods) {
                if (!method.getName().equals(name)) continue;
                if (method.getParameterCount()!=args.length) continue;
                try {
                    method.invoke(event,args);
                } catch (InvocationTargetException | IllegalAccessException exception) {
                    if (exception instanceof InvocationTargetException){
                        throw (InvocationTargetException) exception;
                    }
                }
            }
        }
    }

    public void setDatabaseConfig(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        dispatchEvent("onSetDatabaseConfig",databaseConfig);
    }

    public void addEventCallback(IEventCallback eventCallback){
        eventCallbacks.add(eventCallback);
    }
    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
        accountManager.setWebui(this);
    }

    public boolean hasPermission(Tuple2<HttpServerRequest, Integer> input){
        return getRequiredPermission(input.getT1())<= input.getT2();
    }
    public int getRequiredPermission(String fullPath){
        fullPath = Paths.get(fullPath).normalize().toString().replace("\\","/");
        for (String reg : pathPermission.keySet()) {
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(fullPath);
            if (matcher.matches()) {
                return pathPermission.get(reg);
            }
        }
        return 0;
    }
    public int getRequiredPermission(HttpServerRequest request){
        return getRequiredPermission(request.fullPath());
    }
    private Publisher<Void> handle(HttpServerRequest request, HttpServerResponse httpServerResponse) {
        var stop = false;
        if (sessionManage!=null){
            stop = sessionManage.checkSession(request,httpServerResponse)||stop;
            if (stop){
                sessionManage.save();
            }
        }
        Tuple2<Publisher<Void>,Integer> out = null;
        if(!stop) {
            if (authenticator == null || authenticator.isPass(request)) {
                out = findAndHandler(request, httpServerResponse);
            } else {
                out = Tuples.of(EMPTY_MONO_VOID, 401);
            }
        }else {
            httpServerResponse.addHeader("Location",request.uri());
            out = Tuples.of(EMPTY_MONO_VOID, 307);
        }
        var toSend = out.getT1();
        if (toSend == EMPTY_MONO_VOID) {
            log.info("Error handling request {},return {}.", request.fullPath(), out.getT2());
            Tuple2<Publisher<Void>, Integer> finalOut = out;
            toSend = httpServerResponse.status(out.getT2()).sendString(Mono.create(stringMonoSink -> {
                try {
                    var is = WebUi.class.getClassLoader().getResourceAsStream(String.format("webui/error/%d.html", finalOut.getT2()));
                    if (is != null) {
                        stringMonoSink.success(new String(is.readAllBytes(), StandardCharsets.UTF_8));
                        is.close();
                    } else {
                        stringMonoSink.success("code:" + finalOut.getT2());
                    }
                } catch (Throwable e) {
                    stringMonoSink.error(e);
                }
            }));
        }
        return toSend;
    }

    public boolean isRunning(){
        return this.disposableServer!=null&&!disposableServer.isDisposed();
    }
    public void stopServer(){

    }
    public Tuple2<Publisher<Void>,Integer> findAndHandler(HttpServerRequest httpServerRequest, HttpServerResponse httpServerResponse) {
        AtomicReference<Publisher<Void>> out = new AtomicReference<>(null);
        var retCode = 404;
        for (int i = 0; i < handlers.size(); i++) {
            var handler = handlers.get(i);
            AtomicReference<Publisher<Void>> tmp = new AtomicReference<>(null);
            try {
                var retCodeT = handler.handle(httpServerRequest, httpServerResponse, tmp);
                if (tmp.get()!=null)
                    retCode=retCodeT;
            }catch (Throwable throwable){
                retCode=500;
                break;
            }
            if (out.get() != null && tmp.get() != null) {
                var lastHandler = handlers.get(i - 1);
                var lName = lastHandler.toString();
                var name = handler.toString();
                if (lastHandler instanceof INamed named) lName = named.getName();
                if (handler instanceof INamed named) name = named.getName();
                log.warn("Handler multiple hit!Last:{},This:{}", lName, name);
            }
            if (tmp.get()!=null) {
                out.set(tmp.get());
            }
        }
        if (out.get()==null) out.set(EMPTY_MONO_VOID);
        return Tuples.of(out.get(), retCode);
    }
    @Override
    public void close() throws IOException {
        this.disposableServer.disposeNow();
        this.sessionManage.close();
    }
}
