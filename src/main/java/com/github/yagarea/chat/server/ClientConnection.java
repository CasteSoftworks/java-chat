package com.github.yagarea.chat.server;

import com.github.yagarea.chat.shared.LoginResponse;
import com.github.yagarea.chat.shared.security.RSA;
import com.github.yagarea.chat.shared.SharedFunctions;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a connection to a client in the chat server.
 * This class handles user authentication, message decryption/encryption,
 * and communication with other clients in the chat room.
 */
public class ClientConnection {
    private static final Pattern PRIVATE_MESSAGE_NICKNAME_PATTERN = Pattern.compile("@(\\w+) (.*)");
    private static final Pattern NICKNAME_RULES = Pattern.compile("\\w+");


    protected final String username;
    private final PrintWriter socketWriter;
    private final Map<String, ClientConnection> clients;
    private final RSA decryptor;
    private final RSA encryptor;
    private final BufferedReader socketReader;
    private final Authenticator authenticator;
    protected final Socket clientSocket;

    /**
     * Constructs a ClientConnection instance.
     *
     * @param clientSocket the socket connected to the client
     * @param clients a map of currently connected clients
     * @param decryptor the RSA decryptor for incoming messages
     * @param authenticator the authenticator for user login
     * @throws IOException if an I/O error occurs when creating input/output streams
     */
    ClientConnection(Socket clientSocket, Map<String, ClientConnection> clients, RSA decryptor, Authenticator authenticator) throws IOException {
        this.socketReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.socketWriter = new PrintWriter(clientSocket.getOutputStream());
        this.clients = clients;
        this.decryptor = decryptor;
        this.clientSocket = clientSocket;

        sendEncryptionKeys();
        this.authenticator = authenticator;
        encryptor = makeEncryptor();
        this.username = authenticateUser();
    }

    /**
     * Authenticates the user by reading encrypted username and password,
     * decrypting them, and validating against the registered users.
     *
     * @return the authenticated username
     * @throws IOException if an I/O error occurs during reading
     */
    private String authenticateUser() throws IOException {
        while (true) {
            String encryptedUsername = socketReader.readLine();
            String decryptedUsername = decryptor.decryptString(encryptedUsername);
            String encryptedPassword = socketReader.readLine();
            String decryptedPassword = decryptor.decryptString(encryptedPassword);

            if (NICKNAME_RULES.matcher(decryptedUsername).matches()) {
                if (!clients.containsKey(decryptedUsername)) {
                    if (authenticator.userIsRegistered(decryptedUsername)) {
                        if (authenticator.authenticate(decryptedUsername, decryptedPassword)) {
                            sendEncrypeted(LoginResponse.LOGIN_ACCPETED.name());
                            return decryptedUsername;
                        } else {
                            sendEncrypeted(LoginResponse.PASSWORD_INVALID.name());
                        }
                    } else {
                        authenticator.registerUser(decryptedUsername, decryptedPassword);
                        sendEncrypeted(LoginResponse.REGISTERED.name());
                        return decryptedUsername;
                    }
                } else {
                    sendEncrypeted(LoginResponse.ALREADY_LOGGED_IN.name());
                }
            } else {
                sendEncrypeted(LoginResponse.INVALID_USERNAME.name());
            }
        }

    }

    /**
     * Starts listening for messages from the client and processes them.
     * Handles private messages, client list requests, and password changes.
     */
    public void startListenig() {
        broadcast(SharedFunctions.BLU+"SERVER: " + SharedFunctions.GIALLO + username + SharedFunctions.RESET + " has joined this chatting room");
        System.out.println("SERVER: " + username + " has joined this chatting room");
        try {
            while (true) {
                String clientData = socketReader.readLine();
                if (clientData == null) {
                    disconnect();
                    break;
                }

                String message = decryptor.decryptString(clientData);
                Matcher privateMessageMatcher = PRIVATE_MESSAGE_NICKNAME_PATTERN.matcher(message);
                if (message.equals(":clients")) {
                    sendClientList();
                } else if (message.startsWith(":changePassword")) {
                    String newspwd=message.substring(":changePassword ".length());
                    authenticator.changePassword(username, newspwd);
                    sendEncrypeted(SharedFunctions.GIALLO + "PASSWORD CHANGED" + SharedFunctions.RESET);
                } else if (privateMessageMatcher.matches()) {
                    sendPrivateMessage(privateMessageMatcher);
                } else {
                    broadcast(SharedFunctions.MAGENTA + username + ": " + SharedFunctions.RESET + message);
                }
            }
        } catch (IOException e) {
            disconnect();
        }
    }
    /**
     * Removes the current user from the list of active clients and broadcasts
     * a message to the remaining users indicating that the user has disconnected
     * from the chat room.
     */
    private void disconnect() {
        clients.remove(username);
        broadcast(SharedFunctions.GIALLO + username + " has disconnected this chatting room" + SharedFunctions.RESET);
    }

    /**
     * Broadcasts a message to all connected clients
     * @param message a message to broadcast
     */
    private void broadcast(String message) {
        for (ClientConnection cC : clients.values()) {
            if (cC != this) {
                cC.sendEncrypeted(message);
            }
        }
    }

    /**
     * Sends encrypted data via socket
     * @param data an encrypted message
     */
    private void send(String data) {
        try {
            socketWriter.println(data);
            socketWriter.flush();
        } catch (Exception e) {
            disconnect();
        }
    }

    /**
     * Calls encryptor.encryptString on message and sends the result to send()
     * @param message a message to broadcast
     */
    protected void sendEncrypeted(String message) {
        send(encryptor.encryptString(message));
    }

    /**
     * Sends an encrypted list of all the connected clients
     */
    private void sendClientList() {
        for (String nickname : clients.keySet()) {
            sendEncrypeted("\t" + nickname);
        }
    }

    /**
     * Sends a private message to a specified client.
     *
     * @param privateMessageMatcher a Matcher object that contains the private message details.
     *                               The first group should be the recipient's nickname,
     *                               and the second group should be the message text.
     */
    private void sendPrivateMessage(Matcher privateMessageMatcher) {
        String to = privateMessageMatcher.group(1);
        String messageText = privateMessageMatcher.group(2);
        if (clients.keySet().contains(to)) {
            clients.get(to).sendEncrypeted(SharedFunctions.CIANO + "PRIVATE " + SharedFunctions.MAGENTA +username + ": " + SharedFunctions.RESET + messageText);
        } else {
            sendEncrypeted(SharedFunctions.ROSSO + "SERVER: WRONG NICKNAME" + SharedFunctions.RESET);
        }
    }

    /**
     * Sends the encryption keys to the client.
     * The keys are sent as two separate strings: the exponent and the modulus.
     */
    private void sendEncryptionKeys() {
        send(decryptor.getE().toString());
        send(decryptor.getN().toString());
    }

    /**
     * Creates an RSA encryptor using the provided public key parameters.
     *
     * @return an RSA object initialized with the public key parameters.
     * @throws IOException if an I/O error occurs while reading the key parameters.
     */
    private RSA makeEncryptor() throws IOException {
        BigInteger e = new BigInteger(socketReader.readLine());
        BigInteger n = new BigInteger(socketReader.readLine());
        return new RSA(e, n);
    }

}