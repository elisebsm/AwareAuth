package com.example.testaware.offlineAuth;


import android.util.Log;
import com.github.cliftonlabs.json_simple.Jsoner;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import java.nio.file.Files;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;



/*Methods used to verify user that server wants to connect to - mappings of pub key and mac/Ip- after done challenge response*/

public class VerifyUser {

    private static String LOG = "LOG-Test-Aware-Verify-User";

    public static void setAuthenticatedUser(String connectedPeerIP, String encodedPeerKey) {

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/AuthenticatedUsers.txt", true));
            writer.write(connectedPeerIP+ "split"+ encodedPeerKey+"\n");
            writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static boolean isAuthenticatedUser(String peerIP, PublicKey connectedPeerKey) {   //todo: never returns true
        boolean isAuthenticated = false;
        PublicKey keyDecodedFromFile = null;
        String thisLine = null;
        try{
            BufferedReader myReader = new BufferedReader(new FileReader("/data/data/com.example.testaware/AuthenticatedUsers.txt" ));
            while ((thisLine = myReader.readLine()) != null){
                String[] newSplitString= thisLine.split("split");
                String ip = newSplitString[0];
                String encodedKeyFromFile = newSplitString[1];
                keyDecodedFromFile = Decoder.getPubKeyGenerated(encodedKeyFromFile);
                if(ip.equals(peerIP) && keyDecodedFromFile.equals(connectedPeerKey)){
                    isAuthenticated = true;
                }

            }



    } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isAuthenticated;

    }
}
