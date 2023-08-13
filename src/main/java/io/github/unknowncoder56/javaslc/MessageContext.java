package io.github.unknowncoder56.javaslc;

import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

/**
 * A class to create a context object which contains details about the message like message content, owner (author), server ID, {@link Bot} instance, command name and command arguments.
 */
public class MessageContext {

    private final String content;
    private final User owner;
    private final long serverId;
    private final Bot bot;
    private final String command;
    private final String[] arguments;

    /**
     * Constructor to create an instance of the class for a command without arguments.
     * @param message The message JSON retrieved from the SLChat API.
     * @param serverId The ID of the server where the message was sent.
     * @param bot The {@link Bot} instance receiving the message event.
     * @param command The command name of the command issued by the user.
     */
    public MessageContext(JsonObject message, long serverId, Bot bot, String command) {
        content = message.get("content").getAsString();
        owner = new User(message.get("owner").getAsLong(), bot.getErrorListener());
        this.serverId = serverId;
        this.bot = bot;
        this.command = command;
        this.arguments = new String[]{};
    }

    /**
     * Constructor to create an instance of the class for a command with arguments.
     * @param message The message JSON retrieved from the SLChat API.
     * @param serverId The ID of the server where the message was sent.
     * @param bot The {@link Bot} instance receiving the message event.
     * @param command The command name of the command issued by the user.
     * @param arguments An array of the command arguments.
     */
    public MessageContext(JsonObject message, long serverId, Bot bot, String command, String[] arguments) {
        content = message.get("content").getAsString();
        owner = new User(message.get("owner").getAsLong(), bot.getErrorListener());
        this.serverId = serverId;
        this.bot = bot;
        this.command = command;
        this.arguments = arguments;
    }

    /**
     * This method sends a message to a server directly from where the command originated. This method will throw a {@link RuntimeException} if the bot is not in the server. To fix it add it to a server with {@link Bot#join(long)}.
     * @param message The message to send.
     * @return A {@link CompletableFuture} that will be completed when the message is sent.
     * @see Bot#send(String, long)
     */
    public CompletableFuture<Void> send(String message) {
        return bot.send(message, serverId);
    }

    /**
     * Gets the message content.
     * @return The message content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the message owner.
     * @return The {@link User} instance of the owner.
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Gets the server ID of the server where the message was sent.
     * @return The server ID of the server where the message was sent.
     */
    public long getServerId() {
        return serverId;
    }

    /**
     * Gets the {@link Bot} which received the command.
     * @return The {@link Bot} which received the command.
     */
    public Bot getBot() {
        return bot;
    }

    /**
     * Gets the command name of the command received.
     * @return The command name of the command received.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Gets the array of the arguments supplied with the command.
     * @return The array of the arguments supplied with the command.
     */
    public String[] getArguments() {
        return arguments;
    }
}