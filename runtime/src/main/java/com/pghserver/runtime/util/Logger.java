package com.pghserver.runtime.util;

import com.pghserver.api.PghLogger;

import java.io.PrintStream;
import java.util.Arrays;

public class Logger implements PghLogger {
    public final String name;
    public final PrintStream logStream;
    public final PrintStream warningStream;
    public final PrintStream errorStream;
    public final PrintStream fatalStream;

    public Logger(Object source, PrintStream logStream, PrintStream warningStream, PrintStream errorStream, PrintStream fatalStream) {
        name = source.getClass().getSimpleName();
        this.logStream = logStream;
        this.warningStream = warningStream;
        this.errorStream = errorStream;
        this.fatalStream = fatalStream;
    }

    public Logger(Class<?> clazz, PrintStream logStream, PrintStream warningStream, PrintStream errorStream, PrintStream fatalStream) {
        name = clazz.getSimpleName();
        this.logStream = logStream;
        this.warningStream = warningStream;
        this.errorStream = errorStream;
        this.fatalStream = fatalStream;
    }

    public void info(Object... message) {
        base(logStream, "info", message);
    }

    public void warn(Object... message) {
        base(warningStream, "warning", message);
    }

    public void error(Object... message) {
        base(errorStream, "error", message);
    }

    public void fatal(Object... message) {
        base(fatalStream, "fatal", message);
    }

    private void base(PrintStream out, String prefix, Object[] message) {
        out.println(name + " [" + prefix.trim().toUpperCase() + "] " + String.join(" ", Arrays.stream(message)
                .map(p -> (p instanceof Exception ex) ? ex.getMessage() : p)
                .map(String::valueOf)
                .toList()));
    }

    public static Logger system(Object source) {
        return new Logger(source, System.out, System.err, System.err, System.err);
    }

    public static Logger system(Class<?> clazz) {
        return new Logger(clazz, System.out, System.err, System.err, System.err);
    }
}
