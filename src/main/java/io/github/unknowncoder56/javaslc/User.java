package io.github.unknowncoder56.javaslc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.fluent.Request;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * This class describes a user or bot.
 */
public class User {

    protected final long userId;
    protected ErrorListener errorListener;

    /**
     * Constructor to build a user.
     * @param userId The user's user ID.
     * @param errorListener The {@link ErrorListener} for the instance.
     * @see ErrorListener
     */
    public User(long userId, ErrorListener errorListener) {
        this.userId = userId;
        this.errorListener = errorListener;
    }

    /**
     * Gets the user ID of the user.
     * @return The user ID of the user.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Gets the {@link ErrorListener} of this instance.
     * @return The {@link ErrorListener} of this instance.
     */
    public ErrorListener getErrorListener() {
        return errorListener;
    }

    /**
     * Sets the {@link ErrorListener} of this instance.
     * @param errorListener The {@link ErrorListener} of this instance.
     */
    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    /**
     * Gets the account creation {@link LocalDateTime} of the user.
     * @return A {@link CompletableFuture} containing the {@link LocalDateTime}, that will be completed when the data is received from the API.
     */
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

    /**
     * Gets the label {@link JsonObject} of the user.
     * @return A {@link CompletableFuture} containing the {@link JsonObject}, that will be completed when the data is received from the API.
     */
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

    /**
     * Gets the nickname of the user.
     * @return A {@link CompletableFuture} containing the nickname, that will be completed when the data is received from the API.
     */
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

    /**
     * Gets the profile image URL {@link String} of the user.
     * @return A {@link CompletableFuture} containing the URL {@link String}, that will be completed when the data is received from the API.
     */
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

    /**
     * Gets the array of server IDs of the servers joined by the user.
     * @return A {@link CompletableFuture} containing the array of IDs, that will be completed when the data is received from the API.
     */
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

    /**
     * Gets the username of the user.
     * @return A {@link CompletableFuture} containing the username, that will be completed when the data is received from the API.
     */
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
