package com.pghserver.api;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public interface PghAPI {

    /**
     * Registers route based on a regex path. Later registrations take priority over earlier ones during path resolution.
     *
     * @param path    Regex path
     * @param handler Handler which is called when this route is resolved
     */
    void route(@NotNull Pattern path, @NotNull RouteHandler handler);

    /**
     * <p>Registers route based on a regex path. Later registrations take priority over earlier ones during path resolution.</p>
     * <strong>This override allows you to input the regex path as a string, whilst retaining proper syntax highlighting in JetBrains IDEs.</strong>
     *
     * @param path    Regex path
     * @param handler Handler which is called when this route is resolved
     */
    void route(@NotNull @Language("RegExp") String path, @NotNull RouteHandler handler);

    /**
     * Resolves client-style paths to a few route handlers, if it is registered anywhere in the runtime/plugins.
     *
     * @param path Client-style path
     * @return Route handler list
     */
    @NotNull List<RouteHandler> resolve(@NotNull String path);

    /**
     * Resolves client-style paths to a single route handler, if it is registered anywhere in the runtime/plugins.
     *
     * @param path Client-style path
     * @return Route handler
     */
    @Nullable RouteHandler resolveFirst(@NotNull String path);

    /**
     * @return Directory used by the server for various resources. Can be used by plugins, for example, static file directories and media storage
     */
    @NotNull Path directory();

    /**
     * @return PghServer release data
     */
    @NotNull PghRelease release();
}
