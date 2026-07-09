package com.pghserver.api.exception;

import java.util.Arrays;

/**
 * Stable base for problems with PghServer, implements a nice object-array-to-string converter for messages.
 */
public class PghException extends Exception {

    /**
     * Stable base for problems with PghServer, implements a nice object-array-to-string converter for messages.
     *
     * @param message List of message parts to be joined together and potentially logged depending on context
     */
    public PghException(Object... message) {
        super(String.join(" ", Arrays.stream(message)
                .map(p -> (p instanceof Exception ex) ? ex.getMessage() : p)
                .map(String::valueOf)
                .toList()));
    }
}
