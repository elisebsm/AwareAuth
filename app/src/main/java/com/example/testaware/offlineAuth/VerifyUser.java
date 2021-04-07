package com.example.testaware.offlineAuth;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;
import java.util.HashMap;


/*Methods used to verify user that server wants to connect to - mappings of pub key and mac/Ip- after done challenge response*/

public class VerifyUser {


    // Used to check key provided and IP of connected client
    public static boolean isAuthenticatedUser(String peerIP){
        String thisLine =null;
        AuthenticatedUser user=null;
        boolean cont= true;
        boolean isAuthenticated=false;
        while (cont) {
            try {
                FileInputStream fileIn = new FileInputStream("/data/data/com.example.testaware/authenticatedUsers.txt");
                ObjectInputStream in = new ObjectInputStream(fileIn);

                user = (AuthenticatedUser) in.readObject();
                if(user!=null){
                     if (user.ipAddress == peerIP) {  
                         isAuthenticated = true;
                         in.close();
                         fileIn.close();
                         System.out.println("Successfully read key and ip to the file.");
                         cont= false;
                    }
                } else {
                    in.close();
                    fileIn.close();
                    System.out.println("User not authenticated");
                    cont = false;
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
        return isAuthenticated;
    }

//used to set map key and IP of new users , after challenge response         //TODO: call this somewhere
    public static void setAuthenticatedUser(PublicKey connectedPeerKey, String connectedPeerIP) {
        AuthenticatedUser user= new AuthenticatedUser();
        user.pubKey = connectedPeerKey;
        user.ipAddress = connectedPeerIP;
        try{
            FileOutputStream fileOut =
                    new FileOutputStream("/data/data/com.example.testaware/authenticatedUsers.txt");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(user);
            out.close();
            fileOut.close();
            System.out.println("Successfully wrote key and ipaddr to the file.");
        } catch (IOException i) {
            System.out.println("An error occurred.");
            i.printStackTrace();
        }

    }
    public void generateChallenge(){                   //TODO: IMPLEMENT THIS

        

    }

}
