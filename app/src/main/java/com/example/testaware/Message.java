package com.example.testaware;

import com.example.testaware.Constants;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

//import lombok.Getter;

public class Message {
    private byte[] ciphertext;
    private String plaintext;
    private SecretKey key;
    private byte[] IV;
   // @Getter
    private PublicKey to;
   // @Getter
    private PublicKey from;

    public Message() throws NoSuchAlgorithmException {
        generateIV();
        generateSecretKey();
    }

    public void generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = null;
        Security.getProviders();
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            key = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public byte[] encryptMessage(String messageText) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        byte[] plaintext = messageText.getBytes();
        Cipher cipher = null;
        cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM_AES);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        //SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        ciphertext = cipher.doFinal(plaintext);
        return ciphertext;
    }

    public String decryptMessage(byte[] encryptedMessage) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException {
        Cipher cipher = null;
        cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM_AES);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        plaintext = new String(cipher.doFinal(encryptedMessage), "UTF-8");
        return plaintext;
    }

    public void generateIV() {
        IV = new byte[16];
        SecureRandom random;
        random = new SecureRandom();
        random.nextBytes(IV);
    }

}



/* public void encryptMessage2(String messageText){
        byte[] plaintext = messageText.getBytes();
        KeyGenerator keyGenerator = null;
        Security.getProviders();
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            key = keyGenerator.generateKey();
            Cipher cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM_AES);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            ciphertext = cipher.doFinal(plaintext);

        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }*/