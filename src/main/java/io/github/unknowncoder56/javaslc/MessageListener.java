package io.github.unknowncoder56.javaslc;

/**
 * An interface to implement the method for receiving message events.
 */
public interface MessageListener {

    /**
     * Method to implement for receiving message events.
     * @param context The {@link MessageContext} of the message.
     * @see MessageContext
     */
    void onMessage(MessageContext context);
}

