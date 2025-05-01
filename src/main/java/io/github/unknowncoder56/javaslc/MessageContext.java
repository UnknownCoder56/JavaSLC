package io.github.unknowncoder56.javaslc;

import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

/**
 * A class to create a context object which contains details about the message like message text, owner (author), server ID and {@link Bot} instance.
 */
public class MessageContext {

    private final String text;
    private final User owner;
    private final String serverId;
    private final Bot bot;

    /**
     * Constructor to create an instance of the class for a message.
     * @param message The message JSON retrieved from the SLChat API.
     * @param serverId The ID of the server where the message was sent.
     * @param bot The {@link Bot} instance receiving the message event.
     */
    public MessageContext(JsonObject message, String serverId, Bot bot) {
        text = message.get("text").getAsString();
        owner = new User(message.get("owner").getAsJsonObject().get("id").getAsString(), bot.getErrorListener());
        this.serverId = serverId;
        this.bot = bot;
    }

    /**
     * This method sends a message text to a server directly from where the message originated. This method will throw a {@link RuntimeException} if the bot is not in the server. To fix it, add it to the server with {@link Bot#join(String)}.
     * @param text The message text to send.
     * @return A {@link CompletableFuture} that will be completed when the message is sent.
     * @see Bot#send(String, String)
     */
    public CompletableFuture<Void> send(String text) {
        return bot.send(text, serverId);
    }

    /**
     * Gets the message text.
     * @return The message text.
     */
    public String getText() {
        return text;
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
    public String getServerId() {
        return serverId;
    }

    /**
     * Gets the {@link Bot} which received the message.
     * @return The {@link Bot} which received the message.
     */
    public Bot getBot() {
        return bot;
    }
}