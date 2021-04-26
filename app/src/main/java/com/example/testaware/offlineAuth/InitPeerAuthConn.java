package com.example.testaware.offlineAuth;

import android.util.Log;
import com.example.testaware.IdentityHandler;
import java.security.PublicKey;
import java.util.ArrayList;

public class InitPeerAuthConn {
    private static boolean userIsAuthenticated;
    private static String LOG = "LOG-Test-Aware-InitPeerAuthConn";
    private static ArrayList<PublicKey> verifiedAuthenticatorList;
    private static PublicKey pubKeySelf;

//TODO: remember to verify signer also - check root cert against cert of signer- or not
    public static boolean checkPeerAuthCredentials(String receivedString, String signature, PublicKey clientPubKey, String clientEncodedKey, String peerIP, String peerSignedKey){
        userIsAuthenticated=false;
        pubKeySelf = IdentityHandler.getCertificate().getPublicKey();

        ArrayList<String> signedKeyList= PeerSigner.getSavedSignedKeysFromFile();
        verifiedAuthenticatorList = new ArrayList<>();

        if(VerifyCredentials.verifyString(receivedString, signature, clientPubKey)){
            verifiedAuthenticatorList = VerifyUser.getValidatedAuthenticator();

            Log.d(LOG, "Signature provided is correct");
            if(VerifyUser.isAuthenticatedUser(clientPubKey)){
                userIsAuthenticated=true;
                Log.d(LOG, "Match found for key and IP. User is peer authenticated");
            }

            else if(signedKeyList.contains(peerSignedKey)){
                  VerifyUser.setAuthenticatedUser(peerIP,clientEncodedKey);
                  userIsAuthenticated=true;
                  Log.d(LOG, "Signed key provided is in file of signed keys ");
            }
            else if (checkAuthOnString(verifiedAuthenticatorList,peerSignedKey,clientPubKey)){
                VerifyUser.setAuthenticatedUser(peerIP,clientEncodedKey);
                userIsAuthenticated=true;
                Log.d(LOG, "Signature on key verified against authenticators");
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


    private static boolean checkAuthOnString(ArrayList<PublicKey> signerPublicKeys, String peerSignedKey, PublicKey clientPubKey) {
        boolean match =false;
        if(VerifyCredentials.verifyCredentials(peerSignedKey, pubKeySelf, clientPubKey)){
            match = true;
        }
        else {
            if (signerPublicKeys != null) {
                for (int i = 0; i < signerPublicKeys.size(); i++) {
                    if (VerifyCredentials.verifyCredentials(peerSignedKey, signerPublicKeys.get(i), clientPubKey)) {
                        match = true;
                    }
                }
            }
        }
        return match;
    }



}
