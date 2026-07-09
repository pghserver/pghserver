package com.pghserver.runtime;

import com.pghserver.api.type.Request;
import com.pghserver.api.type.Response;
import com.pghserver.api.type.ResponseStatus;
import com.pghserver.runtime.api.PghServer;
import com.pghserver.runtime.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class Main {
    private static final Logger logger = new Logger(Main.class, System.out, System.err, System.err, System.err);

    static void main(String[] args) {
        int port = 80;
        if (args.length >= 1 && Pattern.compile("[0-9]+").matcher(args[0]).matches())
            port = Integer.parseInt(args[0]);

        Path directory = Path.of(".");
        if (args.length == 2)
            directory = Path.of(args[1]);

        var server = new PghServer(directory);
        PluginManager.load(server.directory().resolve("plugins"));
        PluginManager.onEnable(server);
        try (var serverSocket = new ServerSocket(port)) {
            logger.info("Started PghServer on port " + serverSocket.getLocalPort() + "!");
            logger.info("Press Enter to stop it.");
            AtomicBoolean isRunning = new AtomicBoolean(true);
            serverSocket.setSoTimeout(250);

            Thread.ofPlatform().start(() -> {
                try {
                    System.in.read();
                    isRunning.set(false);
                } catch (IOException ignored) {
                }
            });

            while (isRunning.get()) {
                try (var socket = serverSocket.accept(); InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
                    Response response = new Response();
                    response.header("Date", DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
                            .withZone(ZoneId.of("GMT"))
                            .format(ZonedDateTime.now()));

                    response.header("Server", "Production-Grade HTTP Server (PghServer)");
                    response.header("Connection", "keep-alive");

                    Request request = Request.fromInputStream(in, response);
                    if (request == null) {
                        response.toOutputStream(out);
                        continue;
                    }

                    var handler = server.resolve(request.url().path);
                    if (handler == null) response.status(ResponseStatus.NOT_FOUND);
                    else handler.run(request, response);
                    response.toOutputStream(out);
                    out.flush();
                } catch (SocketTimeoutException ignored) {
                } catch (IOException ex) {
                    logger.error("Client connection failed!", ex);
                }
            }
        } catch (IOException ex) {
            logger.fatal("Could not start PghServer!", ex);
        }

        PluginManager.onDisable(server);
    }
}
