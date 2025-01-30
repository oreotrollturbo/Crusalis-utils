package io.github.pingisfun.hitboxplus.util;

import io.github.pingisfun.hitboxplus.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;

import java.util.Base64;

public class Encryption {

    public static String encryptNumber(int number) {

        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        String key = config.coordSharing.encryptionKey;

        // Handle negative numbers by storing the sign explicitly
        String numberStr = Integer.toString(number); // Convert number to string
        if (number < 0) {
            numberStr = "-" + numberStr.substring(1); // Keep negative sign for encryption
        }

        StringBuilder encryptedStr = new StringBuilder();

        // Repeat or truncate the key to match the length of the number string
        for (int i = 0; i < numberStr.length(); i++) {
            char numChar = numberStr.charAt(i);
            char keyChar = key.charAt(i % key.length()); // Wrap the key if shorter than the number
            // XOR the number character and key character
            char encryptedChar = (char) (numChar ^ keyChar);
            encryptedStr.append(encryptedChar);
        }

        // Convert the result to a Base64 string to ensure it's printable
        return Base64.getEncoder().encodeToString(encryptedStr.toString().getBytes());
    }

    // Decrypt the number using the same key string and handle negative numbers
    public static Integer decryptNumber(String encryptedNumber) {

        try {
            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
            String key = config.coordSharing.encryptionKey;

            // Decode the encrypted number from Base64
            String decryptedStr = new String(Base64.getDecoder().decode(encryptedNumber));

            StringBuilder originalStr = new StringBuilder();

            // Decrypt each character using the key
            for (int i = 0; i < decryptedStr.length(); i++) {
                char encryptedChar = decryptedStr.charAt(i);
                char keyChar = key.charAt(i % key.length()); // Repeat the key if necessary
                // XOR the encrypted character with the key character to get the original number character
                char decryptedChar = (char) (encryptedChar ^ keyChar);
                originalStr.append(decryptedChar);
            }

            // Return the decrypted number as an integer
            return Integer.parseInt(originalStr.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
