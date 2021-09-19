package com.example.testaware.offlineAuth;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public class PeerSigner {

    private static String LOG = "Log-Test-Aware-Peer-Signer";


    public static String signPeerKey(PublicKey peerKey, KeyPair signerKeyPair) {
        String signedKey = null;
        if(peerKey != null && signerKeyPair != null) {

            try {
                PrivateKey privKey = signerKeyPair.getPrivate();
                Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
                ecdsaSign.initSign(privKey);
                byte[] bytes = peerKey.toString().getBytes();
                ecdsaSign.update(bytes);
                byte[] signature = ecdsaSign.sign();

                signedKey = Base64.getEncoder().encodeToString(signature);
                Log.i(LOG, "Signing Peer Key");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return signedKey;
    }


    public static String signString(String stringToSign, KeyPair keyPair){
        String signedString = null;
        if(stringToSign != null && keyPair != null) {
            byte[] signature;
            try {
                PrivateKey privKey = keyPair.getPrivate();

                Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
                ecdsaSign.initSign(privKey);
                byte[] bytes = stringToSign.getBytes();
                ecdsaSign.update(bytes);
                signature = ecdsaSign.sign();

                signedString = Base64.getEncoder().encodeToString(signature);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return signedString;
    }


    public static void saveSignedKeyToFile(String sigKey){
        if(sigKey != null) {
            try {
                if (!getSavedSignedKeysFromFile().contains(sigKey)) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/signedKeys.txt", true));
                    writer.write(sigKey);

                    writer.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static ArrayList<String> getSavedSignedKeysFromFile(){
        ArrayList<String> signedKeys=new ArrayList<>();
        String line;
        try{
            File file = new File("/data/data/com.example.testaware/signedKeys.txt");
            if(file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader("/data/data/com.example.testaware/signedKeys.txt"));
                while ((line = reader.readLine()) != null) {
                    signedKeys.add(line);
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return signedKeys;
    }

    public static HashMap<String, String> getSignedKeySelf(){
        String line = null;
        HashMap<String, String> signedKeysAndAuthKey=new HashMap<>();
        try{
            File file = new File("/data/data/com.example.testaware/signedKeySelf.txt");
            if(file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader("/data/data/com.example.testaware/signedKeySelf.txt"));
                while ((line = reader.readLine()) != null) {
                    String[] newSplitString = line.split("split");
                    String authenticatorKey = newSplitString[0];
                    String signedKey = newSplitString[1];
                    signedKeysAndAuthKey.put(authenticatorKey,signedKey);
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return signedKeysAndAuthKey;
    }


    public static void setSignedKeySelf(String sigKey, String authenticatorKey){
       if(sigKey != null && authenticatorKey!= null) {
           try {
               BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/signedKeySelf.txt", true));
               writer.write(authenticatorKey + "split" + sigKey  + "\n");
               writer.close();
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
    }


    public static void setTmpPeerAuthInfo(String encodedTmpInfo){
        if(encodedTmpInfo != null) {
            try {
                if (!getSavedSignedKeysFromFile().contains(encodedTmpInfo)) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/tmpSavedPeerAuthInfo.txt", true));
                    writer.write(encodedTmpInfo+ "\n");

                    writer.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteTmpFile()  {
        File file = new File("/data/data/com.example.testaware/tmpSavedPeerAuthInfo.txt");
        if (file.exists()){
            file.delete();
        }
    }

}
