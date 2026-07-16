package com.pghserver.runtime.api.type;

import com.pghserver.api.type.Response;
import com.pghserver.api.type.ResponseStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.pghserver.api.PghConstants.CRLF_UTF8;
import static com.pghserver.api.PghConstants.HTTP_VERSION;

public class RuntimeResponse implements Response {
    private final @NotNull Map<String, String> headers = new HashMap<>();
    private final @NotNull Map<String, String> lowerHeaders = new HashMap<>();
    private @NotNull ResponseStatus status;
    private byte[] body;

    public RuntimeResponse() {
        reset();
    }

    public void reset() {
        status = ResponseStatus.OK;
        headers.clear();
        lowerHeaders.clear();
        body(new byte[0]);
    }

    public @NotNull ResponseStatus status() {
        return status;
    }

    public int statusCode() {
        return status.code();
    }

    public @NotNull String statusString() {
        return status.string();
    }

    public void status(@NotNull ResponseStatus status) {
        this.status = status;
    }

    public void status(int code, @NotNull String string) {
        status(new ResponseStatus(code, string));
    }

    public void statusCode(int code) {
        status(ResponseStatus.ofCode(code));
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

    public void header(@NotNull String name, @Nullable String value) {
        if (value == null) {
            for (var header : headers.entrySet()) {
                if (!header.getKey().equalsIgnoreCase(name)) continue;
                headers.remove(header.getKey());
            }

            lowerHeaders.remove(name.toLowerCase(Locale.ROOT));
            return;
        }

        headers.put(name, value);
        lowerHeaders.put(name.toLowerCase(Locale.ROOT), value);
    }

    public void connection(String value) {
        header("Connection", value);
    }

    public void contentType(@NotNull String type) {
        header("Content-Type", type);
    }

    public void redirect(@NotNull String url, boolean permanent) {
        status(permanent ? ResponseStatus.PERMANENT_REDIRECT : ResponseStatus.TEMPORARY_REDIRECT);
        header("Location", url);
    }

    public void redirect(@NotNull String url) {
        redirect(url, false);
    }

    public byte[] body() {
        return body.clone();
    }

    public void body(byte[] body) {
        this.body = body.clone();
        header("Content-Length", String.valueOf(body.length));
    }

    public void body(String text, Charset charset) {
        body(text.getBytes(charset));
    }

    public void body(String text) {
        body(text, StandardCharsets.UTF_8);
    }

    public void toOutputStream(OutputStream out) throws IOException {
        var statusLine = HTTP_VERSION + " " + statusCode() + " " + statusString();
        out.write(statusLine.getBytes(StandardCharsets.UTF_8));
        out.write(CRLF_UTF8);

        for (var header : headers.entrySet()) {
            out.write((header.getKey() + ": " + header.getValue()).getBytes(StandardCharsets.UTF_8));
            out.write(CRLF_UTF8);
        }

        out.write(CRLF_UTF8);
        out.write(body);
    }
}
