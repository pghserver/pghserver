package com.pghserver.api.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * Mutable response data initially created before any route handlers are resolved. Use inside route handlers to modify the response and what the client receives.
 */
public interface Response {

    /**
     * <p>Resets this response data to what it was initially created as.</p>
     * <p><strong>Status</strong> - <code>ResponseStatus.OK (200)</code></p>
     * <p><strong>Headers</strong> - <code>Empty</code></p>
     * <p><strong>Body</strong> - <code>Empty</code></p>
     */
    void reset();

    /**
     * @return Full response status data (code, string/phrase)
     */
    @NotNull ResponseStatus status();

    /**
     * @return Status code
     */
    int statusCode();

    /**
     * @return Status string/phrase
     */
    @NotNull String statusString();

    /**
     * Sets the full response status data (code, string/phrase)
     *
     * @param status Response status
     */
    void status(@NotNull ResponseStatus status);

    /**
     * Sets the full response status data (code, string/phrase)
     *
     * @param code   Status code
     * @param string Status string/phrase
     */
    void status(int code, @NotNull String string);

    /**
     * Sets the response status code, automatically setting the string/phrase along with it based on a list of documented HTTP status codes.
     *
     * @param code Status code
     */
    void statusCode(int code);

    /**
     * Returns a header's value if it exists, null if not.
     *
     * @param name Header's name
     * @return Header's value
     */
    @Nullable String header(@NotNull String name);

    /**
     * Returns a list of existent header names.
     *
     * @return List of headers' names
     */
    @NotNull Set<String> headerNames();

    /**
     * Returns true if the header is set, false if not.
     *
     * @param name Header's name
     * @return Whether the header is set
     */
    boolean hasHeader(@NotNull String name);

    /**
     * Returns true if the header is equal to that exact value, false if not.
     *
     * @param name  Header's name
     * @param value Expected value
     * @return Whether the header is equal to that exact value
     */
    boolean isHeader(@NotNull String name, @NotNull String value);

    /**
     * Sets header to an exact value.
     *
     * @param name  Header's name
     * @param value Header's value
     */
    void header(@NotNull String name, @Nullable String value);

    /**
     * Shorthand for setting the response's connection header, determines whether the client should continue sending requests. This is rarely used, we recommend relying on the Content-Length header unless absolutely required.
     *
     * @param value Header's value
     */
    void connection(String value);

    /**
     * Shorthand for setting the response's content type header.
     *
     * @param type MIME type
     */
    void contentType(@NotNull String type);

    /**
     * Redirects to the specified URL/path. Shorthand for setting the response's status code and location header.
     *
     * @param url       URL/path
     * @param permanent Whether redirect is considered permanent
     */
    void redirect(@NotNull String url, boolean permanent);

    /**
     * Redirects to the specified URL/path. Shorthand for setting the response's status code and location header.
     *
     * @param url URL/path
     */
    void redirect(@NotNull String url);

    /**
     * @return Response body as an array of bytes
     */
    byte[] body();

    /**
     * Sets the response body to an array of bytes.
     *
     * @param body New response body, array of bytes
     */
    void body(byte[] body);

    /**
     * Sets the response body to a specific encoding of text.
     *
     * @param text    Text
     * @param charset Charset to encode with
     */
    void body(String text, Charset charset);

    /**
     * Sets the response body to UTF-8 text. Specify a charset after the text to switch out the encoding.
     *
     * @param text Text
     */
    void body(String text);

    /**
     * Prints to an output stream as HTTP spec-compliant data. Manual flushing is required if you plan to transmit this.
     *
     * @param out Output stream
     * @throws IOException Any problems encountered while writing to the output stream
     */
    void toOutputStream(OutputStream out) throws IOException;
}
