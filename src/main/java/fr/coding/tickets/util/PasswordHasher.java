package fr.coding.tickets.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utilitaire de hashing simple utilisant uniquement SHA-256 (sans salt).
 * 
 */
public class PasswordHasher {

    private static final String ALGORITHM = "SHA-256";

    /**
     * Hash un mot de passe en clair avec SHA-256 simple
     *
     * @param plainPassword Le mot de passe en clair
     * @return hash SHA-256 encodé en Base64
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors du hashing du mot de passe", e);
        }
    }

    /**
     * Vérifie si un mot de passe correspond au hash SHA-256 stocké
     *
     * @param plainPassword Mot de passe en clair
     * @param storedHash Hash SHA-256 en Base64
     * @return true si correspond
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }

        try {
            String computedHash = hashPassword(plainPassword);
            System.out.println("Computed Hash: " + computedHash);
            System.out.println("Stored Hash:   " + storedHash);
            return MessageDigest.isEqual(
                    Base64.getDecoder().decode(storedHash),
                    Base64.getDecoder().decode(computedHash)
            );

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Indique si un mot de passe est déjà hashé (simple heuristique)
     */
    public static boolean isHashed(String value) {
        try {
            Base64.getDecoder().decode(value);
            return value.length() >= 40; // taille typique d'un SHA-256 en Base64
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Migration d’un mot de passe en clair vers SHA-256
     */
    public static String migratePassword(String plainPassword) {
        if (isHashed(plainPassword)) {
            return plainPassword;
        }
        return hashPassword(plainPassword);
    }
}
