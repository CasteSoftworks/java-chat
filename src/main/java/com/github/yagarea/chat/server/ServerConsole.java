package com.github.yagarea.chat.server;

import com.github.yagarea.chat.shared.UserConsoleReader;
import com.github.yagarea.chat.shared.UserReader;
import com.github.yagarea.chat.shared.UserSystemInReader;
import com.google.common.base.Functions;
import com.github.yagarea.chat.shared.SharedFunctions;


import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * A class that represents the server console, where server-side commands are
 * entered to manage client connections, broadcast messages, or shut down the server.
 * This class runs in its own thread to continuously read user input from the console
 * and execute the corresponding actions.
 */
public class ServerConsole implements Runnable{
    private final UserReader consoleReader;
    private final Map<String, ClientConnection> clients;
    public String wrongNick = SharedFunctions.ROSSO + "SERVER: WRONG NICKNAME" + SharedFunctions.RESET;
    
    /**
     * Constructs a new {@link ServerConsole} instance that will handle commands 
     * for managing clients. It uses a {@link UserSystemInReader} or a 
     * {@link UserConsoleReader} depending on the availability of the system console.
     *
     * @param clients a map of currently active client connections
     */
    public ServerConsole(Map<String, ClientConnection> clients){
        this.consoleReader = System.console() == null ? new UserSystemInReader() : new UserConsoleReader();
        this.clients = clients;
    }

    /**
     * Continuously reads input from the console and executes the corresponding
     * server command.
     * <p>
     * The commands can include kicking a user, listing clients, broadcasting messages, 
     * shutting down the server, and sending private messages to clients.
     */
    @Override
    public void run(){
        while (true) {
            try {
                String line = consoleReader.readLine();
                if (line != null) {
                    executeCommand(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Executes a command entered by the server operator.
     * <p>
     * Supported commands:
     * <ul>
     *     <li>kick [client] - Kicks a specified client from the chat.</li>
     *     <li>clients - Lists all connected clients.</li>
     *     <li>broadcast [message] - Sends a message to all connected clients.</li>
     *     <li>kill - Kicks all clients, deletes the Auth.txt file, and shuts down the server.</li>
     *     <li>shutdown - Shuts down the server.</li>
     *     <li>@[client] [message] - Sends a private message to a specified client.</li>
     * </ul>
     *
     * @param command the command entered by the server operator
     */
    private void executeCommand(String command){
        if(command.startsWith("kick ")) {
            kickUser(command.substring("kick ".length()));
        }else if(command.equals("clients")){
            printListOfClients();
        }else if(command.startsWith("broadcast ")){
            broadcast(command.substring("broadcast ".length()));
        }else if(command.equals("kill")){
            killServer();
        }else if(command.equals("help")){
            printHelp();
        }else if(command.equals("shutdown")){
            System.exit(0);
        }else if(command.startsWith("@")){
            String c=command.substring("@".length(),command.indexOf(" "));
            String m=command.substring(command.indexOf(" ")+1);
            privateServerMessage(c,m);
        }else{
            System.err.println(SharedFunctions.ROSSO + "UNKNOWN COMMAND" + SharedFunctions.RESET);
        }
    }

    /**
     * Shuts down the server by first kicking all clients, deleting the authentication 
     * file (Auth.txt), and then stopping the server.
     */
    private void killServer(){
        try{
            int nUtTot=clients.size();
            if(nUtTot==0){
                System.out.println(SharedFunctions.GIALLO + "No users to kick" + SharedFunctions.RESET);
            }else{
                System.out.print(SharedFunctions.ROSSO + "Kicking users" + SharedFunctions.RESET + " - [");

                for(int i=0;i<80;i++){
                    System.out.print(".");
                }
                System.out.print("]\r" + SharedFunctions.ROSSO + "Kicking users" + SharedFunctions.RESET + " - [");
                
                int nCanc=80/nUtTot;

                for(String username : clients.keySet()){
                    kickUser(username);
                    System.out.print(SharedFunctions.VERDE + "#".repeat(nCanc) + SharedFunctions.RESET);
                    Thread.sleep(250);
                }
                System.out.println();
            }

            File file = new File("Auth.txt");
            if(file.delete()){
                System.out.println(SharedFunctions.ROSSO + "Killing myself"  + SharedFunctions.RESET);
                System.exit(0);
            }else{
                throw new IOException("File not cancelled");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Kicks a specified user from the chat room and closes their client connection.
     * 
     * @param username the username of the client to kick
     */
    private void kickUser(String username){
        try{
            if(clients.keySet().contains(username)){
                clients.get(username).sendEncrypeted(SharedFunctions.ROSSO + "SERVER: You have been kicked." + SharedFunctions.RESET);
                clients.get(username).clientSocket.close();
            }else{
                throw new NonExistentClientException(wrongNick);
            }
        }catch(IOException e){
            e.printStackTrace();
        }catch(NonExistentClientException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Sends a private message to a specified client.
     * 
     * @param username the username of the client to send the message to
     * @param message the message to send to the client
     */
    private void printListOfClients(){
        System.out.println(SharedFunctions.VERDE + "Active users(" + clients.size() + "):" + SharedFunctions.RESET);
        for(String username : clients.keySet()){
            System.out.println("\t" + username);
        }
    }

    /**
     * Sends a message to all connected clients.
     *
     * @param message the message to broadcast to all clients
     */
    private void broadcast(String message){
        for(ClientConnection cC : clients.values()){
            cC.sendEncrypeted(SharedFunctions.BLU+"SERVER: " + SharedFunctions.RESET + message);
        }
    }

    /**
     * Sends a private message to a specified client.
     * 
     * @param username the username of the client to send the message to
     * @param message the message to send to the client
     */
    private void privateServerMessage(String username, String message){
        try{
            if(clients.keySet().contains(username)){
                clients.get(username).sendEncrypeted(SharedFunctions.CIANO + "PRIVATE " + SharedFunctions.BLU + "SERVER: " + SharedFunctions.RESET + message);
            }else{
                throw new NonExistentClientException(wrongNick);
            }
        }catch(NonExistentClientException e){
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Prints a help message with available server commands.
     */
    private void printHelp() {
        System.out.println("Server commands:" +
                "\n\tkick [client] - kicks a specified [client] from the chat" +
                "\n\tbroadcast [message] - sends [message] to all connected clients" +
                "\n\tclients - lists all connected clients" +
                "\n\tkill - kicks clients, deletes Auth.txt and shuts server down" +
                "\n\tshutdown - shuts down server" +
                "\n\t@[client] [message] - sends [message] to [client]");
    }
}