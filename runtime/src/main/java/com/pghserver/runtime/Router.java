package com.pghserver.runtime;

import com.pghserver.api.RouteHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class Router extends LinkedHashMap<Pattern, RouteHandler> {

    public @Nullable RouteHandler resolve(@NotNull String path) {
        RouteHandler handler = null;
        for (var route : entrySet())
            if (route.getKey().matcher(path).matches()) handler = route.getValue();

        return handler;
    }
}
