package com.pghserver.runtime;

import com.pghserver.api.RouteHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

public class Router extends LinkedHashMap<Pattern, RouteHandler> {

    public @NotNull List<RouteHandler> resolve(@NotNull String path) {
        var handlers = new ArrayList<RouteHandler>();
        for (var route : entrySet())
            if (route.getKey().matcher(path).matches()) handlers.add(route.getValue());

        return handlers;
    }
}
