package io.github.unknowncoder56.javaslc;

/**
 * An interface to implement the method for receiving error events.
 */
public interface ErrorListener {

    /**
     * Method to implement for receiving error events.
     * @param e The {@link Exception} of the error.
     * @param errorContext The name of the method where the error occurred.
     */
    void onError(Exception e, String errorContext);
}
