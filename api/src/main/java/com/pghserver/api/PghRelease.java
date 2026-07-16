package com.pghserver.api;

/**
 * Release data of PghServer.
 */
public interface PghRelease {

    /**
     * @return Whether this is a development release (not published)
     */
    boolean isDev();

    /**
     * @return PghServer version number (-1 if development release)
     */
    int version();
}
