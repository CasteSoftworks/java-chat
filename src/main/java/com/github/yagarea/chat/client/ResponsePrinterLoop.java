package com.github.yagarea.chat.client;

import com.github.yagarea.chat.shared.security.RSA;
import com.github.yagarea.chat.shared.SharedFunctions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * The ResponsePrinterLoop class implements Runnable and is responsible for
 * continuously reading responses from a socket and printing the decrypted
 * messages to the console. It handles the decryption of messages using
 * an RSA decryptor and manages the connection state.
 */
public class ResponsePrinterLoop implements Runnable {
    private BufferedReader socketReader;
    private RSA decryptor;

    /**
     * Constructs a ResponsePrinterLoop with the specified socket and RSA decryptor.
     *
     * @param input the socket from which to read messages
     * @param decryptor the RSA decryptor used to decrypt messages
     * @throws IOException if an I/O error occurs when creating the BufferedReader
     */
    public ResponsePrinterLoop(Socket input, RSA decryptor) throws IOException {
        this.decryptor = decryptor;
        this.socketReader = new BufferedReader(new InputStreamReader(input.getInputStream()));
    }

    /**
     * Continuously reads lines from the socket and prints the decrypted messages
     * to the console. If the server disconnects, it clears the screen and
     * prints a disconnect message before terminating the application.
     */
    @Override
    public void run() {
        try {
            while (true) {
                String line = socketReader.readLine();
                if (line != null) {
                    System.out.println(decryptor.decryptString(line));
                } else {
                    SharedFunctions.clearScreen();
                    System.err.println(SharedFunctions.ROSSO + "SERVER DISCONNECTED" + SharedFunctions.RESET);
                    // Must by sys.exit - when only break; -ing, main Thread will continue to run
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a line from the socket.
     *
     * @return the line read from the socket
     * @throws IOException if an I/O error occurs
     */
    public String readLine() throws IOException {
        return socketReader.readLine();
    }
}
