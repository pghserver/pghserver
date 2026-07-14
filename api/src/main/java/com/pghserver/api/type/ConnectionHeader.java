package com.pghserver.api.type;

import org.jetbrains.annotations.NotNull;

public enum ConnectionHeader {
    CLOSE("close"),
    KEEPALIVE("keep-alive"),
    ;

    public final @NotNull String s;

    ConnectionHeader(@NotNull String s) {
        this.s = s;
    }
}
