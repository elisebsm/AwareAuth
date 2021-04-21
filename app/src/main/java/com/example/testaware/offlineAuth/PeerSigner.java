package com.example.testaware.offlineAuth;

import android.util.Log;

import com.example.testaware.IdentityHandler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;

/*
This method is supposed to be called before the ssl connection is established with another peer, because user is not yet authenticated. Peer authenticates another peer. Web of trust. Sign public key
of user who wants to get authenticated.
*/

public class PeerSigner {

    private static String LOG = "LOG-Test-Aware-Peer-Signer";


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
                //  boolean valid = VerifyCredentials.verifyString(stringToSign,signedString,pubKey);
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
        ArrayList<String> signedKeys=new ArrayList();
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

    public static String getSignedKeySelf(){  //this is encoded base 64
        String line = null;
        String signedKeysSelf=null;
        try{
            File file = new File("/data/data/com.example.testaware/signedKeySelf.txt");
            if(file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader("/data/data/com.example.testaware/signedKeySelf.txt"));
                while ((line = reader.readLine()) != null) {
                    signedKeysSelf = line;

                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return signedKeysSelf;
    }

    public static void setSignedKeySelf(String sigKey){  //this is encoded base 64
       if(sigKey != null) {
           try {
               if (getSignedKeySelf() == null) {  //only peer authneticated once
                   BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/signedKeySelf.txt", true));
                   writer.write(sigKey);
                   Log.i(LOG, "Setting signed keys");

                   writer.close();
               }

           } catch (Exception e) {
               e.printStackTrace();
           }
       }
    }


    public static void savetmpSignedKeyToFile(String sigKey){
        if(sigKey != null) {
            try {
                if (!getSavedSignedKeysFromFile().contains(sigKey)) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/tmpSignedKeys.txt", true));
                    writer.write(sigKey);

                    writer.close();
                    Log.i(LOG, "wrote key to file");
                } else {
                    Log.i(LOG, "No keys to save to file");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<String> getSavedtmpSignedKeysFromFile(){
        ArrayList<String> signedKeysTMP=new ArrayList();
        String line=null;
        try{
            File file = new File("/data/data/com.example.testaware/tmpSignedKeys.txt");
            if (file.exists()) {


                BufferedReader reader = new BufferedReader(new FileReader("/data/data/com.example.testaware/tmpSignedKeys.txt"));

                while ((line = reader.readLine()) != null) {
                    signedKeysTMP.add(line);
                }
                file.delete();
            }
            
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return signedKeysTMP;
    }
}
