package io.github.unknowncoder56.javaslc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The main class of the library containing all important methods, like to run the bot. This class cannot be instantiated directly, use {@link BotBuilder} instead.
 * @see BotBuilder
 * @see User
 */
public class Bot extends User {

    private String prefix;
    private StartListener startListener;
    private final String token;
    private final ArrayList<MessageListener> messageListeners;
    private final ArrayList<CommandListener> commandListeners;
    private final ArrayList<Long> serverIds = new ArrayList<>();
    private final Map<Long, String> latestMessageTimesPerServer = new HashMap<>();

    /**
     * The enum containing all possible property change keys.
     * @see Bot#change(ChangeKey, String)
     */
    public enum ChangeKey {

        /**
         * The key to change the profile image of the bot.
         */
        PROFILE_IMAGE("profile_img"),

        /**
         * The key to change the nickname of the bot.
         */
        NICKNAME("nickname");

        private final String key;

        ChangeKey(String key) {
            this.key = key;
        }

        /**
         * Gets the key string of the enum value.
         * @return The key string of the enum value.
         */
        public String getKeyString() {
            return key;
        }
    }

    /**
     * The constructor of the {@link Bot} class. This constructor has default-level access and is only used by the {@link BotBuilder} class.
     */
    Bot(String prefix, StartListener startListener, ErrorListener errorListener, String token, long userId, ArrayList<MessageListener> messageListeners, ArrayList<CommandListener> commandListeners) {
        super(userId, errorListener);
        this.prefix = prefix;
        this.startListener = startListener;
        this.token = token;
        this.messageListeners = messageListeners;
        this.commandListeners = commandListeners;
    }

