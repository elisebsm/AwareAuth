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
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;


/*Implies that user x has gotten public key of m signed by n, and the certificate of n. Picks up this info by picking up broadcast message from n. Use this to add peer authenticated user to list/file
Only used to add verified public key to list, not to verify user in actual connection.

 */
public class VerifyCredentials {
    private static String LOG = "VerifyPeer";

    public static boolean verifyCredentials(String sigPeerKey, Certificate signerCert, PublicKey peerPubKey){

        if (verifySignature(sigPeerKey, signerCert, peerPubKey) && verifySigner(signerCert)) {
            addAuthenticatedUserKey(peerPubKey);
            return true;
        }
        else{
            return false;
        }
    }

    public static Boolean verifySignature(String sigPeerKey, Certificate signerCert, PublicKey peerPubKey){
        PublicKey signerPubKey = signerCert.getPublicKey();   //public key of n
        Boolean valid=false;
        try{
            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
            ecdsaSign.initVerify(signerPubKey);       //TODO: put somewhere else for verifying, just for testing

            byte[] bytes = peerPubKey.toString().getBytes();  //public key of m--> message
            ecdsaSign.update(bytes);
            byte [] signedPeerKeyBytes = sigPeerKey.getBytes();

            if (ecdsaSign.verify(signedPeerKeyBytes)) {
                Log.i(LOG, "valid");
                valid=true;
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



    //called if signature is valid aka verify ==true. Only sets key to file. Used before client connects. LAST STEP
    public static void addAuthenticatedUserKey(PublicKey key){
        //PublicKey peerPubKey = peerCertificate.getPublicKey();
        try {
            BufferedWriter myWriter = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/authenticateUserKeys.txt",true) );
            myWriter.write(key.toString());
            myWriter.close();
            System.out.println("Successfully wrote key to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }
    public static boolean checkAuthenticatedUserKey(PublicKey key){
        //PublicKey peerPubKey = peerCertificate.getPublicKey();
        boolean isAuth= false;
        String thisLine=null;
        try {
            BufferedReader myReader = new BufferedReader(new FileReader("/data/data/com.example.testaware/authenticateUserKeys.txt" ));
            while ((thisLine = myReader.readLine()) != null) {
                if(thisLine==key.toString()){
                    isAuth=true;
                }
            }
            myReader.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return isAuth;
    }


    //verify signer cert against root ca
    public static Boolean verifySigner(Certificate signerCert){//use verify signer cert  //TODO: implement this

            return true;
    }





}
