package com.ifmo.epampractice.serviceimpl;

import com.ifmo.epampractice.exceptions.HashingException;
import com.ifmo.epampractice.services.PasswordHashService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class PasswordHashServiceImpl implements PasswordHashService {

    private final char SALT_SEPARATOR = '#';

    @Override
    public String getHash(String password) throws HashingException {
        byte[] saltBytes = getSalt();
        byte[] passwordBytes = password.getBytes(StandardCharsets.US_ASCII);
        byte[] passwordHashBytes = getHash(passwordBytes, saltBytes);

        return new String(saltBytes, StandardCharsets.US_ASCII) +
                SALT_SEPARATOR +
                new String(passwordHashBytes, StandardCharsets.US_ASCII);
    }

    @Override
    public boolean isMatching(String saltedHash, String password)
            throws HashingException, IllegalArgumentException {

        String[] hashParts = saltedHash.split(Character.toString(SALT_SEPARATOR));
        if (hashParts.length != 2) {
            throw new IllegalArgumentException("'saltedHash' should contain a salt and a hash " +
                    "separated by '" + SALT_SEPARATOR + "' character.");
        }

        byte[] saltBytes = hashParts[0].getBytes(StandardCharsets.US_ASCII);
        byte[] hashBytes = hashParts[1].getBytes(StandardCharsets.US_ASCII);
        byte[] passwordBytes = password.getBytes(StandardCharsets.US_ASCII);
        byte[] passwordHashBytes = getHash(passwordBytes, saltBytes);

        return Arrays.equals(hashBytes, passwordHashBytes);
    }

    private byte[] getSalt() {
        SecureRandom rand = new SecureRandom();
        byte[] saltBytes = new byte[16];
        rand.nextBytes(saltBytes);

        // Ensure salt string only contains allowed letters
        for (int i = 0; i < saltBytes.length; i++) {
            saltBytes[i] = ensureInAlphabet(saltBytes[i]);
        }
        return saltBytes;
    }

    private byte[] getHash(byte[] input, byte[] salt) throws HashingException {
        final String HASH_ALGORITHM = "SHA-512";

        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashBytes = md.digest(input);

            // Ensure hash string only contains allowed letters
            for (int i = 0; i < hashBytes.length; i++) {
                hashBytes[i] = ensureInAlphabet(hashBytes[i]);
            }

            return hashBytes;
        }
        catch (NoSuchAlgorithmException e) {
            throw new HashingException("Hashing algorithm not found: " + HASH_ALGORITHM, e);
        }
    }

    /**
     * Ensures input byte value is between ALPHABET_START and ALPHABET_END.
     */
    private byte ensureIsInAlphabet(byte input) {
        // Allowed characters for hash/salt output: 0-9 a-z A-Z :;<=>?@
        final byte ALPHABET_START = 48;
        final byte ALPHABET_END = 122;

        final byte ALPHABET_SIZE = ALPHABET_END - ALPHABET_START + 1;
        return (byte)(ALPHABET_START + (Math.abs(input) % ALPHABET_SIZE));
    }
}
