package com.pghserver.runtime.api.type;

import com.pghserver.api.type.*;
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

public class RuntimeRequest implements Request {
    private final @NotNull RequestMethod method;
    private final @NotNull RequestUrl url;
    private final @NotNull Map<String, String> headers;
    private final @NotNull Map<String, String> lowerHeaders;
    private final byte[] body;

    public RequestMethod method() {
        return method;
    }

    public @NotNull RequestUrl url() {
        return url;
    }

    public @Nullable String header(@NotNull String name) {
        return lowerHeaders.get(name.toLowerCase(Locale.ROOT));
    }

    public @NotNull Set<String> headerNames() {
        return Set.copyOf(headers.keySet());
    }

    public boolean hasHeader(@NotNull String name) {
        return lowerHeaders.containsKey(name.toLowerCase(Locale.ROOT));
    }

    public boolean isHeader(@NotNull String name, @NotNull String value) {
        var actual = header(name);
        return actual != null && actual.equals(value);
    }

    public byte[] body() {
        return body.clone();
    }

    protected RuntimeRequest(@NotNull RequestMethod method, @NotNull RequestUrl url, @NotNull Map<String, String> headers, @NotNull Map<String, String> lowerHeaders, byte[] body) {
        this.method = method;
        this.url = url;
        this.headers = new HashMap<>(headers);
        this.lowerHeaders = new HashMap<>(lowerHeaders);
        this.body = body.clone();
    }

    public void toOutputStream(OutputStream out) throws IOException {
        var requestLine = method().name() + " " + url().toStringWithoutHost() + " " + HTTP_VERSION;
        out.write(requestLine.getBytes(StandardCharsets.UTF_8));
        out.write(CRLF_UTF8);

        for (var header : headers.entrySet()) {
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
        var requestLine = new String[]{"", "", ""};
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

        var path = requestLine[1];
        var headers = new HashMap<String, String>();
        var lowerHeaders = new HashMap<String, String>();
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

            var headerLine = headerLineBuilder.toString();
            if (headerLine.isBlank()) break;

            var header = headerLine.split(":", 2);
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

        var host = lowerHeaders.getOrDefault("host", "");
        var cleanPath = path;
        var query = new LinkedHashMap<String, String>();
        if (path.contains("?")) {
            String[] pathQuery = path.split(Pattern.quote("?"), 2);
            String[] queryEntries = pathQuery[1].split(Pattern.quote("&"));
            cleanPath = pathQuery[0];
            for (String raw : queryEntries) {
                String[] queryEntry = raw.split(Pattern.quote("="), 2);
                query.put(queryEntry[0], queryEntry[1]);
            }
        }

        return new RuntimeRequest(method, new RuntimeUrl(host, cleanPath, query), headers, lowerHeaders, rawBody);
    }
}

