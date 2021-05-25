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
                PublicKey pubKey = signerKeyPair.getPublic();

                Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
                ecdsaSign.initSign(privKey);
                byte[] bytes = peerKey.toString().getBytes();
                ecdsaSign.update(bytes);
                byte[] signature = ecdsaSign.sign();

                String pub = Base64.getEncoder().encodeToString(pubKey.getEncoded());
                signedKey = Base64.getEncoder().encodeToString(signature);

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        return signedKey;

    }

    public static String signString(String stringToSign, KeyPair keyPair){
        String signedString = null;
        if(stringToSign != null && keyPair != null) {
            byte[] signature = null;
            try {
                PrivateKey privKey = keyPair.getPrivate();
                PublicKey pubKey = keyPair.getPublic();

                Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
                ecdsaSign.initSign(privKey);
                byte[] bytes = stringToSign.getBytes();
                ecdsaSign.update(bytes);
                signature = ecdsaSign.sign();

                String pub = Base64.getEncoder().encodeToString(pubKey.getEncoded());
                signedString = Base64.getEncoder().encodeToString(signature);

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        return signedString;
    }


    public static void saveSignedKeyToFile(String sigKey){
        if(sigKey != null) {
            try {
                if (!getSavedSignedKeysFromFile().contains(sigKey)) {  //dont save if it user is peer auth
                    BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/signedKeys.txt", true));
                    writer.write(sigKey);
                    Log.i(LOG, "write signed key to file");
                    writer.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<String> getSavedSignedKeysFromFile(){
        ArrayList<String> signedKeys=new ArrayList<>();
        String line = null;
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
               Log.i(LOG, "Setting signed key self and authenticator");
               writer.close();
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
    }


    public static void setTmpPeerAuthInfo(String encodedTmpInfo){
        //if (encodedTmpInfo.contains("sigKeyList"))
       // encoded= messageIn.replace("sigKeyList", "");
        //String trustedAuthenticatorKey = messageIn.replace("authUser", "");
        if(encodedTmpInfo != null) {
            try {
                if (!getSavedSignedKeysFromFile().contains(encodedTmpInfo)) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/tmpSavedPeerAuthInfo.txt", true));
                    writer.write(encodedTmpInfo+ "\n");

                    writer.close();
                    Log.i(LOG, "wrote key to file");
                } else {
                    Log.i(LOG, "No keys to save to file. Already in file");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<String> getTmpPeerAuthInfo(boolean reqSignedKeyList){
        ArrayList<String> peerAuthInfoList =new ArrayList<>();
        String line=null;
        try{
            File file = new File("/data/data/com.example.testaware/tmpSavedPeerAuthInfo.txt");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader("/data/data/com.example.testaware/tmpSavedPeerAuthInfo.txt"));
                while ((line = reader.readLine()) != null) {
                    if(reqSignedKeyList==true) {
                        if (line.contains("sigKeyList")) {
                            String signedKey = line.replace("sigKeyList", "");
                            peerAuthInfoList.add(signedKey);
                        }
                    }
                    else {
                        if (line.contains("authUser")) {
                            String trustedAuthenticator = line.replace("authUser", "");
                            peerAuthInfoList.add(trustedAuthenticator);
                        }
                    }


                }
                file.delete();
            }
            
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return peerAuthInfoList;
    }

    public static void deleteTmpFile()  {
        File file = new File("/data/data/com.example.testaware/tmpSavedPeerAuthInfo.txt");
        if (file.exists()){
            file.delete();
        }

    }

}
