package com.pghserver.api.exception;

/**
 * Thrown when failure to parse a response occurs.
 */
public class MalformedResponseException extends PghException {

    /**
     * Thrown when failure to parse a response occurs.
     *
     * @param message List of message parts to be joined together and potentially logged depending on context
     */
    public MalformedResponseException(Object... message) {
        super(message);
    }
}
