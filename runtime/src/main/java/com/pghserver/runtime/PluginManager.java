package com.pghserver.runtime;

import com.pghserver.api.PghAPI;
import com.pghserver.api.PghPlugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PluginManager {
    private static final List<PghPlugin> plugins = new ArrayList<>();

    public static void load(Path directory) {
        // TODO: Loop through all JARs
        // TODO: Use PghLogger.warn(Object... message); and skip any JARs not containing a `manifest.pgh` at the root
        // TODO: Create a Properties instance from that manifest and verify all the required fields, including `main-class` (required string, must directly resolve to a class inside the JAR), `name` (required string), and `version` (required string), PghLogger.error(Object... message); for any invalid/missing fields
        // TODO: Verify the main class implements PghPlugin, PghLogger.error(Object... message); skip it if not
        // TODO: Create an instance of the main class, PghLogger.error(Object... message); if that somehow fails
        // TODO: Add the instance to plugins List above
    }

    public static void onEnable(PghAPI server) {
        for (PghPlugin plugin : plugins) plugin.onEnable(server);
    }

    public static void onDisable(PghAPI server) {
        for (PghPlugin plugin : plugins) plugin.onDisable(server);
    }
}
