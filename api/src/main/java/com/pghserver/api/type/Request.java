package com.pghserver.api.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static com.pghserver.api.PghConstants.CRLF_UTF8;
import static com.pghserver.api.PghConstants.HTTP_VERSION;

/**
 * Immutable request data created before any route handlers are resolved. Use inside route handlers for dynamic responses based off user input.
 */
public class Request {
    private final @NotNull RequestMethod method;
    private final @NotNull RequestUrl url;
    private final @NotNull Map<String, String> headers;
    private final @NotNull Map<String, String> lowerHeaders;
    private final byte[] body;

    /**
     * @return Request method
     */
    public RequestMethod method() {
        return method;
    }

    /**
     * @return URL requested by client
     */
    public @NotNull RequestUrl url() {
        return url;
    }

    /**
     * Returns a header's value if it exists, null if not.
     *
     * @param name Header's name
     * @return Header's value
     */
    public @Nullable String header(@NotNull String name) {
        return lowerHeaders.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns a list of existent header names.
     *
     * @return List of headers' names
     */
    public @NotNull Set<String> headerNames() {
        return Set.copyOf(headers.keySet());
    }

    /**
     * Returns true if the header is set, false if not.
     *
     * @param name Header's name
     * @return Whether the header is set
     */
    public boolean hasHeader(@NotNull String name) {
        return lowerHeaders.containsKey(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns true if the header is equal to that exact value, false if not.
     *
     * @param name  Header's name
     * @param value Expected value
     * @return Whether the header is equal to that exact value
     */
    public boolean isHeader(@NotNull String name, @NotNull String value) {
        String actual = header(name);
        return actual != null && actual.equals(value);
    }

    /**
     * @return Request body as an array of bytes
     */
    public byte[] body() {
        return body.clone();
    }

    /**
     * Immutable request data created before any route handlers are resolved. Use inside route handlers for dynamic responses based off user input.
     *
     * @param method  Request method
     * @param url     URL requested by client
     * @param headers Request headers sent by client
     * @param body    Request body as an array of bytes
     */
    public Request(@NotNull RequestMethod method, @NotNull RequestUrl url, @NotNull Map<String, String> headers, byte[] body) {
        this.method = method;
        this.url = url;
        this.headers = new HashMap<>(headers);

        lowerHeaders = new HashMap<>();
        for (Map.Entry<String, String> header : headers.entrySet())
            lowerHeaders.put(header.getKey().toLowerCase(Locale.ROOT), header.getValue());

        this.body = body.clone();
    }

    protected Request(@NotNull RequestMethod method, @NotNull RequestUrl url, @NotNull Map<String, String> headers, @NotNull Map<String, String> lowerHeaders, byte[] body) {
        this.method = method;
        this.url = url;
        this.headers = new HashMap<>(headers);
        this.lowerHeaders = new HashMap<>(lowerHeaders);
        this.body = body.clone();
    }

    /**
     * Prints to an output stream as HTTP spec-compliant data. Manual flushing is required if you plan to transmit this.
     *
     * @param out Output stream
     * @throws IOException Any problems encountered while writing to the output stream
     */
    public void toOutputStream(OutputStream out) throws IOException {
        String requestLine = method().name() + " " + url().toStringWithoutHost() + " " + HTTP_VERSION;
        out.write(requestLine.getBytes(StandardCharsets.UTF_8));
        out.write(CRLF_UTF8);

        for (Map.Entry<String, String> header : headers.entrySet()) {
            out.write((header.getKey() + ": " + header.getValue()).getBytes(StandardCharsets.UTF_8));
            out.write(CRLF_UTF8);
        }

        out.write(CRLF_UTF8);
        out.write(body);
    }

    /**
     * Creates request data from an input stream containing only HTTP spec-compliant data. All problems encountered while reading from the input stream reset and modify response data for extensive compatibility with the runtime.
     *
     * @param in       Input stream
     * @param response Response data to report errors to
     * @return Request data
     */
    public static @Nullable Request fromInputStream(@NotNull InputStream in, Response response) {
        String[] requestLine = new String[]{"", "", ""};
        int requestLineIdx = 0;
        try {
            while (true) {
                int b = in.read();
                if (b == -1) {
                    response.reset();
                    response.status(ResponseStatus.BAD_REQUEST);
                    return null;
                }

                char c = (char) b;
                if (c == '\r') continue;
                if (c == '\n') break;
                if (c == ' ' && !requestLine[requestLineIdx].isBlank()) {
                    requestLineIdx++;
                    if (requestLineIdx >= requestLine.length) {
                        response.reset();
                        response.status(ResponseStatus.BAD_REQUEST);
                        response.contentType("text/plain");
                        response.body("Invalid request line! Too many parts.");
                        return null;
                    }

                    continue;
                }

                requestLine[requestLineIdx] += c;
            }
        } catch (IOException ex) {
            response.reset();
            response.status(ResponseStatus.BAD_REQUEST);
            response.contentType("text/plain");
            response.body("Invalid request line! " + String.join(" ", requestLine));
        }

        if (requestLine[0].isBlank() || requestLine[1].isBlank() || requestLine[2].isBlank()) {
            response.reset();
            response.status(ResponseStatus.BAD_REQUEST);
            response.contentType("text/plain");
            response.body("Request line does not have 3 space-separated parts!");
            return null;
        }

        if (!requestLine[2].equals(HTTP_VERSION)) {
            response.reset();
            response.status(ResponseStatus.HTTP_VERSION_NOT_SUPPORTED);
            return null;
        }

        RequestMethod method;
        try {
            method = RequestMethod.valueOf(requestLine[0]);
        } catch (IllegalArgumentException ex) {
            response.reset();
            response.status(ResponseStatus.NOT_IMPLEMENTED);
            response.contentType("text/plain");
            response.body("Invalid request method " + requestLine[0] + "!");
            return null;
        }

        String path = requestLine[1];
        Map<String, String> headers = new HashMap<>();
        Map<String, String> lowerHeaders = new HashMap<>();
        StringBuilder headerLineBuilder;
        while (true) {
            headerLineBuilder = new StringBuilder();
            try {
                while (true) {
                    int b = in.read();
                    if (b == -1) {
                        response.reset();
                        response.status(ResponseStatus.BAD_REQUEST);
                        return null;
                    }

                    char c = (char) b;
                    if (c == '\r') continue;
                    if (c == '\n') break;
                    headerLineBuilder.append(c);
                }
            } catch (IOException ex) {
                response.reset();
                response.status(ResponseStatus.BAD_REQUEST);
                response.contentType("text/plain");
                response.body("Invalid request header! " + headerLineBuilder);
                return null;
            }

            String headerLine = headerLineBuilder.toString();
            if (headerLine.isBlank()) break;

            String[] header = headerLine.split(":", 2);
            if (header.length != 2) {
                response.reset();
                response.status(ResponseStatus.BAD_REQUEST);
                response.contentType("text/plain");
                response.body("Invalid request header! " + headerLine);
                return null;
            }

            headers.put(header[0].trim(), header[1].trim());
            lowerHeaders.put(header[0].trim().toLowerCase(Locale.ROOT), header[1].trim());
        }

        byte[] rawBody = new byte[0];
        if (lowerHeaders.containsKey("content-length")) {
            long contentLength;
            try {
                contentLength = Long.parseLong(lowerHeaders.get("content-length"));
                if (contentLength < 0L) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                response.reset();
                response.status(ResponseStatus.BAD_REQUEST);
                response.contentType("text/plain");
                response.body("Invalid Content-Length header! " + lowerHeaders.get("content-length"));
                return null;
            }

            if (contentLength > Integer.MAX_VALUE) {
                response.reset();
                response.status(ResponseStatus.CONTENT_TOO_LARGE);
                return null;
            }

            try {
                rawBody = new byte[Math.toIntExact(contentLength)];
                new DataInputStream(in).readFully(rawBody);
            } catch (IOException ex) {
                response.reset();
                response.status(ResponseStatus.BAD_REQUEST);
                response.contentType("text/plain");
                response.body("Invalid request body! " + ex.getMessage());
                return null;
            }
        }

        String host = lowerHeaders.getOrDefault("host", "");
        String cleanPath = path;
        LinkedHashMap<String, String> query = new LinkedHashMap<>();
        if (path.contains("?")) {
            String[] pathQuery = path.split(Pattern.quote("?"), 2);
            String[] queryEntries = pathQuery[1].split(Pattern.quote("&"));
            cleanPath = pathQuery[0];
            for (String raw : queryEntries) {
                String[] queryEntry = raw.split(Pattern.quote("="), 2);
                query.put(queryEntry[0], queryEntry[1]);
            }
        }

        return new Request(method, new RequestUrl(host, cleanPath, query), headers, lowerHeaders, rawBody);
    }
}
