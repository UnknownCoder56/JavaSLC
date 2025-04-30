package io.github.unknowncoder56.javaslc;

import java.util.ArrayList;

/**
 * A builder class to build a {@link Bot} instance.
 */
public class BotBuilder {

    private String prefix = "";
    private StartListener startListener = null;
    private ErrorListener errorListener = null;
    private String token = "";
    private String botUserId = "";
    private final ArrayList<MessageListener> messageListeners = new ArrayList<>();
    private final ArrayList<CommandListener> commandListeners = new ArrayList<>();

    /**
     * Private constructor to force the usage of {@link BotBuilder#newInstance()}.
     * @see BotBuilder#newInstance()
     */
    private BotBuilder() {

    }

    /**
     * Returns a new {@link BotBuilder} instance. This method is used for instantiation to enable chaining of method calls.
     * @return A new {@link BotBuilder} instance to which method calls are to be chained.
     */
    public static BotBuilder newInstance() {
        return new BotBuilder();
    }

    /**
     * Sets the bot prefix.
     * @param prefix The bot prefix.
     * @return The {@link BotBuilder} instance to facilitate chaining of method calls.
     */
    public BotBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * Sets the start listener.
     * @param startListener The start listener.
     * @return The {@link BotBuilder} instance to facilitate chaining of method calls.
     * @see StartListener
     */
    public BotBuilder setStartListener(StartListener startListener) {
        this.startListener = startListener;
        return this;
    }

    /**
     * Sets the error listener.
     * @param errorListener The error listener.
     * @return The {@link BotBuilder} instance to facilitate chaining of method calls.
     * @see ErrorListener
     */
    public BotBuilder setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
        return this;
    }

    /**
     * Sets the bot token. It is a confidential value, so it cannot be accessed anymore.
     * @param token The bot token.
     * @return The {@link BotBuilder} instance to facilitate chaining of method calls.
     */
    public BotBuilder setToken(String token) {
        this.token = token;
        return this;
    }

    /**
     * Sets the bot user ID.
     * @param botUserId The bot user ID.
     * @return The {@link BotBuilder} instance to facilitate chaining of method calls.
     */
    public BotBuilder setBotUserId(String botUserId) {
        this.botUserId = botUserId;
        return this;
    }

    /**
     * Adds a message listener.
     * @param messageListener The message listener.
     * @return The {@link BotBuilder} instance to facilitate chaining of method calls.
     * @see MessageListener
     */
    public BotBuilder addMessageListener(MessageListener messageListener) {
        messageListeners.add(messageListener);
        return this;
    }

    /**
     * Adds a command listener.
     * @param commandListener The command listener.
     * @return The {@link BotBuilder} instance to facilitate chaining of method calls.
     * @see CommandListener
     */
    public BotBuilder addCommandListener(CommandListener commandListener) {
        commandListeners.add(commandListener);
        return this;
    }

    /**
     * Builds the bot and returns a {@link Bot} instance constructed with the specified values. Any skipped values are set to default. Skipping the prefix, token, or bot user ID will result in a {@link RuntimeException} if not set later in the {@link Bot} before running the {@link Bot#run()} method.
     * @return A {@link Bot} instance constructed with the specified values.
     */
    public Bot build() {
        return new Bot(prefix, startListener, errorListener, token, botUserId, messageListeners, commandListeners);
    }
}
