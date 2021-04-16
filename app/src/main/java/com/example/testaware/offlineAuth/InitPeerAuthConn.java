package com.example.testaware.offlineAuth;

import android.util.Log;

import java.security.PublicKey;

public class InitPeerAuthConn {
    private static boolean userIsAuthenticated;
    private static String LOG = "LOG-Test-Aware-InitPeerAuthConn";

//TODO: remember to verify signer also - check root cert against cert of signer- or not
    public static boolean checkPeerAuthCredentials(String receivedString, String signature, PublicKey clientPubKey, String clientEncodedKey, String peerIP){
        userIsAuthenticated=false;

        if(VerifyCredentials.verifyString(receivedString, signature, clientPubKey)){

            Log.d(LOG, "Signature provided is correct");
            if(VerifyUser.isAuthenticatedUser(peerIP, clientPubKey)){
                userIsAuthenticated=true;
                Log.d(LOG, "Match found for key and IP. User is peer authenticated");
            }
            else if(VerifyCredentials.checkAuthenticatedUserKey(clientPubKey)){
                VerifyUser.setAuthenticatedUser(peerIP,clientEncodedKey);
                userIsAuthenticated=true;
                Log.d(LOG, "Peer key has been peer authenticated by other user. User is authenticated");
            }
            else{
                userIsAuthenticated=false;
                Log.d(LOG, "User not peer authenticated");
            }
        }
        else{
            userIsAuthenticated=false;
            Log.d(LOG, "Signature provided is not valid. No connection initialized");
        }

        return userIsAuthenticated;
    }

}
