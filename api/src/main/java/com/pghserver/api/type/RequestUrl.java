package com.pghserver.api.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Structured request URL data to assist in handling requests.
 */
public class RequestUrl {
    public static final @NotNull RequestUrl NULL = new RequestUrl(null, "/", new LinkedHashMap<>());
    public final @NotNull String host;
    public final @NotNull String path;
    private final @NotNull LinkedHashMap<String, String> query;

    /**
     * Converts the query parameters to a string.
     *
     * @return String formatted as such: <code>?a=b&c=d&e=f</code>
     */
    public @NotNull String queryString() {
        char start = query.isEmpty() ? ' ' : '&';
        return start + String.join("&" + query.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .toList()).trim();
    }

    /**
     * Retrieves a query parameter by name.
     *
     * @param key Parameter's name
     * @return Parameter's value
     */
    public @Nullable String query(@NotNull String key) {
        return query.get(key);
    }

    /**
     * Returns whether a parameter by that name is set.
     *
     * @param key Parameter's name
     * @return Whether a parameter by that name is set
     */
    public boolean hasQuery(@NotNull String key) {
        return query.containsKey(key);
    }

    /**
     * Returns whether a parameter by that name is exactly equal to that value.
     *
     * @param key   Parameter's name
     * @param value Expected value
     * @return Whether a parameter by that name is exactly equal to that value
     */
    public boolean isQuery(@NotNull String key, @NotNull String value) {
        return hasQuery(key) && query.get(key).equals(value);
    }

    /**
     * Structured request URL data to assist in handling requests.
     *
     * @param host  Optional client-supplied host string
     * @param path  URL path
     * @param query Query parameters
     */
    public RequestUrl(@Nullable String host, @NotNull String path, @NotNull LinkedHashMap<String, String> query) {
        this.host = Objects.requireNonNullElse(host, "");
        this.path = path;
        this.query = new LinkedHashMap<>(query);
    }

    /**
     * @return String formatted as such: <code>/about/us?a=b&c=d&e=f</code>
     */
    public String toStringWithoutHost() {
        return path + queryString();
    }

    /**
     * @return String formatted as such: <code>localhost:3000/about/us?a=b&c=d&e=f</code>
     */
    @Override
    public String toString() {
        return host + path + queryString();
    }
}
