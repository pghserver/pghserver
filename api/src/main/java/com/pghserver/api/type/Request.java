package com.pghserver.api.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

/**
 * Immutable request data created before any route handlers are resolved. Use inside route handlers for dynamic responses based off user input.
 */
public interface Request {

    /**
     * @return Request method
     */
    RequestMethod method();

    /**
     * @return URL requested by client
     */
    @NotNull RequestUrl url();

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
     * @return Request body as an array of bytes
     */
    byte[] body();

    /**
     * Prints to an output stream as HTTP spec-compliant data. Manual flushing is required if you plan to transmit this.
     *
     * @param out Output stream
     * @throws IOException Any problems encountered while writing to the output stream
     */
    void toOutputStream(OutputStream out) throws IOException;
}