    /**
     * The method to run the bot. This method will throw a {@link RuntimeException} if the prefix, token or bot user ID is not set in the {@link BotBuilder} or later in {@link Bot}.
     * @throws RuntimeException If the prefix, token or bot user ID is not set.
     */
    public void run() throws RuntimeException {
        if (prefix.isEmpty()) {
            throw new RuntimeException("Prefix not set.");
        }
        if (token.isEmpty()) {
            throw new RuntimeException("Token not set.");
        }
        if (userId == 0) {
            throw new RuntimeException("Bot user ID not set.");
        }
        try {
            String responseJsonString = Request.get("https://chat.slsearch.eu.org/api/user/" + userId + "/").execute().returnContent().asString();
            JsonObject responseJson = JsonParser.parseString(responseJsonString).getAsJsonObject();
            JsonArray servers = responseJson.getAsJsonArray("servers");
            servers.forEach(server -> serverIds.add(Long.parseLong(server.getAsString())));
            if (startListener != null) {
                startListener.onStart();
            }
            Runnable runnable = this::checkForNewMessage;
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(runnable, 0, 2500, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            errorListener.onError(e, "run");
            System.out.println("Failed to start bot: " + e.getMessage());
        }
    }

    /**
     * This private method checks for new messages (or commands). This method is called every 2.5 seconds by a {@link ScheduledExecutorService} scheduled in the {@link Bot#run()} method.
     */
    private void checkForNewMessage() {
        if (!commandListeners.isEmpty()) {
            for (long serverId : serverIds) {
                try {
                    String responseJsonString = Request.get("https://chat.slsearch.eu.org/api/server/" + serverId + "/").execute().returnContent().asString();
                    JsonArray messages = JsonParser.parseString(responseJsonString).getAsJsonObject().get("messages").getAsJsonArray();
                    JsonObject latestMessage = messages.get(messages.size() - 1).getAsJsonObject();
                    if (latestMessageTimesPerServer.containsKey(serverId)) {
                        if (!Objects.equals(latestMessageTimesPerServer.get(serverId), latestMessage.get("date").getAsString())) {
                            latestMessageTimesPerServer.replace(serverId, latestMessage.get("date").getAsString());
                        } else {
                            continue;
                        }
                    } else {
                        latestMessageTimesPerServer.put(serverId, latestMessage.get("date").getAsString());
                    }
                    if (!Objects.equals(latestMessage.get("owner").getAsString(), String.valueOf(userId))) {
                        if (latestMessage.get("content").getAsString().startsWith(prefix)) {
                            String[] commandParts = latestMessage.get("content").getAsString().split(" ");
                            if (commandParts.length > 1) {
                                String[] arguments = Arrays.copyOfRange(commandParts, 1, commandParts.length - 1);
                                commandListeners.forEach(commandListener -> commandListener.onCommand(new CommandContext(latestMessage, serverId, Bot.this, commandParts[0].substring(1), arguments)));
                            } else {
                                commandListeners.forEach(commandListener -> commandListener.onCommand(new CommandContext(latestMessage, serverId, Bot.this, commandParts[0].substring(1))));
                            }
                        } else {
                            messageListeners.forEach(messageListener -> messageListener.onMessage(new MessageContext(latestMessage, serverId, Bot.this)));
                        }
                    }
                } catch (Exception e) {
                    if (errorListener != null) {
                        errorListener.onError(e, "checkNewCommand");
                    }
                    System.out.println("Failed to fetch server data of " + serverId);
                }
            }
        }
    }

    /**
     * This method sends a message to a server if the bot is in it. If not this method will fail, add it to a server with {@link Bot#join(long)}.
     * @param message The message to send.
     * @param serverId The ID of the server to send the message to.
     * @return A {@link CompletableFuture} that will be completed when the message is sent.
     * @see Bot#join(long)
     */
    public CompletableFuture<Void> send(String message, long serverId) {
        return CompletableFuture.runAsync(() -> {
            if (serverIds.contains(serverId)) {
                try {
                    Request.post("https://chat.slsearch.eu.org/api/send").bodyForm(() -> Arrays.asList(
                            (NameValuePair) new BasicNameValuePair("message", message),
                            new BasicNameValuePair("server_id", String.valueOf(serverId)),
                            new BasicNameValuePair("token", token)
                    ).iterator()).addHeader("Content-Type", "application/x-www-form-urlencoded").execute();
                } catch (Exception e) {
                    errorListener.onError(e, "send");
                    System.out.println("Failed to send message to server " + serverId);
                }
            } else {
                if (errorListener != null) {
                    errorListener.onError(new Exception("Bot is not in server " + serverId), "send");
                }
                System.out.println("Bot is not in server " + serverId);
            }
        });
    }

    /**
     * This method joins a server if not already joined by the bot.
     * @param serverId The ID of the server to join.
     * @return A {@link CompletableFuture} that will be completed when the bot has joined the server.
     */
    public CompletableFuture<Void> join(long serverId) {
        return CompletableFuture.runAsync(() -> {
            if (!serverIds.contains(serverId)) {
                try {
                    Request.post("https://chat.slsearch.eu.org/api/new_server").bodyForm(() -> Arrays.asList(
                            (NameValuePair) new BasicNameValuePair("server_id", String.valueOf(serverId)),
                            new BasicNameValuePair("token", token)
                    ).iterator()).addHeader("Content-Type", "application/x-www-form-urlencoded").execute();
                    serverIds.add(serverId);
                    System.out.println("Current server IDs: " + serverIds);
                } catch (Exception e) {
                    if (errorListener != null) {
                        errorListener.onError(e, "join");
                    }
                    System.out.println("Failed to join server " + serverId);
                }
            } else {
                if (errorListener != null) {
                    errorListener.onError(new Exception("Bot is already in server " + serverId), "join");
                }
                System.out.println("Bot is already in server " + serverId);
            }
        });
    }

    /**
     * This method changes a bot property defined in the {@link ChangeKey} enum.
     * @param changeKey The key of the property to change.
     * @param changeValue The value to change the key to.
     * @return A {@link CompletableFuture} that will be completed when the value is changed.
     * @see ChangeKey
     */
    public CompletableFuture<Void> change(ChangeKey changeKey, String changeValue) {
        return CompletableFuture.runAsync(() -> {
            try {
                Request.post("https://chat.slsearch.eu.org/api/change").bodyForm(() -> Arrays.asList(
                        (NameValuePair) new BasicNameValuePair("change_key", changeKey.getKeyString()),
                        new BasicNameValuePair("change_value", changeValue),
                        new BasicNameValuePair("token", token)
                ).iterator()).addHeader("Content-Type", "application/x-www-form-urlencoded").execute();
                System.out.println("Changed key " + changeKey.name() + " into " + changeValue);
            } catch (Exception e) {
                if (errorListener != null) {
                    errorListener.onError(e, "change");
                }
                System.out.println("Failed to change key " + changeKey.name() + " into " + changeValue);
            }
        });
    }

    /**
     * Gets the prefix of the bot.
     * @return The prefix of the bot.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix of the bot.
     * @param prefix The prefix to set.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the {@link StartListener} of the bot.
     * @return The {@link StartListener} of the bot.
     */
    public StartListener getStartListener() {
        return startListener;
    }

    /**
     * Sets the {@link StartListener} of the bot.
     * @param startListener The {@link StartListener} to set.
     */
    public void setStartListener(StartListener startListener) {
        this.startListener = startListener;
    }

    /**
     * Gets the user ID of the bot.
     * @return The user ID of the bot.
     */
    public long getBotUserId() {
        return userId;
    }

    /**
     * Gets the list of {@link MessageListener}s of the bot.
     * @return An {@link ArrayList} containing the {@link MessageListener}s of the bot.
     */
    public ArrayList<MessageListener> getMessageListeners() {
        return messageListeners;
    }

    /**
     * Adds a {@link MessageListener} to the bot.
     * @param messageListener The {@link MessageListener} to add.
     */
    public void addMessageListener(MessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    /**
     * Gets the list of {@link CommandListener}s of the bot.
     * @return An {@link ArrayList} containing the {@link CommandListener}s of the bot.
     */
    public ArrayList<CommandListener> getCommandListeners() {
        return commandListeners;
    }

    /**
     * Adds a {@link CommandListener} to the bot.
     * @param commandListener The {@link CommandListener} to add.
     */
    public void addCommandListener(CommandListener commandListener) {
        commandListeners.add(commandListener);
    }
}