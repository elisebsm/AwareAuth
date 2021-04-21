package com.example.testaware.offlineAuth;

import android.util.Log;

import com.example.testaware.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


/*Implies that user x has gotten public key of m signed by n, and the certificate of n. Picks up this info by picking up broadcast message from n. Use this to add peer authenticated user to list/file
Only used to add verified public key to list, not to verify user in actual connection.

 */
public class VerifyCredentials {
    private static String LOG = "LOG-Test-Aware-Verify-Credentials";

    //used if user who wants to talk to an peer autehnticated user who provides a singed key
       public static boolean verifyCredentials(String sigPeerKey,PublicKey signerPubKey, PublicKey peerPubKey){

        if (verifySignature(sigPeerKey, signerPubKey, peerPubKey) ) {
            String encodedKey= Base64.getEncoder().encodeToString(peerPubKey.getEncoded());
            addAuthenticatedUserKey(encodedKey);
            return true;
        }
        else{
            return false;
        }
    }

    public static Boolean verifySignature(String sigPeerKey,PublicKey signerPubKey, PublicKey peerPubKey){
           boolean valid = false;
           if(sigPeerKey != null && signerPubKey != null && peerPubKey != null) {
               byte[] decodedSignature = Base64.getDecoder().decode(sigPeerKey);

               try {
                   Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
                   ecdsaSign.initVerify(signerPubKey);

                   byte[] bytes = peerPubKey.toString().getBytes();  //public key of m--> message
                   ecdsaSign.update(bytes);


                   if (ecdsaSign.verify(decodedSignature)) {
                       Log.i(LOG, "valid");
                       valid = true;
                   } else {
                       Log.i(LOG, "invalid");
                       valid = false;
                   }

               } catch (Exception e) {
                   // TODO: handle exception
                   e.printStackTrace();
               }

           }
        return valid;
    }

    //called if signature is valid aka verify ==true. Only sets key to file. Used before client connects. LAST STEP
    public static void addAuthenticatedUserKey(String encodedKeyString){
        if(encodedKeyString != null) {
            try {
                PublicKey clientPubKey = Decoder.getPubKeyGenerated(encodedKeyString);
                if (checkAuthenticatedUserKey(clientPubKey)) {
                    Log.i(LOG, "Key already in file. ");
                } else {
                    BufferedWriter myWriter = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/authenticateUserKeys.txt", true));
                    myWriter.write(encodedKeyString + "\n");
                    myWriter.close();
                    System.out.println("Successfully wrote key to the file.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }

    }
    public static boolean checkAuthenticatedUserKey(PublicKey clientKey){
        boolean isAuth= false;
        String thisLine=null;
        PublicKey keyFromFile =null;
        if(clientKey != null) {
            try {
                File file = new File("/data/data/com.example.testaware/authenticateUserKeys.txt");
                if (file.exists()) {
                    BufferedReader myReader = new BufferedReader(new FileReader("/data/data/com.example.testaware/authenticateUserKeys.txt"));
                    while ((thisLine = myReader.readLine()) != null) {
                        keyFromFile = Decoder.getPubKeyGenerated(thisLine);
                        if (keyFromFile.equals(clientKey)) {
                            isAuth = true;
                        }
                    }
                    myReader.close();
                }

            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
        return isAuth;
    }


    public static boolean verifyString(String message, String signature, PublicKey pubKey) {
        boolean valid = false;
        if( message !=null && signature != null && pubKey != null) {
            byte[] decodedSignature = Base64.getDecoder().decode(signature);
            try {
                Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
                ecdsaSign.initVerify(pubKey);
                byte[] bytes = message.getBytes();  // message
                ecdsaSign.update(bytes);
                if (ecdsaSign.verify(decodedSignature)) {
                    Log.i(LOG, "Signed string is valid!!!!!!!!");
                    valid = true;
                } else {
                    Log.i(LOG, "Signed string isnot valid");
                }

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        return valid;

    }



}
