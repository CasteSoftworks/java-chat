package com.github.yagarea.chat.server;

import com.github.yagarea.chat.shared.security.RSA;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The main server class that listens for incoming client connections, initializes
 * new client connections, and manages client communication in a multi-threaded environment.
 * It also starts a console for server-side interaction and handles RSA decryption 
 * for secure client communications.
 */
public class Server {

    /**
     * The entry point of the server application. It sets up the RSA decryptor, 
     * initializes the server socket on the specified port, and starts listening 
     * for incoming client connections. For each new connection, a new thread is 
     * started to handle communication with the client.
     *
     * @param args the command-line arguments. args[0] is the path to the authentication 
     *             file, and args[1] is the port number on which the server will listen.
     */
    public static void main(String[] args) {
        RSA decryptor = new RSA();
        final int PORT = Integer.parseInt(args[1]);

        ServerSocket chatServer = null;
        try {
            chatServer = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, ClientConnection> clients = new ConcurrentHashMap<>();
        Authenticator auth = new Authenticator(args[0]);

        new Thread(new ServerConsole(clients)).start();

        while (chatServer != null) {
            try {
                Socket clientSocket = chatServer.accept();
                ClientConnectionRunnable clientConnectionInit = new ClientConnectionRunnable(clientSocket, clients, decryptor, auth);
                new Thread(clientConnectionInit).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}