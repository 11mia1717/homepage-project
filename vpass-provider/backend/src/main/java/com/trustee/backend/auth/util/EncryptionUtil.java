package com.trustee.backend.auth.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyGenerator; // KeyGenerator import 추가
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128; // Must be one of {128, 120, 112, 104, 96}
    private static final int IV_LENGTH_BYTE = 12;
    
    // AES-256 비트 키를 동적으로 생성합니다. (프로덕션 환경에서는 KMS/Env에서 로드해야 합니다.)
    private static final SecretKey SECRET_KEY;

    static {
        try {
            // 256비트 (32바이트) AES 키를 동적으로 생성
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256, new SecureRandom());
            SECRET_KEY = keyGen.generateKey();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing AES key", e);
        }
    }

    public static String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
            SecretKey secretKey = SECRET_KEY; // 동적으로 생성된 키 사용
            byte[] iv = new byte[IV_LENGTH_BYTE];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // Return IV + CipherText encoded in Base64
            byte[] encrypted = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption Error", e);
        }
    }

    public static String decrypt(String encryptedText) {
        if (encryptedText == null) return null;
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            
            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(decoded, 0, iv, 0, iv.length);

            byte[] cipherText = new byte[decoded.length - iv.length];
            System.arraycopy(decoded, iv.length, cipherText, 0, cipherText.length);

            SecretKey secretKey = SECRET_KEY; // 동적으로 생성된 키 사용

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption Error", e);
        }
    }
}
