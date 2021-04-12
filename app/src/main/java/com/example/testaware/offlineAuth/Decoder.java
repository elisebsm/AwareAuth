package com.example.testaware.offlineAuth;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Decoder {
    private static PublicKey pubKey;

    //Server uses returned key to validate signature
    private static PublicKey byteArrayToKey(byte[] publicKeyBytes)  {
        PublicKey pubKeyGenerated=null;
        try{
            //generate public key object from bytes received
            pubKeyGenerated = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        }
        catch(NoSuchAlgorithmException | InvalidKeySpecException i) {
            System.out.println("An error occurred.");
            i.printStackTrace();
        }
        return pubKeyGenerated;
    }

    //use to decode encoded key and signature
    public static byte[] decodeString(String encodedString){
        byte[] decodedString = Base64.getDecoder().decode(encodedString);
        return decodedString;
    }

   public static PublicKey getPubKeyGenerated(String encodedKey){
        pubKey= byteArrayToKey(decodeString(encodedKey));
        return pubKey;
   }



}
