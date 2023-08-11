package com.uniqueapps.javaslc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.fluent.Request;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public class User {

    protected final long userId;
    protected ErrorListener errorListener;

    public User(long userId, ErrorListener errorListener) {
        this.userId = userId;
        this.errorListener = errorListener;
    }

    public long getUserId() {
        return userId;
    }

    public ErrorListener getErrorListener() {
        return errorListener;
    }

    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public CompletableFuture<LocalDateTime> getAccountCreationDate() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String responseJsonString = Request.get("https://chat.slsearch.eu.org/api/user/" + userId + "/").execute().returnContent().asString();
                JsonObject responseJson = JsonParser.parseString(responseJsonString).getAsJsonObject();
                return LocalDateTime.parse(responseJson.get("creation_date").getAsString());
            } catch (IOException e) {
                if (errorListener != null) {
                    errorListener.onError(e, "getAccountCreationDate");
                }
                System.out.println("Failed to fetch account creation date: " + e.getMessage());
                return null;
            }
        });
    }

    public CompletableFuture<JsonObject> getLabelJsonObject() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String responseJsonString = Request.get("https://chat.slsearch.eu.org/api/user/" + userId + "/").execute().returnContent().asString();
                JsonObject responseJson = JsonParser.parseString(responseJsonString).getAsJsonObject();
                return responseJson.get("label").getAsJsonObject();
            } catch (IOException e) {
                if (errorListener != null) {
                    errorListener.onError(e, "getLabelJsonObject");
                }
                System.out.println("Failed to fetch label JSON object: " + e.getMessage());
                return null;
            }
        });
    }

    public CompletableFuture<String> getNickname() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String responseJsonString = Request.get("https://chat.slsearch.eu.org/api/user/" + userId + "/").execute().returnContent().asString();
                JsonObject responseJson = JsonParser.parseString(responseJsonString).getAsJsonObject();
                return responseJson.get("nickname").getAsString();
            } catch (IOException e) {
                if (errorListener != null) {
                    errorListener.onError(e, "getNickname");
                }
                System.out.println("Failed to fetch nickname: " + e.getMessage());
                return null;
            }
        });
    }

    public CompletableFuture<String> getProfileImageUrl() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String responseJsonString = Request.get("https://chat.slsearch.eu.org/api/user/" + userId + "/").execute().returnContent().asString();
                JsonObject responseJson = JsonParser.parseString(responseJsonString).getAsJsonObject();
                return responseJson.get("profile_img").getAsString();
            } catch (IOException e) {
                if (errorListener != null) {
                    errorListener.onError(e, "getProfileImageUrl");
                }
                System.out.println("Failed to fetch profile image URL: " + e.getMessage());
                return null;
            }
        });
    }

    public CompletableFuture<long[]> getJoinedServerIds() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String responseJsonString = Request.get("https://chat.slsearch.eu.org/api/user/" + userId + "/").execute().returnContent().asString();
                JsonObject responseJson = JsonParser.parseString(responseJsonString).getAsJsonObject();
                return responseJson.get("servers").getAsJsonArray().asList().stream().mapToLong(JsonElement::getAsLong).toArray();
            } catch (IOException e) {
                if (errorListener != null) {
                    errorListener.onError(e, "getJoinedServerIds");
                }
                System.out.println("Failed to fetch joined server IDs: " + e.getMessage());
                return null;
            }
        });
    }

    public CompletableFuture<String> getUsername() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String responseJsonString = Request.get("https://chat.slsearch.eu.org/api/user/" + userId + "/").execute().returnContent().asString();
                JsonObject responseJson = JsonParser.parseString(responseJsonString).getAsJsonObject();
                return responseJson.get("username").getAsString();
            } catch (IOException e) {
                if (errorListener != null) {
                    errorListener.onError(e, "getUsername");
                }
                System.out.println("Failed to fetch username: " + e.getMessage());
                return null;
            }
        });
    }
}
