package com.example.testaware.offlineAuth;

import android.util.Log;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class VerifyCredentials {
    private static String LOG = "LOG-Test-Aware-Verify-Credentials";


       public static boolean verifyCredentials(String sigPeerKey,PublicKey signerPubKey, PublicKey peerPubKey){

        if (verifySignature(sigPeerKey, signerPubKey, peerPubKey) ) {
            String encodedKey= Base64.getEncoder().encodeToString(peerPubKey.getEncoded());
            return true;
        }
        else{
            return false;
        }
    }

    public static Boolean verifySignature(String sigPeerKey,PublicKey signerPubKey, PublicKey peerPubKey){
           boolean valid = false;
           if(sigPeerKey != null && signerPubKey != null && peerPubKey != null) {
               byte[] decodedSignature = Base64.getDecoder().decode(sigPeerKey);

               try {
                   Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
                   ecdsaSign.initVerify(signerPubKey);

                   byte[] bytes = peerPubKey.toString().getBytes();  //public key of m--> message
                   ecdsaSign.update(bytes);


                   if (ecdsaSign.verify(decodedSignature)) {
                       Log.i(LOG, "valid");
                       valid = true;
                   } else {
                       Log.i(LOG, "invalid");
                       valid = false;
                   }

               } catch (Exception e) {
                   // TODO: handle exception
                   e.printStackTrace();
               }

           }
        return valid;
    }


    public static boolean verifyString(String message, String signature, PublicKey pubKey) {
        boolean valid = false;
        if( message !=null && signature != null && pubKey != null) {
            byte[] decodedSignature = Base64.getDecoder().decode(signature);
            try {
                Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
                ecdsaSign.initVerify(pubKey);
                byte[] bytes = message.getBytes();  // message
                ecdsaSign.update(bytes);
                if (ecdsaSign.verify(decodedSignature)) {
                    Log.i(LOG, "Signed string is valid!!!!!!!!");
                    valid = true;
                } else {
                    Log.i(LOG, "Signed string isnot valid");
                }

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        return valid;

    }



}
