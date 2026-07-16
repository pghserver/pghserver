package com.pghserver.runtime;

import com.pghserver.api.PghAPI;
import com.pghserver.api.PghPlugin;
import com.pghserver.runtime.util.Logger;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;

public class PluginManager {
    private static final Logger logger = Logger.system(PluginManager.class);
    private static final List<PghPlugin> plugins = new ArrayList<>();
    private static final List<URLClassLoader> classLoaders = new ArrayList<>();

    public static void load(Path directory) {
        if (!Files.exists(directory)) try {
            Files.createDirectories(directory);
        } catch (IOException ex) {
            logger.error("Could not create plugins directory!", ex);
            return;
        }

        try (var files = Files.list(directory)) {
            files.filter(path -> path.toString().endsWith(".jar")).forEach(PluginManager::loadJar);
        } catch (Exception ex) {
            logger.error("Failed to scan plugins directory!", ex);
        }
    }

    private static void loadJar(Path file) {
        var jarName = file.getFileName().toString();
        try {
            var jar = new JarFile(file.toFile());
            var manifestEntry = jar.getJarEntry("manifest.pgh");
            if (manifestEntry == null) {
                logger.warn("Skipping JAR without manifest.pgh!", file);
                jar.close();
                return;
            }

            var properties = new Properties();
            try (var in = jar.getInputStream(manifestEntry)) {
                properties.load(in);
            }

            var name = properties.getProperty("name");
            var version = properties.getProperty("version");
            var mainClassName = properties.getProperty("main-class");
            var pghVersion = properties.getProperty("pgh");

            if (name == null || name.isBlank()) {
                logger.error("Plugin missing required name field!", jarName);
                jar.close();
                return;
            }

            if (version == null || version.isBlank()) {
                logger.error("Plugin missing required version field!", jarName);
                jar.close();
                return;
            }

            if (mainClassName == null || mainClassName.isBlank()) {
                logger.error("Plugin missing required main-class field!", jarName);
                jar.close();
                return;
            }

            if (pghVersion != null && !pghVersion.isBlank() && !Main.release.isDev()) {
                int plugin;
                try {
                    plugin = Integer.parseInt(pghVersion);
                } catch (NumberFormatException ex) {
                    logger.error("Plugin has non-integer pgh field!", jarName);
                    jar.close();
                    return;
                }

                int pgh = Main.release.version();
                if (plugin != pgh) {
                    logger.error("Plugin not for your PghServer version! You're running v" + pgh + ", plugin is for v" + plugin + ".", jarName);
                    jar.close();
                    return;
                }
            }

            var classEntry = jar.getJarEntry(mainClassName.replace('.', '/') + ".class");
            if (classEntry == null) {
                logger.error("Plugin main class does not exist!", jarName, mainClassName);
                jar.close();
                return;
            }

            jar.close();
            var loader = new URLClassLoader(new URL[]{file.toUri().toURL()}, PluginManager.class.getClassLoader());
            Class<?> clazz;
            try {
                clazz = Class.forName(mainClassName, true, loader);
            } catch (ClassNotFoundException ex) {
                logger.error("Could not load plugin main class!", jarName, mainClassName, ex);
                loader.close();
                return;
            }

            if (!PghPlugin.class.isAssignableFrom(clazz)) {
                logger.error("Plugin main class does not implement PghPlugin!", jarName, mainClassName);
                loader.close();
                return;
            }

            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            var instance = constructor.newInstance();
            plugins.add((PghPlugin) instance);
            classLoaders.add(loader);
            logger.info("Loaded plugin!", name, "v" + version);
        } catch (Exception ex) {
            logger.error("Failed to load plugin!", jarName, ex);
        }
    }

    public static void onEnable(PghAPI server) {
        for (var plugin : plugins)
            try {
                plugin.onEnable(server, new Logger(plugin, logger.logStream, logger.warningStream, logger.errorStream, logger.fatalStream));
            } catch (Exception ex) {
                logger.error("Plugin crashed during enable!", plugin, ex);
            }
    }

    public static void onDisable(PghAPI server) {
        for (var plugin : plugins)
            try {
                plugin.onDisable(server, new Logger(plugin, logger.logStream, logger.warningStream, logger.errorStream, logger.fatalStream));
            } catch (Exception ex) {
                logger.error("Plugin crashed during disable!", plugin, ex);
            }

        for (var loader : classLoaders)
            try {
                loader.close();
            } catch (Exception ignored) {
            }
    }
}
