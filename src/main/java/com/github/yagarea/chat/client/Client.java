package com.github.yagarea.chat.client;

import com.github.yagarea.chat.shared.LoginResponse;
import com.github.yagarea.chat.shared.UserConsoleReader;
import com.github.yagarea.chat.shared.UserReader;
import com.github.yagarea.chat.shared.UserSystemInReader;
import com.github.yagarea.chat.shared.security.RSA;
import com.github.yagarea.chat.shared.SharedFunctions;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;

/**
 * The Client class is responsible for establishing a connection to the chat server,
 * handling user authentication, and facilitating communication between the user and the server.
 * It utilizes RSA encryption for secure transmission of user credentials and messages.
 */
public class Client {
    /**
     * The main method serves as the entry point for the Client application.
     * It establishes a socket connection to the server, handles user login,
     * and manages the sending and receiving of messages.
     *
     * @param args Command line arguments where args[0] is the server address
     *             and args[1] is the server port.
     */
    public static void main(String[] args) {
        try {
            Socket clientSocket = new Socket(args[0], Integer.parseInt(args[1]));

            RSA decryptor = new RSA();
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
            writer.println(decryptor.getE());
            writer.println(decryptor.getN());
            writer.flush();

            ResponsePrinterLoop responsePrinterLoop = new ResponsePrinterLoop(clientSocket, decryptor);
            BigInteger e = new BigInteger(responsePrinterLoop.readLine());
            BigInteger n = new BigInteger(responsePrinterLoop.readLine());
            RSA encryptor = new RSA(e, n);
            UserReader consoleReader = System.console() == null ? new UserSystemInReader() : new UserConsoleReader();

            LoginResponse usernameResponse;
            do {
                System.out.print("Nickname: ");
                String username = consoleReader.readLine();
                System.out.print("Password: ");
                String password = consoleReader.readPassword();
                writer.println(encryptor.encryptString(username));
                writer.println(encryptor.encryptString(password));
                writer.flush();
                usernameResponse = LoginResponse.valueOf(decryptor.decryptString(responsePrinterLoop.readLine()));
                System.out.println(usernameResponse.name());
            } while (usernameResponse != LoginResponse.LOGIN_ACCPETED && usernameResponse != LoginResponse.REGISTERED);
            new Thread(responsePrinterLoop).start();
            System.out.println("type :help for a list of all commands, or just type a message");
            while (true) {
                String messageToServer = consoleReader.readLine();
                if (!messageToServer.equals("")) {
                    if (messageToServer.toLowerCase().startsWith(":changepassword")) {
                        String psw=consoleReader.readPassword();
                        writer.println(encryptor.encryptString(":changePassword " + psw));
                        writer.flush();
                    }else if (messageToServer.toLowerCase().startsWith(":quit")) {
                        break;
                    }else if (messageToServer.toLowerCase().startsWith(":help")) {
                        System.out.println("commands:\n"+
                        "\t@[username] [message] - sends to client [username] [message]\n"+
                        "\t:clients - lists all connected clients\n"+
                        "\t:changepassword - reads the new password after pressing ENTER and\n\tchanges it. SAVE IT!\n"+
                        "\t:help - lists all commands\n"+
                        "\t:quit - disconnects from the server\n");
                    }else{
                        writer.println(encryptor.encryptString(messageToServer));
                        writer.flush();
                    }
                }
                
            }
            SharedFunctions.clearScreen();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}