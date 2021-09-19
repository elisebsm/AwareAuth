package com.example.testaware.offlineAuth;

import android.util.Log;
import com.example.testaware.IdentityHandler;
import java.security.PublicKey;
import java.util.ArrayList;

public class InitPeerAuthConn {
    private static String LOG = "Log-Test-Aware-InitPeerAuthConn";

    public static boolean checkPeerAuthCredentials(String receivedString, String signature, PublicKey clientPubKey, String clientEncodedKey, String encodedSignerKey, String peerSignedKey){
        boolean userIsAuthenticated = false;
        PublicKey pubKeySelf = IdentityHandler.getCertificate().getPublicKey();
        ArrayList<PublicKey> verifiedAuthenticatorList;

        //Verify signature of user
        if(VerifyCredentials.verifyString(receivedString, signature, clientPubKey)){
            verifiedAuthenticatorList = VerifyUser.getValidatedAuthenticator();

            PublicKey signerKey = Decoder.getPubKeyGenerated(encodedSignerKey);
            if(VerifyCredentials.verifyCredentials(peerSignedKey, pubKeySelf, clientPubKey)){
                userIsAuthenticated =true;
                Log.d(LOG, "Key signed by me");
            }

            else if(verifiedAuthenticatorList.contains(signerKey)){
                if(checkSignatureOnString(signerKey, peerSignedKey, clientPubKey)){
                    userIsAuthenticated = true;
                    VerifyUser.setAuthenticatedUser(clientEncodedKey);
                    Log.d(LOG, "Signature on string is valid- user is peer auth");
                }
            }else{
                userIsAuthenticated = false;
                Log.d(LOG, "No match for authenticator key. Not starting server.");
            }
        }
        else{
            userIsAuthenticated = false;
            Log.d(LOG, "Signature provided is not valid. No connection ");
        }
        return userIsAuthenticated;
    }

    private static boolean checkSignatureOnString(PublicKey signerKey, String peerSignedKey, PublicKey clientPubKey) {
        boolean match =false;
        if(VerifyCredentials.verifyCredentials(peerSignedKey, signerKey, clientPubKey)){
            match = true;
        }
        return match;
    }
}
