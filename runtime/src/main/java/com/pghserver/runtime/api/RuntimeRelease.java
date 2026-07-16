package com.pghserver.runtime.api;

import com.pghserver.api.PghRelease;
import com.pghserver.runtime.util.Logger;

import java.io.IOException;
import java.util.Properties;

public class RuntimeRelease implements PghRelease {
    private static final Logger logger = Logger.system(RuntimeRelease.class);
    private final boolean isDev;
    private final int version;

    public boolean isDev() {
        return isDev;
    }

    public int version() {
        return version;
    }

    protected RuntimeRelease(boolean isDev, int version) {
        this.isDev = isDev;
        this.version = version;
    }

    public static RuntimeRelease instantiate(Class<?> clazz) {
        if (System.getenv().containsKey("DEV_RELEASE"))
            return new RuntimeRelease(true, -1);

        var defaultRelease = new RuntimeRelease(false, -1);
        var properties = new Properties();
        try (var input = clazz.getClassLoader().getResourceAsStream("release.pgh")) {
            if (input == null) {
                logger.error("Broken PghServer release! Does not contain release info, it may not load correctly.");
                return defaultRelease;
            }

            properties.load(input);
            String versionRaw = properties.getProperty("version");
            if (versionRaw == null) throw new IOException("Does not define version number!");
            int version;
            try {
                version = Integer.parseInt(versionRaw);
            } catch (NumberFormatException ex) {
                throw new IOException("Version number is not a number!");
            }

            return new RuntimeRelease(false, version);
        } catch (IOException ex) {
            logger.error("Broken PghServer release! Whilst attempting to load release info:", ex);
            return defaultRelease;
        }
    }
}
