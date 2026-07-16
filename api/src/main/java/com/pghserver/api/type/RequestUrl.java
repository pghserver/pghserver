package com.pghserver.api.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Structured request URL data to assist in handling requests.
 */
public interface RequestUrl {

    /**
     * @return URL host (e.g. example.com)
     */
    @NotNull String host();

    /**
     * @return URL path (e.g. /about)
     */
    @NotNull String path();

    /**
     * Converts the query parameters to a string.
     *
     * @return Query parameters string
     */
    @NotNull String queryString();

    /**
     * Retrieves a query parameter by name.
     *
     * @param key Parameter's name
     * @return Parameter's value
     */
    @Nullable String query(@NotNull String key);

    /**
     * Returns whether a parameter by that name is set.
     *
     * @param key Parameter's name
     * @return Whether a parameter by that name is set
     */
    boolean hasQuery(@NotNull String key);

    /**
     * Returns whether a parameter by that name is exactly equal to that value.
     *
     * @param key   Parameter's name
     * @param value Expected value
     * @return Whether a parameter by that name is exactly equal to that value
     */
    boolean isQuery(@NotNull String key, @NotNull String value);

    /**
     * @return URL as a string including the path and query parameters
     */
    String toStringWithoutHost();

    /**
     * @return Full URL as a string including the host, path, and query parameters
     */
    @Override
    String toString();
}
