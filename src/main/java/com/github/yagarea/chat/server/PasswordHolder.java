package com.github.yagarea.chat.server;

import com.github.yagarea.chat.shared.security.ShaUtil;

import java.util.Random;

/**
 * A class that holds a salted password hash for secure password storage and verification.
 * It generates a salted hash when a password is provided and allows access to the salt and hash
 * values for further authentication operations.
 */
public class PasswordHolder {
    private final String salt;
    private final String hash;

    /**
     * Constructs a {@link PasswordHolder} using the provided salt and hash values. 
     * This constructor is typically used when the salt and hash are already available, 
     * such as when storing a previously generated password hash.
     *
     * @param salt the salt used in the password hashing
     * @param hash the hash of the password concatenated with the salt
     */
    public PasswordHolder(String salt, String hash) {
        this.hash = hash;
        this.salt = salt;
    }

    /**
     * Constructs a {@link PasswordHolder} by generating a new random salt and hashing 
     * the provided password combined with the salt.
     *
     * @param password the password to be hashed and salted
     */
    public PasswordHolder(String password) {
        Random saltGenerator = new Random();
        this.salt = Long.toString(Math.abs(saltGenerator.nextLong()));
        this.hash = ShaUtil.hash(salt + password);
    }

    /**
     * Returns the salt used to hash the password.
     *
     * @return the salt used in the password hashing
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Returns the hashed password, which includes the salt.
     *
     * @return the hash of the password with the salt
     */
    public String getHash() {
        return hash;
    }
}
