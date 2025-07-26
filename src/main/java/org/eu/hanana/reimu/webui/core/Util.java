package org.eu.hanana.reimu.webui.core;

import com.google.gson.JsonObject;
import io.netty.handler.codec.http.cookie.Cookie;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Util {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Logger log = LogManager.getLogger(Util.class);

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
    private static void applyTextWarp(Graphics2D g, String text, int width, int height) {
        AffineTransform origTransform = g.getTransform();

        int x = 20;
        int y = height / 2 + 10;

        for (char c : text.toCharArray()) {
            // 生成波浪效果
            double shearX = (RANDOM.nextDouble() - 0.5) * 1.1; // 水平扭曲
            double shearY = (RANDOM.nextDouble() - 0.5) * 1.1; // 垂直扭曲

            AffineTransform transform = new AffineTransform();
            transform.translate(x, y);
            transform.shear(shearX, shearY);

            g.setTransform(transform);
            g.drawString(String.valueOf(c), 0, 0);
            x += 25 + RANDOM.nextInt(5);
        }

        g.setTransform(origTransform);
    }
    public static BufferedImage generateCaptchaImage(String captchaText) throws IOException {
        int width = 150, height = 50;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 随机字体和颜色
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.setColor(new Color(RANDOM.nextInt(200), RANDOM.nextInt(200), RANDOM.nextInt(200)));

        // 生成扭曲文本
        applyTextWarp(g, captchaText, width, height);

        // 添加干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(RANDOM.nextInt(255), RANDOM.nextInt(255), RANDOM.nextInt(255)));
            g.drawLine(RANDOM.nextInt(width), RANDOM.nextInt(height), RANDOM.nextInt(width), RANDOM.nextInt(height));
        }

        g.dispose();
        return image;
    }
    public static Map<String, List<String>> parseQueryParams(String uri) {
        Map<String, List<String>> queryPairs = new LinkedHashMap<>();
        try {
            URI u = new URI(uri);
            String query = u.getRawQuery();
            if (query == null) {
                return queryPairs;
            }
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;
                String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : null;
                queryPairs.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryPairs;
    }
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
        return httpServerResponse.addHeader("content-type",getMimeType(httpServerResponse.fullPath()));
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

    public static String parseColumn(JsonObject column, String dbName) {
        String name = column.get("name").getAsString();
        String type = column.get("type").getAsString();
        boolean primaryKey = column.has("primaryKey") && column.get("primaryKey").getAsBoolean();
        boolean autoIncrement = column.has("autoIncrement") && column.get("autoIncrement").getAsBoolean();
        boolean unique = column.has("unique") && column.get("unique").getAsBoolean();
        boolean notNull = column.has("notNull") && column.get("notNull").getAsBoolean();

        // 兼容不同数据库的 AUTO_INCREMENT
        if (autoIncrement) {
            if (dbName.contains("mysql") || dbName.contains("postgresql")) {
                type += " AUTO_INCREMENT";
            } else if (dbName.contains("sqlite")) {
                type = "INTEGER PRIMARY KEY AUTOINCREMENT"; // SQLite 主键必须是 INTEGER
            } else if (dbName.contains("sql server")) {
                type = "INT IDENTITY(1,1) PRIMARY KEY";
            } else if (dbName.contains("oracle")) {
                type = "NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY";
            }
        }

        StringBuilder columnDef = new StringBuilder(name + " " + type);
        if (notNull) columnDef.append(" NOT NULL");
        if (unique) columnDef.append(" UNIQUE");
        if (primaryKey && !autoIncrement) columnDef.append(" PRIMARY KEY");

        return columnDef.toString();
    }
}
