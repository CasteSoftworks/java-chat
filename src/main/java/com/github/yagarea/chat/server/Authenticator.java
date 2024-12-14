package com.github.yagarea.chat.server;

import com.github.yagarea.chat.shared.security.ShaUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Authenticator class is responsible for managing user authentication and registration.
 * It reads user credentials from a specified file and provides methods to register new users,
 * authenticate existing users, and change user passwords.
 */
public class Authenticator {


    private Pattern login = Pattern.compile("(\\w+):(\\d+):(.+)");
    private Map<String, PasswordHolder> users;

    private String file;

    /**
     * Constructs an Authenticator instance with the specified file.
     * It initializes the user map by reading user credentials from the file.
     *
     * @param file the path to the file containing user credentials
     */
    public Authenticator(String file) {
        this.file = file;
        try {
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            this.users = new HashMap<>();
            while (true) {
                String line = fileReader.readLine();
                if (line != null && !line.equals("")) {
                    Matcher lineMatcher = login.matcher(line);
                    lineMatcher.find();
                    users.put(lineMatcher.group(1), new PasswordHolder(lineMatcher.group(2), lineMatcher.group(3)));
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if a user is registered.
     *
     * @param username the username to check
     * @return true if the user is registered, false otherwise
     */
    public boolean userIsRegistered(String username) {
        return users.get(username) != null;
    }

    /**
     * Authenticates a user with the provided username and password.
     *
     * @param descryptedUsername the username of the user
     * @param descryptedPassword the password of the user
     * @return true if authentication is successful, false otherwise
     */
    public boolean authenticate(String descryptedUsername, String descryptedPassword) {
        String hashPassword = ShaUtil.hash(users.get(descryptedUsername).getSalt() + descryptedPassword);
        return hashPassword.equals(users.get(descryptedUsername).getHash());
    }

    /**
     * Registers a new user with the specified username and password.
     *
     * @param newUsername the username of the new user
     * @param newPassword the password of the new user
     */
    public void registerUser(String newUsername, String newPassword) {
        try {
            PrintWriter fileWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            PasswordHolder newPasswordHolder = new PasswordHolder(newPassword);
            fileWriter.println(newUsername + ":" + newPasswordHolder.getSalt() + ":" + newPasswordHolder.getHash());
            fileWriter.flush();
            users.put(newUsername, newPasswordHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Changes the password for the specified user.
     *
     * @param username   the username of the user whose password is to be changed
     * @param newPassword the new password for the user
     */
    public void changePassword(String username, String newPassword) {
        try {
            PrintWriter fileWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, false)));
            PasswordHolder newPasswordHolder = new PasswordHolder(newPassword);
            users.put(username, newPasswordHolder);
            for (String userNameInSet : users.keySet()) {
                fileWriter.println(userNameInSet + ":" + users.get(userNameInSet).getSalt() + ":" + users.get(userNameInSet).getHash());
            }
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}