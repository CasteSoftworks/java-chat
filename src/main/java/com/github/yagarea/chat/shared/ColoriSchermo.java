package com.github.yagarea.chat.shared;

public class ColoriSchermo {

    // Definizioni per i codici ANSI (per Unix/Linux/macOS)
    private static String RESET = "\u001B[0m";
    private static String NERO = "\u001B[30m";
    private static String ROSSO = "\u001B[31m";
    private static String VERDE = "\u001B[32m";
    private static String GIALLO = "\u001B[33m";
    private static String BLU = "\u001B[34m";
    private static String MAGENTA = "\u001B[35m";
    private static String CIANO = "\u001B[36m";
    private static String BIANCO = "\u001B[37m";


    public static void ColoriScherm() {
        if (System.getProperty("os.name").contains("Windows")) {
            // Abilita i codici di escape ANSI su Windows 10 (versione 1511 o successiva)
            try {
                String osVersion = System.getProperty("os.version");
                if (osVersion != null && osVersion.startsWith("10")) {
                    // Attiva i colori ANSI se supportati
                    ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "REG ADD \"HKCU\\Console\" /v VirtualTerminalLevel /t REG_DWORD /d 1 /f");
                    processBuilder.inheritIO().start();
                }else{
                    RESET = "";
                    NERO = "";
                    ROSSO = "";
                    VERDE = "";
                    GIALLO = "";
                    BLU = "";
                    MAGENTA = "";
                    CIANO = "";
                    BIANCO = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ColoriScherm();
    }
}