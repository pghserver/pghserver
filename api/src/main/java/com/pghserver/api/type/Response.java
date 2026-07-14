package com.pghserver.api.type;

import com.pghserver.api.exception.MalformedResponseException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.pghserver.api.PghConstants.CRLF_UTF8;
import static com.pghserver.api.PghConstants.HTTP_VERSION;

/**
 * Mutable response data initially created before any route handlers are resolved. Use inside route handlers to modify the response and what the client receives.
 */
public class Response {
    private final @NotNull Map<String, String> headers = new HashMap<>();
    private final @NotNull Map<String, String> lowerHeaders = new HashMap<>();
    private @NotNull ResponseStatus status;
    private byte[] body;

    /**
     * Mutable response data initially created before any route handlers are resolved. Use inside route handlers to modify the response and what the client receives.
     */
    public Response() {
        reset();
    }

    /**
     * <p>Resets this response data to what it was initially created as.</p>
     * <p><strong>Status</strong> - <code>ResponseStatus.OK (200)</code></p>
     * <p><strong>Headers</strong> - <code>Empty</code></p>
     * <p><strong>Body</strong> - <code>Empty</code></p>
     */
    public void reset() {
        status = ResponseStatus.OK;
        headers.clear();
        lowerHeaders.clear();
        body(new byte[0]);
    }

    /**
     * @return Full response status data (code, string/phrase)
     */
    public @NotNull ResponseStatus status() {
        return status;
    }

    /**
     * @return Status code
     */
    public int statusCode() {
        return status.code();
    }

    /**
     * @return Status string/phrase
     */
    public @NotNull String statusString() {
        return status.string();
    }

    /**
     * Sets the full response status data (code, string/phrase)
     *
     * @param status Response status
     */
    public void status(@NotNull ResponseStatus status) {
        this.status = status;
    }

    /**
     * Sets the full response status data (code, string/phrase)
     *
     * @param code   Status code
     * @param string Status string/phrase
     */
    public void status(int code, @NotNull String string) {
        status(new ResponseStatus(code, string));
    }

    /**
     * Sets the response status code, automatically setting the string/phrase along with it based on a list of documented HTTP status codes.
     *
     * @param code Status code
     */
    public void statusCode(int code) {
        status(ResponseStatus.ofCode(code));
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
        var actual = header(name);
        return actual != null && actual.equals(value);
    }

    /**
     * Sets header to an exact value.
     *
     * @param name  Header's name
     * @param value Header's value
     */
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

    /**
     * Shorthand for setting the response's connection header, determines whether the client should continue sending requests. This is rarely used, we recommend relying on the Content-Length header unless absolutely required.
     *
     * @param value Header's value
     */
    public void connection(String value) {
        header("Connection", value);
    }

    /**
     * Shorthand for setting the response's connection header, determines whether the client should continue sending requests. This is rarely used, we recommend relying on the Content-Length header unless absolutely required.
     *
     * @param value Header's value (enum)
     */
    public void connection(ConnectionHeader value) {
        connection(value.s);
    }

    /**
     * Shorthand for setting the response's content type header.
     *
     * @param type MIME type
     */
    public void contentType(@NotNull String type) {
        header("Content-Type", type);
    }

    /**
     * Redirects to the specified URL/path. Shorthand for setting the response's status code and location header.
     *
     * @param url       URL/path
     * @param permanent Whether redirect is considered permanent
     */
    public void redirect(@NotNull String url, boolean permanent) {
        status(permanent ? ResponseStatus.PERMANENT_REDIRECT : ResponseStatus.TEMPORARY_REDIRECT);
        header("Location", url);
    }

    /**
     * Redirects to the specified URL/path. Shorthand for setting the response's status code and location header.
     *
     * @param url URL/path
     */
    public void redirect(@NotNull String url) {
        redirect(url, false);
    }

    /**
     * @return Response body as an array of bytes
     */
    public byte[] body() {
        return body.clone();
    }

    /**
     * Sets the response body to an array of bytes.
     *
     * @param body New response body, array of bytes
     */
    public void body(byte[] body) {
        this.body = body.clone();
        header("Content-Length", String.valueOf(body.length));
    }

    /**
     * Sets the response body to a specific encoding of text.
     *
     * @param text    Text
     * @param charset Charset to encode with
     */
    public void body(String text, Charset charset) {
        body(text.getBytes(charset));
    }

    /**
     * Sets the response body to UTF-8 text. Specify a charset after the text to switch out the encoding.
     *
     * @param text Text
     */
    public void body(String text) {
        body(text, StandardCharsets.UTF_8);
    }

    /**
     * Prints to an output stream as HTTP spec-compliant data. Manual flushing is required if you plan to transmit this.
     *
     * @param out Output stream
     * @throws IOException Any problems encountered while writing to the output stream
     */
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

    /**
     * <p><strong>Warning!</strong> Not implemented yet. Do not utilize in production code.</p>
     * <p>Creates response data from an input stream containing only HTTP spec-compliant data.</p>
     *
     * @param in Input stream
     * @return Response data
     * @throws MalformedResponseException Any problems encountered while reading/parsing the input stream
     */
    public static @ApiStatus.Experimental @Nullable Response fromInputStream(InputStream in) throws MalformedResponseException {
        return null;
    }
}
