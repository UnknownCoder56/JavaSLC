package io.github.unknowncoder56.javaslc;

import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

public class MessageContext {

    private final String content;
    private final User owner;
    private final long serverId;
    private final Bot bot;
    private final String command;
    private final String[] arguments;

    public MessageContext(JsonObject message, long serverId, Bot bot, String command) {
        content = message.get("content").getAsString();
        owner = new User(message.get("owner").getAsLong(), bot.getErrorListener());
        this.serverId = serverId;
        this.bot = bot;
        this.command = command;
        this.arguments = new String[]{};
    }

    public MessageContext(JsonObject message, long serverId, Bot bot, String command, String[] arguments) {
        content = message.get("content").getAsString();
        owner = new User(message.get("owner").getAsLong(), bot.getErrorListener());
        this.serverId = serverId;
        this.bot = bot;
        this.command = command;
        this.arguments = arguments;
    }

    public CompletableFuture<Void> send(String message) {
        return bot.send(message, serverId);
    }

    public String getContent() {
        return content;
    }

    public User getOwner() {
        return owner;
    }

    public long getServerId() {
        return serverId;
    }

    public Bot getBot() {
        return bot;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArguments() {
        return arguments;
    }
}