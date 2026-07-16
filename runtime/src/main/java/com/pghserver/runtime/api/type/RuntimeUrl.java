package com.pghserver.runtime.api.type;

import com.pghserver.api.type.RequestUrl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Objects;

public class RuntimeUrl implements RequestUrl {
    private final @NotNull String host;
    private final @NotNull String path;
    private final @NotNull LinkedHashMap<String, String> query;

    @Override
    public @NotNull String host() {
        return host;
    }

    @Override
    public @NotNull String path() {
        return path;
    }

    public @NotNull String queryString() {
        char start = query.isEmpty() ? ' ' : '&';
        return start + String.join("&" + query.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .toList()).trim();
    }

    public @Nullable String query(@NotNull String key) {
        return query.get(key);
    }

    public boolean hasQuery(@NotNull String key) {
        return query.containsKey(key);
    }

    public boolean isQuery(@NotNull String key, @NotNull String value) {
        return hasQuery(key) && query.get(key).equals(value);
    }

    public RuntimeUrl(@Nullable String host, @NotNull String path, @NotNull LinkedHashMap<String, String> query) {
        this.host = Objects.requireNonNullElse(host, "");
        this.path = path;
        this.query = new LinkedHashMap<>(query);
    }

    public String toStringWithoutHost() {
        return path + queryString();
    }

    @Override
    public String toString() {
        return host + path + queryString();
    }
}
