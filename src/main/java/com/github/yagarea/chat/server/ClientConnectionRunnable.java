package com.github.yagarea.chat.server;

import com.github.yagarea.chat.shared.security.RSA;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

/**
 * A Runnable that handles the connection of a new client to the server. It
 * initializes a new {@link ClientConnection} object for the client, adds it to
 * the list of active clients, and starts listening for incoming messages from
 * the client.
 */
public class ClientConnectionRunnable implements Runnable {


    private final Socket clientSocket;
    private final Map<String, ClientConnection> clients;
    private final RSA decryptor;
    private final Authenticator auth;

    /**
     * Constructs a {@link ClientConnectionRunnable} with the given client socket,
     * active clients map, RSA decryptor, and authenticator.
     *
     * @param clientSocket the socket representing the client's connection
     * @param clients the map of active clients
     * @param decryptor the RSA decryptor used for message decryption
     * @param auth the authenticator used to validate the client
     */
    public ClientConnectionRunnable(Socket clientSocket, Map<String, ClientConnection> clients, RSA decryptor, Authenticator auth) {

        this.clientSocket = clientSocket;
        this.clients = clients;
        this.decryptor = decryptor;
        this.auth = auth;
    }

    /**
     * This method is executed when the {@link Runnable} is started. It creates a
     * new {@link ClientConnection} for the client, adds the connection to the list
     * of active clients, and starts the client's listening process for incoming messages.
     * 
     * If an {@link IOException} occurs during client initialization or connection,
     * the exception is printed to the console.
     */
    @Override
    public void run() {
        try {
            ClientConnection newClient = new ClientConnection(clientSocket, clients, decryptor, auth);
            clients.put(newClient.username, newClient);
            newClient.startListenig();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
