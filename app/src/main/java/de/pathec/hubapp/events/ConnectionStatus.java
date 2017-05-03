package de.pathec.hubapp.events;

public class ConnectionStatus {
    // connected > 0
    public static final Integer CONNECTED = 1;
    public static final Integer CONNECTING = 2;
    // not connected < 0
    public static final Integer NOT_CONNECTED = -1;
    public static final Integer UNAVAILABLE = -2;
    public static final Integer UNAUTHORIZED = -3;
    public static final Integer CONFIGURATION_ERROR = -4;
}
