package com.pghserver.runtime.util;

import java.io.PrintStream;
import java.util.Arrays;

public class PghLogger {

    public static void info(Object... message) {
        base(System.out, "info", message);
    }

    public static void warn(Object... message) {
        base(System.err, "warning", message);
    }

    public static void error(Object... message) {
        base(System.err, "error", message);
    }

    public static void fatal(Object... message) {
        base(System.err, "fatal", message);
    }

    private static void base(PrintStream out, String prefix, Object[] message) {
        out.println("[" + prefix.trim().toUpperCase() + "] " + String.join(" ", Arrays.stream(message)
                .map(p -> (p instanceof Exception ex) ? ex.getMessage() : p)
                .map(String::valueOf)
                .toList()));
    }
}
