package com.example.testaware.offlineAuth;

import java.security.PublicKey;

//initialized in main by client(peer authenticated user) pressing button and sending msg to server
public class InitPeerAuthConn {
    private static PublicKey peerPubKey;
    private static String peerIP;


    public void InitPeerAuthConn(PublicKey peerPubKey, String peerIP){
        this.peerPubKey= peerPubKey;
        this.peerIP=peerIP;
    }
/*
    public static boolean setupPeerAuthConn(){
        boolean isAuthenticated=false;
        if (VerifyCredentials.checkAuthenticatedUserKey(peerPubKey)){     //check if peer has authenticated this user
            //Check if user has already ben authenticated - and know that this Ip corresponds to pub key (user holds private key)
            //if(VerifyUser.isAuthenticatedUser(peerIP)){
                 isAuthenticated=true;//start server
            }
        }
        else{
            //get credentials from peer  //TODO: GET THIS IMPLEMENTED IN MAIN
        }
       // return isAuthenticated;  //if this is true, setup conn
    }
*/
}
