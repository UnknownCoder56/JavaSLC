package io.github.unknowncoder56.javaslc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    private final Map<String, Socket> serverMap = new HashMap<>();

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
    Bot(String prefix, StartListener startListener, ErrorListener errorListener, String token, String userId, ArrayList<MessageListener> messageListeners, ArrayList<CommandListener> commandListeners) {
        super(userId, errorListener);
        this.prefix = prefix;
        this.startListener = startListener;
        this.token = token;
        this.messageListeners = messageListeners;
        this.commandListeners = commandListeners;
    }

    /**
     * The method to run the bot. This method will throw a {@link RuntimeException} if the prefix, token or bot user ID is not set in the {@link BotBuilder} or later in {@link Bot}.
     * Note: This is a blocking method. Use it as the last method call in your thread.
     * @throws RuntimeException If the prefix, token or bot user ID is not set.
     */
    public void run() throws RuntimeException {
        if (prefix.isEmpty()) {
            throw new RuntimeException("Prefix not set.");
        }
        if (token.isEmpty()) {
            throw new RuntimeException("Token not set.");
        }
        if (userId.isEmpty()) {
            throw new RuntimeException("Bot user ID not set.");
        }
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            String responseJsonString = client.execute(new HttpGet("https://slchat.alwaysdata.net/api/user/" + userId + "/"), classicHttpResponse -> new String(classicHttpResponse.getEntity().getContent().readAllBytes()));
            JsonObject responseJson = JsonParser.parseString(responseJsonString).getAsJsonObject();
            JsonArray servers = responseJson.getAsJsonArray("servers");
            for (JsonElement server : servers) {
                String serverId = server.getAsString();
                if (!serverMap.containsKey(serverId)) {
                    makeSocketForServer(serverId);
                }
            }
            if (startListener != null) {
                startListener.onStart();
            }
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            if (errorListener != null) {
                errorListener.onError(e, "run");
            }
            System.out.println("Failed to start bot: " + e.getMessage());
        }
    }

    /**
     * This private method handles new messages (and commands). This method is called by socket event handlers for each server.
     */
    private void handleMessage(JsonObject prompt) {
        JsonObject message = prompt.get("message").getAsJsonObject();
        String serverId = prompt.get("server_id").getAsString();
        try {
            if (!Objects.equals(message.get("owner").getAsString(), getBotUserId())) {
                if (message.get("content").getAsString().startsWith(prefix)) {
                    String[] commandParts = message.get("content").getAsString().split(" ");
                    if (commandParts.length > 1) {
                        String[] arguments = Arrays.copyOfRange(commandParts, 1, commandParts.length - 1);
                        commandListeners.forEach(commandListener -> commandListener.onCommand(new CommandContext(message, serverId, Bot.this, commandParts[0].substring(1), arguments)));
                    } else {
                        commandListeners.forEach(commandListener -> commandListener.onCommand(new CommandContext(message, serverId, Bot.this, commandParts[0].substring(1))));
                    }
                }
            }
            messageListeners.forEach(messageListener -> messageListener.onMessage(new MessageContext(message, serverId, Bot.this)));
        } catch (Exception e) {
            if (errorListener != null) {
                errorListener.onError(e, "checkNewCommand");
            }
            System.out.println("Failed to handle message from server " + serverId + ": " + e.getMessage());
        }
    }

    /**
     * This method sends a message to a server if the bot is in it. If not this method will fail, add it to a server with {@link Bot#join(String)}.
     * @param message The message to send.
     * @param serverId The ID of the server to send the message to.
     * @return A {@link CompletableFuture} that will be completed when the message is sent.
     * @see Bot#join(String)
     */
    public CompletableFuture<Void> send(String message, String serverId) {
        return CompletableFuture.runAsync(() -> {
            if (serverMap.containsKey(serverId)) {
                try {
                    JSONObject payload = new JSONObject();
                    payload.put("content", message);
                    payload.put("server_id", serverId);
                    payload.put("token", token);
                    payload.put("op", getBotUserId());
                    serverMap.get(serverId).emit("message", payload);
                } catch (Exception e) {
                    errorListener.onError(e, "send");
                    System.out.println("Failed to send message to server " + serverId + ": " + e.getMessage());
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
    public CompletableFuture<Void> join(String serverId) {
        return CompletableFuture.runAsync(() -> {
            if (!serverMap.containsKey(serverId)) {
                BasicCookieStore cookieStore = new BasicCookieStore();
                BasicClientCookie tokenCookie = new BasicClientCookie("token", token);
                tokenCookie.setDomain("slchat.alwaysdata.net");
                tokenCookie.setAttribute("domain", "true");
                tokenCookie.setPath("/");
                BasicClientCookie opCookie = new BasicClientCookie("op", getBotUserId());
                opCookie.setDomain("slchat.alwaysdata.net");
                opCookie.setAttribute("domain", "true");
                opCookie.setPath("/");
                cookieStore.addCookie(tokenCookie);
                cookieStore.addCookie(opCookie);
                try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build()) {
                    HttpPost post = new HttpPost("https://slchat.alwaysdata.net/api/new_server");
                    post.setEntity(new UrlEncodedFormEntity(List.of((NameValuePair) new BasicNameValuePair("server_id", serverId))));
                    post.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    ClassicHttpResponse response = client.execute(post, classicHttpResponse -> classicHttpResponse);
                    int code = response.getCode();
                    if ((400 <= code && code < 500) || (500 <= code && code < 600)) {
                        throw new Exception("(" + code + ") " + response.getReasonPhrase());
                    }
                    makeSocketForServer(serverId);
                    System.out.println("Current server IDs: " + serverMap.keySet());
                } catch (Exception e) {
                    if (errorListener != null) {
                        errorListener.onError(e, "join");
                    }
                    System.out.println("Failed to join server " + serverId + ": " + e.getMessage());
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
            BasicCookieStore cookieStore = new BasicCookieStore();
            BasicClientCookie tokenCookie = new BasicClientCookie("token", token);
            tokenCookie.setDomain("slchat.alwaysdata.net");
            tokenCookie.setAttribute("domain", "true");
            tokenCookie.setPath("/");
            BasicClientCookie opCookie = new BasicClientCookie("op", getBotUserId());
            opCookie.setDomain("slchat.alwaysdata.net");
            opCookie.setAttribute("domain", "true");
            opCookie.setPath("/");
            cookieStore.addCookie(tokenCookie);
            cookieStore.addCookie(opCookie);
            try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build()) {
                HttpPost post = new HttpPost("https://slchat.alwaysdata.net/api/change");
                post.setEntity(new UrlEncodedFormEntity(Arrays.asList(
                        (NameValuePair) new BasicNameValuePair("change_key", changeKey.getKeyString()),
                        new BasicNameValuePair("change_value", changeValue),
                        new BasicNameValuePair("token", token),
                        new BasicNameValuePair("op", getBotUserId())
                )));
                post.setHeader("Content-Type", "application/x-www-form-urlencoded");
                ClassicHttpResponse response = client.execute(post, classicHttpResponse -> classicHttpResponse);
                int code = response.getCode();
                if ((400 <= code && code < 500) || (500 <= code && code < 600)) {
                    throw new Exception("(" + code + ") " + response.getReasonPhrase());
                }
                System.out.println("Changed key " + changeKey.name() + " into " + changeValue);
            } catch (Exception e) {
                if (errorListener != null) {
                    errorListener.onError(e, "change");
                }
                System.out.println("Failed to change key " + changeKey.name() + " into " + changeValue + ": " + e.getMessage());
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
    public String getBotUserId() {
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

    /**
     * Private utility method to make socket and socket event handler for server, then connect it and add it to socket map.
     * @param serverId The ID of the server to make the socket for.
     */
    private void makeSocketForServer(String serverId) {
        try {
            Socket socket = IO.socket("https://slchat.alwaysdata.net?server_id=" + serverId);
            socket.on("prompt", objects -> handleMessage(JsonParser.parseString(objects[0].toString()).getAsJsonObject()));
            socket.connect();
            serverMap.put(serverId, socket);
        } catch (URISyntaxException e) {
            if (errorListener != null) {
                errorListener.onError(e, "makeSocketForServer");
            }
            System.out.println("Failed to make socket for server " + serverId);
        }
    }
}