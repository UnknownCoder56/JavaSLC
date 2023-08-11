package com.uniqueapps.javaslc;

import java.util.ArrayList;

public class BotBuilder {

    private String prefix = "";
    private StartListener startListener = null;
    private ErrorListener errorListener = null;
    private String token = "";
    private long botUserId = 0;
    private final ArrayList<CommandListener> commandListeners = new ArrayList<>();

    private BotBuilder() {

    }

    public static BotBuilder newInstance() {
        return new BotBuilder();
    }

    public BotBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public BotBuilder setStartListener(StartListener startListener) {
        this.startListener = startListener;
        return this;
    }

    public BotBuilder setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
        return this;
    }

    public BotBuilder setToken(String token) {
        this.token = token;
        return this;
    }

    public BotBuilder setBotUserId(long botUserId) {
        this.botUserId = botUserId;
        return this;
    }

    public BotBuilder addCommandListener(CommandListener commandListener) {
        commandListeners.add(commandListener);
        return this;
    }

    public Bot build() {
        return new Bot(prefix, startListener, errorListener, token, botUserId, commandListeners);
    }
}
