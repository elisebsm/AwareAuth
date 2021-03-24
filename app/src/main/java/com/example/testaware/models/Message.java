package com.example.testaware.models;

import com.example.testaware.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Collection;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.Getter;

//import lombok.Getter;

public class Message implements Serializable {
    private byte[] ciphertext;
    private String plaintext;

    @Getter
    private byte[] signature;

    @Getter
    private PublicKey to;
    @Getter
    private PublicKey from;

    public Message(PublicKey to, PublicKey from, String message, PrivateKey privateKey) {
        this.to = to;
        this.from = from;

        Cipher cipher = null;
        try {
            /*cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM_AES);
            cipher.init(Cipher.ENCRYPT_MODE, to);
            this.ciphertext = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8)); //TODO: Blir dette riktig?*/

            byte[] key = new byte[16];
            (new SecureRandom()).nextBytes(key);

            cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
            this.ciphertext = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

            // Signature
            ByteArrayOutputStream signatureStream = new ByteArrayOutputStream();
            signatureStream.write(to.getEncoded());
            signatureStream.write(from.getEncoded());
            signatureStream.write(this.ciphertext);

            Signature signature = Signature.getInstance(Constants.SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(signatureStream.toByteArray());
            this.signature = signature.sign();
            this.plaintext = message;

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException | SignatureException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }


    public String getPlaintext(PrivateKey privateKey) {
        if(plaintext != null){
            return plaintext;
        }
        Cipher cipher = null;
        try {
            /*cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM_AES);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);*/
            cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM_AES);
            byte[] key = new byte[16];
            (new SecureRandom()).nextBytes(key);

            byte[] iv = cipher.getIV();
            GCMParameterSpec gcmspec = cipher.getParameters().getParameterSpec(GCMParameterSpec.class);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));

            return new String(cipher.doFinal(this.ciphertext), StandardCharsets.UTF_8);
        } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException | InvalidParameterSpecException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return "Decryption failed.";
    }


    public boolean verify() {
        ByteArrayOutputStream signatureStream = new ByteArrayOutputStream();
        try {
            signatureStream.write(to.getEncoded());

            signatureStream.write(from.getEncoded());
            signatureStream.write(this.ciphertext);

            Signature signature = Signature.getInstance(Constants.SIGNATURE_ALGORITHM);
            signature.initVerify(from);
            signature.update(signatureStream.toByteArray());
            return signature.verify(this.signature);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            return false;
        }
    }

    public void testEncrypt(String inputPlain) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidParameterSpecException, BadPaddingException, IllegalBlockSizeException {
        byte[] key = new byte[16];
        (new SecureRandom()).nextBytes(key);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));

        byte[] ciphertext = cipher.doFinal(inputPlain.getBytes());
        byte[] iv = cipher.getIV();
        GCMParameterSpec gcmspec = cipher.getParameters().getParameterSpec(GCMParameterSpec.class);
        System.out.println("ciphertext: " + ciphertext.length + ", IV: " + iv.length + ", tLen: " + gcmspec.getTLen());

    }

    public void testDecrypt(String inputCipher) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        byte[] key = new byte[16];
        (new SecureRandom()).nextBytes(key);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        byte[] iv = cipher.getIV();
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
        byte[] plaintext = cipher.doFinal(ciphertext);

        System.out.println("plaintext : " + new String(plaintext));
    }

}

/*    public Message(String message) throws NoSuchAlgorithmException {
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

    private String getPlaintext(){
        return plaintext;
    }*/

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