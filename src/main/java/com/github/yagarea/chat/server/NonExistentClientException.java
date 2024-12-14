package com.github.yagarea.chat.server;

/**
 * Exception thrown when attempting to access or interact with a non-existent client.
 */
public class NonExistentClientException extends Exception {

    /**
     * Constructs a new NonExistentClientException with the specified detail message.
     *
     * @param message the detail message.
     */
    public NonExistentClientException(String message) {
        super(message);
    }

    /**
     * Constructs a new NonExistentClientException with the specified cause.
     *
     * @param cause the cause of the exception.
     */
    public NonExistentClientException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new NonExistentClientException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause of the exception.
     */
    public NonExistentClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
