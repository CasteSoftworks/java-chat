package com.github.yagarea.chat.shared;

import java.io.IOException;

/**
 * An interface for reading user input from different sources.
 * <p>
 * Implementations of this interface should provide methods to read a line of text and securely
 * read a password from the user.
 * </p>
 */
public interface UserReader {
    String readLine() throws IOException;
    String readPassword() throws IOException;
}
