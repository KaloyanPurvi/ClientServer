import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // welcome.html
        server.createContext("/welcome.html", exchange -> {
            serveStaticFile(exchange, "www/welcome.html", "text/html");});

        server.createContext("/welcome.css", exchange -> {
            serveStaticFile(exchange, "www/welcome.css", "text/css");});

        // index.html (регистрация)
        server.createContext("/index.html", exchange -> {
            serveStaticFile(exchange, "www/index.html", "text/html");
        });



        // login.html
        server.createContext("/login.html", exchange -> {
            serveStaticFile(exchange, "www/login.html", "text/html");
        });

        // index.css
        server.createContext("/index.css", exchange -> {
            serveStaticFile(exchange, "www/index.css", "text/css");
        });

        // login.css
        server.createContext("/login.css", exchange -> {
            serveStaticFile(exchange, "www/login.css", "text/css");
        });

        // регистрация
        server.createContext("/register", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                String msg = "Only POST allowed";
                exchange.sendResponseHeaders(405, msg.length());
                exchange.getResponseBody().write(msg.getBytes());
                exchange.close();
                return;
            }

            String raw = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String name = "", email = "", pass = "";

            for (String p : raw.split("&")) {
                String[] kv = p.split("=");
                if (kv.length < 2) continue;
                String key = kv[0];
                String val = URLDecoder.decode(kv[1], "UTF-8").trim();
                switch (key) {
                    case "name" -> name = val;
                    case "email" -> email = val;
                    case "password" -> pass = val;
                }
            }

            String msg = Database.registerUser(name, email, pass);
            byte[] response = msg.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        // логин
        server.createContext("/login", exchange -> {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                String msg = "Only POST allowed";
                exchange.sendResponseHeaders(405, msg.length());
                exchange.getResponseBody().write(msg.getBytes());
                exchange.close();
                return;
            }

            String raw = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String email = "", password = "";

            for (String part : raw.split("&")) {
                String[] kv = part.split("=");
                if (kv.length < 2) continue;
                String key = kv[0];
                String val = URLDecoder.decode(kv[1], "UTF-8").trim();
                switch (key) {
                    case "email" -> email = val;
                    case "password" -> password = val;
                }
            }

            boolean loginSuccess = Database.checkCredentials(email, password);
            String msg = loginSuccess ? "Вход успешен!" : "Невалиден имейл или парола.";
            System.out.println("Login attempt: " + email + " => " + msg);

            byte[] response = msg.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        // root fallback
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.equals("/index")) {
                serveStaticFile(exchange, "www/index.html", "text/html");
            } else {
                String msg = "404 Not Found";
                exchange.sendResponseHeaders(404, msg.length());
                exchange.getResponseBody().write(msg.getBytes());
                exchange.close();
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Сървърът работи на http://localhost:8080");
    }

    private static void serveStaticFile(HttpExchange exchange, String path, String contentType) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            String notFound = "404 Not Found";
            exchange.sendResponseHeaders(404, notFound.length());
            exchange.getResponseBody().write(notFound.getBytes());
            exchange.close();
            return;
        }

        byte[] content = Files.readAllBytes(file.toPath());
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(200, content.length);
        OutputStream os = exchange.getResponseBody();
        os.write(content);
        os.close();
    }
}
