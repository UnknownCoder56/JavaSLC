package io.github.unknowncoder56.javaslc;

/**
 * An interface to implement the method for receiving command (messages sent with the currently set bot prefix) events.
 */
public interface CommandListener {

    /**
     * Method to implement for receiving command events.
     * @param context The {@link CommandContext} of the message.
     * @see CommandContext
     */
    void onCommand(CommandContext context);
}
