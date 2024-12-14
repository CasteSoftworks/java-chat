package com.github.yagarea.chat.shared;

/**
 * A utility class that provides shared constants and functions used across the application.
 * This class includes console escape codes for text colors and a method to clear the console screen.
 */
public class SharedFunctions {
    /**
     * Collection of console escape codes for colours
     */
    public static final String RESET = "\u001B[0m";
    public static final String NERO = "\u001B[30m";
    public static final String ROSSO = "\u001B[31m";
    public static final String VERDE = "\u001B[32m";
    public static final String GIALLO = "\u001B[33m";
    public static final String BLU = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CIANO = "\u001B[36m";
    public static final String BIANCO = "\u001B[37m";

    /**
     * Clears the console screen. The method works by executing system-specific commands
     * to clear the screen. It detects the operating system and uses the appropriate command:
     * <ul>
     *     <li>"cls" for Windows</li>
     *     <li>"clear" for Unix-based systems (Linux, macOS, etc.)</li>
     * </ul>
     * If the screen clearing operation fails, it prints the stack trace of the exception.
     */
    public static void clearScreen(){
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
