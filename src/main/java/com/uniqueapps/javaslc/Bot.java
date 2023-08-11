package com.uniqueapps.javaslc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Bot extends User {

    private String prefix;
    private StartListener startListener;
    private final String token;
    private final ArrayList<CommandListener> commandListeners;
    private final ArrayList<Long> serverIds = new ArrayList<>();

    public enum ChangeKey {
        PROFILE_IMAGE, NICKNAME
    }

    Bot(String prefix, StartListener startListener, ErrorListener errorListener, String token, long userId, ArrayList<CommandListener> commandListeners) {
        super(userId, errorListener);
        this.prefix = prefix;
        this.startListener = startListener;
        this.token = token;
        this.commandListeners = commandListeners;
    }

    public void run() {
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
            Runnable runnable = this::checkForNewCommand;
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(runnable, 0, 2500, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            errorListener.onError(e, "run");
            System.out.println("Failed to start bot: " + e.getMessage());
        }
    }

    private void checkForNewCommand() {
        if (!commandListeners.isEmpty()) {
            for (long serverId : serverIds) {
                try {
                    String responseJsonString = Request.get("https://chat.slsearch.eu.org/api/server/" + serverId + "/").execute().returnContent().asString();
                    JsonArray messages = JsonParser.parseString(responseJsonString).getAsJsonObject().get("messages").getAsJsonArray();
                    JsonObject latestMessage = messages.get(messages.size() - 1).getAsJsonObject();
                    if (!Objects.equals(latestMessage.get("owner").getAsString(), String.valueOf(userId))) {
                        if (latestMessage.get("content").getAsString().startsWith(prefix)) {
                            String[] commandParts = latestMessage.get("content").getAsString().split(" ");
                            if (commandParts.length > 1) {
                                String[] arguments = Arrays.copyOfRange(commandParts, 1, commandParts.length - 1);
                                commandListeners.forEach(commandListener -> commandListener.onCommand(new MessageContext(latestMessage, serverId, Bot.this, commandParts[0].substring(1), arguments)));
                            } else {
                                commandListeners.forEach(commandListener -> commandListener.onCommand(new MessageContext(latestMessage, serverId, Bot.this, commandParts[0].substring(1))));
                            }
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

    public CompletableFuture<Void> change(ChangeKey changeKey, String changeValue) {
        return CompletableFuture.runAsync(() -> {
            String changeKeyString;
            switch (changeKey) {
                case PROFILE_IMAGE:
                    changeKeyString = "profile_image";
                    break;
                case NICKNAME:
                    changeKeyString = "nickname";
                    break;
                default:
                    throw new RuntimeException("Invalid change key.");
            }
            try {
                Request.post("https://chat.slsearch.eu.org/api/change").bodyForm(() -> Arrays.asList(
                        (NameValuePair) new BasicNameValuePair("change_key", changeKeyString),
                        new BasicNameValuePair("change_value", changeValue),
                        new BasicNameValuePair("token", token)
                ).iterator()).addHeader("Content-Type", "application/x-www-form-urlencoded").execute();
                System.out.println("Changed key " + changeKey + " into " + changeValue);
            } catch (Exception e) {
                if (errorListener != null) {
                    errorListener.onError(e, "change");
                }
                System.out.println("Failed to change key " + changeKey + " into " + changeValue);
            }
        });
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public StartListener getStartListener() {
        return startListener;
    }

    public void setStartListener(StartListener startListener) {
        this.startListener = startListener;
    }

    public long getBotUserId() {
        return userId;
    }

    public ArrayList<CommandListener> getCommandListeners() {
        return commandListeners;
    }

    public void addCommandListener(CommandListener commandListener) {
        commandListeners.add(commandListener);
    }
}