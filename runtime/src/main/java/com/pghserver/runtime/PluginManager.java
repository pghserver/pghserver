package com.pghserver.runtime;

import com.pghserver.api.PghAPI;
import com.pghserver.api.PghPlugin;
import com.pghserver.runtime.util.PghLogger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginManager {
    private static final List<PghPlugin> plugins = new ArrayList<>();
    private static final List<URLClassLoader> classLoaders = new ArrayList<>();

    public static void load(Path directory) {
        if (!Files.exists(directory)) try {
            Files.createDirectories(directory);
        } catch (IOException ex) {
            PghLogger.error("Could not create plugins directory!", ex);
            return;
        }

        try (var files = Files.list(directory)) {
            files.filter(path -> path.toString().endsWith(".jar")).forEach(PluginManager::loadJar);
        } catch (Exception ex) {
            PghLogger.error("Failed to scan plugins directory!", ex);
        }
    }

    private static void loadJar(Path file) {
        String jarName = file.getFileName().toString();
        try {
            JarFile jar = new JarFile(file.toFile());
            JarEntry manifestEntry = jar.getJarEntry("manifest.pgh");
            if (manifestEntry == null) {
                PghLogger.warn("Skipping JAR without manifest.pgh!", file);
                jar.close();
                return;
            }

            Properties properties = new Properties();
            try (InputStream in = jar.getInputStream(manifestEntry)) {
                properties.load(in);
            }

            String name = properties.getProperty("name");
            String version = properties.getProperty("version");
            String mainClassName = properties.getProperty("main-class");

            if (name == null || name.isBlank()) {
                PghLogger.error("Plugin missing required name field!", jarName);
                jar.close();
                return;
            }

            if (version == null || version.isBlank()) {
                PghLogger.error("Plugin missing required version field!", jarName);
                jar.close();
                return;
            }

            if (mainClassName == null || mainClassName.isBlank()) {
                PghLogger.error("Plugin missing required main-class field!", jarName);
                jar.close();
                return;
            }

            JarEntry classEntry = jar.getJarEntry(mainClassName.replace('.', '/') + ".class");
            if (classEntry == null) {
                PghLogger.error("Plugin main class does not exist!", jarName, mainClassName);
                jar.close();
                return;
            }

            jar.close();
            URLClassLoader loader = new URLClassLoader(new URL[]{file.toUri().toURL()}, PluginManager.class.getClassLoader());
            Class<?> clazz;
            try {
                clazz = Class.forName(mainClassName, true, loader);
            } catch (ClassNotFoundException ex) {
                PghLogger.error("Could not load plugin main class!", jarName, mainClassName, ex);
                loader.close();
                return;
            }

            if (!PghPlugin.class.isAssignableFrom(clazz)) {
                PghLogger.error("Plugin main class does not implement PghPlugin!", jarName, mainClassName);
                loader.close();
                return;
            }

            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();
            plugins.add((PghPlugin) instance);
            classLoaders.add(loader);
            PghLogger.info("Loaded plugin!", name, "v" + version);
        } catch (Exception ex) {
            PghLogger.error("Failed to load plugin!", jarName, ex);
        }
    }

    public static void onEnable(PghAPI server) {
        for (PghPlugin plugin : plugins)
            try {
                plugin.onEnable(server);
            } catch (Exception ex) {
                PghLogger.error("Plugin crashed during enable!", plugin, ex);
            }
    }

    public static void onDisable(PghAPI server) {
        for (PghPlugin plugin : plugins)
            try {
                plugin.onDisable(server);
            } catch (Exception ex) {
                PghLogger.error("Plugin crashed during disable!", plugin, ex);
            }

        for (URLClassLoader loader : classLoaders)
            try {
                loader.close();
            } catch (Exception ignored) {
            }
    }
}
