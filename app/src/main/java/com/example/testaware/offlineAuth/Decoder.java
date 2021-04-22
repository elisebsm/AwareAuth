package com.example.testaware.offlineAuth;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Random;

public class Decoder {
    private static PublicKey pubKey;

    private static PublicKey byteArrayToKey(byte[] publicKeyBytes)  {
        PublicKey pubKeyGenerated=null;
        try{
            pubKeyGenerated = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        }
        catch(NoSuchAlgorithmException | InvalidKeySpecException i) {
            System.out.println("An error occurred.");
            i.printStackTrace();
        }
        return pubKeyGenerated;
    }

    public static byte[] decodeString(String encodedString){
        byte[] decodedString = Base64.getDecoder().decode(encodedString);
        return decodedString;
    }

   public static PublicKey getPubKeyGenerated(String encodedKey){
        pubKey= byteArrayToKey(decodeString(encodedKey));
        return pubKey;
   }

    public static String generateRandomString(int len) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk"
                +"lmnopqrstuvwxyz!@#$%&";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }


}
