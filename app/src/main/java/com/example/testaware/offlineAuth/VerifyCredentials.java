package com.example.testaware.offlineAuth;

import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;


/*Implies that user x has gotten public key of m signed by n, and the certificate of n. Picks up this info by picking up broadcast message from n

 */
public class VerifyCredentials {
    private static String LOG = "VerifyPeer";



    public static Boolean verifySignature(String signedPeerKey,Certificate signerCert, Certificate peerCertificate){
        PublicKey signerPubKey = signerCert.getPublicKey();   //public key of n
        Boolean valid=false;
        try{
            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
            ecdsaSign.initVerify(signerPubKey);       //TODO: put somewhere else for verifying, just for testing
            PublicKey peerPubKey = peerCertificate.getPublicKey();
            byte[] bytes = peerPubKey.toString().getBytes();  //public key of m--> message
            ecdsaSign.update(bytes);
            byte [] signedPeerKeyBytes = signedPeerKey.getBytes();

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

    //called if signature is valid aka verify ==true
    public static void addAuthenticatedUser(Certificate peerCertificate){
        //PublicKey peerPubKey = peerCertificate.getPublicKey();
        try {
            FileWriter myWriter = new FileWriter("/data/data/com.example.testaware/authenticateUserCertificate.txt");
            myWriter.write(peerCertificate.toString());
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    //TODO: change location. Should not be called here -> before second ssl connection
    public static void generateChallenge(){
        
    }
    //verify signer cert against root ca
    public static void verifySigner(Certificate signerCert){

    }





}
