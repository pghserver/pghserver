package com.pghserver.runtime;

import com.pghserver.api.type.ConnectionHeader;
import com.pghserver.api.type.Request;
import com.pghserver.api.type.Response;
import com.pghserver.api.type.ResponseStatus;
import com.pghserver.runtime.api.PghServer;
import com.pghserver.runtime.util.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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

    private static void handle(PghServer server, Socket socket) {
        try (socket; var in = new DataInputStream(socket.getInputStream()); var out = new DataOutputStream(socket.getOutputStream())) {
            boolean keepAlive = true;
            while (keepAlive) {
                var response = new Response();
                response.header("Date", DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
                        .withZone(ZoneId.of("GMT"))
                        .format(ZonedDateTime.now()));

                response.header("Server", "Production-Grade HTTP Server (PghServer)");
                response.header("Connection", "keep-alive");

                var request = Request.fromInputStream(in, response);
                if (request == null) break;
                response.connection("close".equalsIgnoreCase(request.header("Connection")) ? ConnectionHeader.CLOSE : ConnectionHeader.KEEPALIVE);

                var handler = server.resolve(request.url().path);
                if (handler == null) response.status(ResponseStatus.NOT_FOUND);
                else handler.run(request, response);

                keepAlive = !"close".equalsIgnoreCase(response.header("Connection"));
                response.toOutputStream(out);
                out.flush();
            }
        } catch (SocketTimeoutException ignored) {
        } catch (IOException ex) {
            logger.error("Client connection failed!", ex);
        }
    }

    static void main(String[] args) {
        int port = 80;
        if (args.length >= 1 && Pattern.compile("[0-9]+").matcher(args[0]).matches())
            port = Integer.parseInt(args[0]);

        var directory = Path.of(".");
        if (args.length == 2)
            directory = Path.of(args[1]);

        var server = new PghServer(directory);
        PluginManager.load(server.directory().resolve("plugins"));
        PluginManager.onEnable(server);
        try (var serverSocket = new ServerSocket(port)) {
            logger.info("Started PghServer on port " + serverSocket.getLocalPort() + "!");
            logger.info("Press Enter to stop it.");
            var isRunning = new AtomicBoolean(true);
            serverSocket.setSoTimeout(250);
            Thread.ofPlatform().start(() -> {
                try {
                    System.in.read();
                    isRunning.set(false);
                } catch (IOException ignored) {
                }
            });

            while (isRunning.get()) try {
                var socket = serverSocket.accept();
                Thread.ofVirtual().start(() -> handle(server, socket));
            } catch (SocketTimeoutException ignored) {
            } catch (IOException ex) {
                logger.error("Failed to accept client!", ex);
            }
        } catch (IOException ex) {
            logger.fatal("Could not start PghServer!", ex);
        }

        PluginManager.onDisable(server);
    }
}
