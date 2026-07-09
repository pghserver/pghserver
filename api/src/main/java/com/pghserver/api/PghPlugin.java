package com.pghserver.api;

public interface PghPlugin {

    /**
     * Called when this plugin is ready to be enabled (commonly during runtime startup)
     *
     * @param server Server instance for registering/resolving routes, etc
     */
    void onEnable(PghAPI server);

    /**
     * Called when this plugin is ready to be disabled (commonly during runtime shutdown)
     *
     * @param server Server instance for further cleanup
     */
    void onDisable(PghAPI server);
}
