package io.github.unknowncoder56.javaslc;

import com.google.gson.JsonObject;

/**
 * A class to create a context object which contains details about the command like message content, owner (author), server ID, {@link Bot} instance, command name and command arguments.
 */
public class CommandContext extends MessageContext {

    private final String command;
    private final String[] arguments;

    /**
     * Constructor to create an instance of the class for a command without arguments.
     * @param message The message JSON retrieved from the SLChat API.
     * @param serverId The ID of the server where the message was sent.
     * @param bot The {@link Bot} instance receiving the message event.
     * @param command The command name of the command issued by the user.
     */
    public CommandContext(JsonObject message, String serverId, Bot bot, String command) {
        super(message, serverId, bot);
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
    public CommandContext(JsonObject message, String serverId, Bot bot, String command, String[] arguments) {
        super(message, serverId, bot);
        this.command = command;
        this.arguments = arguments;
    }

    /**
     * Gets the command name of the command received.
     * Note: The command name does not contain the bot prefix.
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
