package com.github.yagarea.chat.shared;

import java.io.Console;

/**
 * An implementation of the {@link UserReader} interface that uses the system's {@link Console}
 * to read input and passwords from the user.
 * <p>
 * This class provides methods to read lines of text and password inputs securely from the console.
 * It assumes the presence of a terminal or console environment where the {@link System#console()} method is available.
 * </p>
 */
public class UserConsoleReader implements UserReader {
    private final Console console = System.console();

    /**
     * Reads a line of text input from the console.
     *
     * @return the input line entered by the user.
     */
    @Override
    public String readLine() {
        return console.readLine();
    }

    /**
     * Reads a password input from the console, securely hiding the input characters.
     *
     * @return the password entered by the user as a {@link String}.
     */
    @Override
    public String readPassword() {
        return new String(console.readPassword());
    }
}
