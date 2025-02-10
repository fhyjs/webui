package org.eu.hanana.reimu.webui.core;

import io.netty.handler.codec.http.cookie.Cookie;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class Util {
    public static Cookie getCookieValue(HttpServerRequest request, String cookieName) {
        // 获取所有的 cookies
        return Optional.of(request.cookies())
                .map(cookies -> cookies.get(cookieName))
                .map(cookie -> cookie.toArray(new io.netty.handler.codec.http.cookie.Cookie[0])[0]) // cookies.get(cookieName) 返回一个列表，取第一个值
                .orElse(null);
    }
    public static String getMimeType(String uri) {
        // 根据路径解析文件扩展名
        String extension = Optional.ofNullable(uri)
                .filter(u -> u.contains("."))
                .map(u -> u.substring(uri.lastIndexOf('.') + 1))
                .orElse("");

        // 根据扩展名返回 MIME 类型
        try {
            String type = Files.probeContentType(Paths.get("dummy." + extension));
            if (type.startsWith("text/")) type+="; charset=UTF-8";
            return type;
        } catch (IOException e) {
            log.warn("Failed to resolve MIME type for {}: {}", uri, e.getMessage());
            return "application/octet-stream"; // 默认 MIME 类型
        }
    }
    public static HttpServerResponse sendRedirect(HttpServerResponse httpServerResponse,String path){
        return httpServerResponse.status(307).addHeader("Location",path);
    }
    public static HttpServerResponse autoContentType(HttpServerResponse httpServerResponse){
        return httpServerResponse.addHeader("content-type",getMimeType(httpServerResponse.uri()));
    }
    public static String getDescriptor(Constructor<?> constructor) {
        StringBuilder descriptor = new StringBuilder("<init>(");

        for (Parameter parameter : constructor.getParameters()) {
            descriptor.append(getJVMDescriptor(parameter.getType()));
        }

        descriptor.append(")V"); // 构造方法的返回值固定是 V（void）
        return descriptor.toString();
    }

    private static String getJVMDescriptor(Class<?> type) {
        if (type.isArray()) {
            return type.getName().replace('.', '/'); // 处理数组类型，如 "[Ljava/lang/String;"
        } else if (type.isPrimitive()) {
            return getPrimitiveDescriptor(type);
        } else {
            return "L" + type.getName().replace('.', '/') + ";";
        }
    }

    private static String getPrimitiveDescriptor(Class<?> type) {
        if (type == void.class) return "V";
        if (type == int.class) return "I";
        if (type == long.class) return "J";
        if (type == double.class) return "D";
        if (type == float.class) return "F";
        if (type == boolean.class) return "Z";
        if (type == char.class) return "C";
        if (type == byte.class) return "B";
        if (type == short.class) return "S";
        throw new IllegalArgumentException("Unknown primitive type: " + type);
    }
    public static Constructor<?> findConstructor(Class<?> clazz, String signature) throws NoSuchMethodException {
        if (!signature.startsWith("<init>(")) {
            throw new IllegalArgumentException("Not a constructor signature: " + signature);
        }

        Class<?>[] paramTypes = parseParameterTypes(signature);
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (matchesParameterTypes(constructor.getParameterTypes(), paramTypes)) {
                return constructor;
            }
        }

        throw new NoSuchMethodException("No matching constructor found: " + signature);
    }

    public static Method findMethod(Class<?> clazz, String methodName, String signature) throws NoSuchMethodException {
        if (!signature.startsWith("(") || !signature.contains(")")) {
            throw new IllegalArgumentException("Invalid method signature: " + signature);
        }

        Class<?>[] paramTypes = parseParameterTypes(signature);
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) && matchesParameterTypes(method.getParameterTypes(), paramTypes)) {
                return method;
            }
        }

        throw new NoSuchMethodException("No matching method found: " + methodName + signature);
    }

    private static Class<?>[] parseParameterTypes(String signature) {
        List<Class<?>> paramTypes = new ArrayList<>();
        String params = signature.substring(signature.indexOf('(') + 1, signature.indexOf(')'));

        while (!params.isEmpty()) {
            if (params.startsWith("L")) { // 对象类型
                int end = params.indexOf(';') + 1;
                String className = params.substring(1, end - 1).replace('/', '.');
                try {
                    paramTypes.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Class not found: " + className);
                }
                params = params.substring(end);
            } else if (params.startsWith("[")) { // 数组类型
                int dim = 0;
                while (params.charAt(dim) == '[') dim++;
                char baseType = params.charAt(dim);
                String baseTypeStr = params.substring(0, dim + 1);
                paramTypes.add(getArrayClass(baseTypeStr));
                params = params.substring(dim + 1);
            } else { // 基本类型
                char primitive = params.charAt(0);
                paramTypes.add(getPrimitiveClass(primitive));
                params = params.substring(1);
            }
        }

        return paramTypes.toArray(new Class<?>[0]);
    }

    private static Class<?> getPrimitiveClass(char descriptor) {
        return switch (descriptor) {
            case 'I' -> int.class;
            case 'J' -> long.class;
            case 'D' -> double.class;
            case 'F' -> float.class;
            case 'Z' -> boolean.class;
            case 'C' -> char.class;
            case 'B' -> byte.class;
            case 'S' -> short.class;
            case 'V' -> void.class;
            default -> throw new IllegalArgumentException("Unknown primitive type: " + descriptor);
        };
    }

    private static Class<?> getArrayClass(String descriptor) {
        try {
            return Class.forName(descriptor.replace('/', '.'));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Array class not found: " + descriptor);
        }
    }

    private static boolean matchesParameterTypes(Class<?>[] actual, Class<?>[] expected) {
        if (actual.length != expected.length) return false;
        for (int i = 0; i < actual.length; i++) {
            if (!actual[i].equals(expected[i])) return false;
        }
        return true;
    }
    @SneakyThrows
    public static Mono<byte[]> getAllPostData(HttpServerRequest request){
        return request.receive()
                .aggregate()
                .asByteArray();
    }
    public static Mono<String> getAllPostDataString(HttpServerRequest request){
        return Mono.create(stringMonoSink -> {
            getAllPostData(request).doOnSuccess(bytes -> {
                stringMonoSink.success(new String(bytes,StandardCharsets.UTF_8));
            }).doOnError(stringMonoSink::error).subscribe();
        });
    }
}
