package com.example.testaware.offlineAuth;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/*Methods used to verify user that server wants to connect to */

public class VerifyUser {


    private HashMap keyToIPMapping;

    // Used to check key provided and IP of connected client
    public HashMap getAuthenticatedUsers(){
        try{
            BufferedWriter myWriter = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/authenticatedUsers.txt"));
            myWriter.readLine();
            myWriter.close();
            System.out.println("Successfully wrote key to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return keyToIPMapping;

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


    public void generateChallenge(){

    }

}
