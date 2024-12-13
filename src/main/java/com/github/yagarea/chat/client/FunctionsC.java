package com.github.yagarea.chat.client;

public class FunctionsC {
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

    public static final String RESET = "\u001B[0m";
    public static final String NERO = "\u001B[30m";
    public static final String ROSSO = "\u001B[31m";
    public static final String VERDE = "\u001B[32m";
    public static final String GIALLO = "\u001B[33m";
    public static final String BLU = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CIANO = "\u001B[36m";
    public static final String BIANCO = "\u001B[37m";
}
