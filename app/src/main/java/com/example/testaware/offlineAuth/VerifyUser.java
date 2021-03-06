package com.example.testaware.offlineAuth;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;

public class VerifyUser {

    private static String LOG = "Log-Test-Aware-Verify-User";

    public static void setAuthenticatedUser(String encodedPeerKey) {

        try {
            PublicKey clientPubKey = Decoder.getPubKeyGenerated(encodedPeerKey);
            if(isAuthenticatedUser(clientPubKey)){
                Log.i(LOG, "User Already authenticated and in file");
            }
            else {
                Log.i(LOG, "Saving key to file");
                BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/AuthenticatedUsers.txt", true));
                writer.write(encodedPeerKey + "\n");
                writer.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static boolean isAuthenticatedUser(PublicKey connectedPeerKey) {
        boolean isAuthenticated = false;
        PublicKey keyDecodedFromFile = null;
        String thisLine = null;
        try{
            File file = new File("/data/data/com.example.testaware/AuthenticatedUsers.txt");
            if(file.exists()) {
                BufferedReader myReader = new BufferedReader(new FileReader("/data/data/com.example.testaware/AuthenticatedUsers.txt"));
                while ((thisLine = myReader.readLine()) != null) {
                    String encodedKeyFromFile = thisLine;
                    keyDecodedFromFile = Decoder.getPubKeyGenerated(encodedKeyFromFile);
                    if ( keyDecodedFromFile.equals(connectedPeerKey)) {
                        isAuthenticated = true;
                    }

                }
            }

    } catch (IOException e) {
            e.printStackTrace();
        }
        return isAuthenticated;
    }


    public static void setValidatedAuthenticator(PublicKey peerKey) {
        String encodedPeerKey= Base64.getEncoder().encodeToString(peerKey.getEncoded());
        ArrayList<PublicKey> authenticatorList= VerifyUser.getValidatedAuthenticator();
        try {
            if(authenticatorList != null) {
                if (authenticatorList.contains(peerKey)) {
                    //do nothing
                }
                else{
                    BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/validatedAuthenticators.txt", true));
                    writer.write(encodedPeerKey + "\n");
                    writer.close();
                }
            }
            else {
                BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/validatedAuthenticators.txt", true));
                writer.write(encodedPeerKey + "\n");
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<PublicKey> getValidatedAuthenticator() {
        ArrayList<PublicKey> authenticatorList = new ArrayList<>();
        PublicKey keyDecodedFromFile ;
        String thisLine;
        try {
            File file = new File("/data/data/com.example.testaware/validatedAuthenticators.txt");
            if (file.exists()) {
                BufferedReader myReader = new BufferedReader(new FileReader("/data/data/com.example.testaware/validatedAuthenticators.txt"));
                while ((thisLine = myReader.readLine()) != null) {
                    String encodedKeyFromFile = thisLine;
                    keyDecodedFromFile = Decoder.getPubKeyGenerated(encodedKeyFromFile);
                    authenticatorList.add(keyDecodedFromFile);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return authenticatorList;
    }


}
