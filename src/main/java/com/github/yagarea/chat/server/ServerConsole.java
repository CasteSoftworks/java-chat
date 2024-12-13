package com.github.yagarea.chat.server;

import com.github.yagarea.chat.shared.UserConsoleReader;
import com.github.yagarea.chat.shared.UserReader;
import com.github.yagarea.chat.shared.UserSystemInReader;
import com.google.common.base.Functions;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ServerConsole implements Runnable {
    private final UserReader consoleReader;
    private final Map<String, ClientConnection> clients;

    

    public ServerConsole(Map<String, ClientConnection> clients) {
        this.consoleReader = System.console() == null ? new UserSystemInReader() : new UserConsoleReader();
        this.clients = clients;
    }

    @Override
    public void run() {
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

    private void executeCommand(String command) {
        if (command.startsWith("kick ")) {
            kickUser(command.substring("kick ".length()));
        } else if (command.equals("clients")) {
            printListOfClients();
        } else if (command.startsWith("broadcast ")) {
            broadcast(command.substring("broadcast ".length()));
        }else if (command.equals("kill")) {
            killServer();
        } else if (command.equals("help")) {
            System.out.println("Server commands:" +
                    "\n\tkick" +
                    "\n\tbroadcast" +
                    "\n\tclients" +
                    "\n\tkill");
        } else {
            System.err.println(FunctionsS.ROSSO + "UNKNOWN COMMAND" + FunctionsS.RESET);
        }
    }

    private void killServer() {
        try {
            int nUtTot=clients.size();
            if(nUtTot==0){
                System.out.println(FunctionsS.GIALLO + "No users to kick" + FunctionsS.RESET);
            }else{
                System.out.print(FunctionsS.ROSSO + "Kicking users" + FunctionsS.RESET + " - [");

                for(int i=0;i<80;i++){
                    System.out.print(".");
                }
                System.out.print("]\r" + FunctionsS.ROSSO + "Kicking users" + FunctionsS.RESET + " - [");
                
                int nCanc=80/nUtTot;

                for (String username : clients.keySet()) {
                    kickUser(username);
                    System.out.print(FunctionsS.VERDE + "#".repeat(nCanc) + FunctionsS.RESET);
                    Thread.sleep(250);
                }
                System.out.println();
            }

            File file = new File("Auth.txt");
            if(file.delete()){
                System.out.println(FunctionsS.ROSSO + "Killing myself"  + FunctionsS.RESET);
                System.exit(0);
            }else{
                throw new IOException("File not cancelled");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void kickUser(String username) {
        try {
            clients.get(username).sendEncrypeted(FunctionsS.ROSSO + "SERVER: You have been kicked." + FunctionsS.RESET);
            clients.get(username).clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printListOfClients() {
        System.out.println(FunctionsS.VERDE + "Active users(" + clients.size() + "):" + FunctionsS.RESET);
        for (String username : clients.keySet()) {
            System.out.println("\t" + username);
        }
    }

    private void broadcast(String message) {
        for (ClientConnection cC : clients.values()) {
            cC.sendEncrypeted(FunctionsS.BLU+"SERVER: " + FunctionsS.RESET +message);
        }
    }
}