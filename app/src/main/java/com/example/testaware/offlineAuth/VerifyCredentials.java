package com.example.testaware.offlineAuth;

import android.util.Log;

import com.example.testaware.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;


/*Implies that user x has gotten public key of m signed by n, and the certificate of n. Picks up this info by picking up broadcast message from n. Use this to add peer authenticated user to list/file
Only used to add verified public key to list, not to verify user in actual connection.

 */
public class VerifyCredentials {
    private static String LOG = "VerifyPeer";

    public static Boolean verifyCredentials(String sigPeerKey, Certificate signerCert, Certificate peerCert){

        if (verifySignature(sigPeerKey, signerCert, peerCert)&& verifySigner(signerCert)) {
            addAuthenticatedUserKey(peerCert);
            return true;
        }
        else{
            return false;
        }
    }

    public static Boolean verifySignature(String sigPeerKey, Certificate signerCert, Certificate peerCert){
        PublicKey signerPubKey = signerCert.getPublicKey();   //public key of n
        Boolean valid=false;
        try{
            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
            ecdsaSign.initVerify(signerPubKey);       //TODO: put somewhere else for verifying, just for testing
            PublicKey peerPubKey = peerCert.getPublicKey();
            byte[] bytes = peerPubKey.toString().getBytes();  //public key of m--> message
            ecdsaSign.update(bytes);
            byte [] signedPeerKeyBytes = sigPeerKey.getBytes();

            if (ecdsaSign.verify(signedPeerKeyBytes)) {
                Log.i(LOG, "valid");
                valid=true;
                //TODO: call authetnicatedUser here
            } else {
                Log.i(LOG, "invalid");
                valid = false;
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return valid;

    }
    // Used to check key provided and IP of connected client
    public static void checkAuthenticatedUser(PublicKey connectedPeerKey, String connectedPeerIP){

    }

    public static void setAuthenticatedUser(PublicKey connectedPeerKey, String connectedPeerIP) {
        try{
            BufferedWriter myWriter = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/authenticatedUsers.txt"));
            myWriter.write(connectedPeerKey.toString());
            myWriter.close();
            System.out.println("Successfully wrote key to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }


    //called if signature is valid aka verify ==true. Only sets key to file. Used before client connects
    public static void addAuthenticatedUserKey(Certificate peerCert){
        //PublicKey peerPubKey = peerCertificate.getPublicKey();
        try {
            BufferedWriter myWriter = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/authenticateUserKeys.txt") );
            PublicKey key = peerCert.getPublicKey();
            myWriter.write(key.toString());
            myWriter.close();
            System.out.println("Successfully wrote key to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    //verify signer cert against root ca
    public static Boolean verifySigner(Certificate signerCert){//use verify signer cert

            return true;
    }





}
