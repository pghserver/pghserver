package com.pghserver.api;

import com.pghserver.api.type.NextCallback;
import com.pghserver.api.type.Request;
import com.pghserver.api.type.Response;
import org.jetbrains.annotations.NotNull;

/**
 * Handles a route when called by a client/runtime/plugin. Can read request data and modify response data to be sent after this handler runs.
 */
public interface RouteHandler {

    /**
     * Called when this route is resolved.
     *
     * @param request  Immutable request data
     * @param response Mutable response data, sent after this handler runs
     * @param next     Attempts the next route in the queue (callback)
     */
    void run(@NotNull Request request, @NotNull Response response, @NotNull NextCallback next);
}
