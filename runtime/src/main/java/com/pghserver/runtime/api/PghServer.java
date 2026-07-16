package com.pghserver.runtime.api;

import com.pghserver.api.PghAPI;
import com.pghserver.api.PghRelease;
import com.pghserver.api.RouteHandler;
import com.pghserver.runtime.Router;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class PghServer implements PghAPI {
    private final @NotNull Path directory;
    private final @NotNull Router routes;
    private final @NotNull PghRelease release;

    public PghServer(@NotNull Path directory, @NotNull PghRelease release) {
        this.directory = directory;
        routes = new Router();
        this.release = release;
    }

    @Override
    public void route(@NotNull Pattern path, @NotNull RouteHandler handler) {
        routes.put(path, handler);
    }

    @Override
    public void route(@NotNull @Language("RegExp") String path, @NotNull RouteHandler handler) {
        route(Pattern.compile(path), handler);
    }

    @Override
    public @NotNull List<RouteHandler> resolve(@NotNull String path) {
        return routes.resolve(path);
    }

    @Override
    public @Nullable RouteHandler resolveFirst(@NotNull String path) {
        return resolve(path).getFirst();
    }

    @Override
    public @NotNull Path directory() {
        return directory;
    }

    @Override
    public @NotNull PghRelease release() {
        return release;
    }
}
