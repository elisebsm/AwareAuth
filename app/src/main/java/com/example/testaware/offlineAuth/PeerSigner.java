package com.example.testaware.offlineAuth;

import android.util.Log;

import com.example.testaware.IdentityHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

/*
This method is supposed to be called before the ssl connection is established with another peer, because user is not yet authenticated. Peer authenticates another peer. Web of trust. Sign public key
of user who wants to get authenticated.
*/

public class PeerSigner {

    private static String LOG = "PeerSigner";
    private static String signedKey;
    private static X509Certificate signerCertificate;
    private static X509Certificate peerCertificate;

    public static String peerSign() {
        try {

        signerCertificate= IdentityHandler.getCertificate();
        KeyPair signerKeyPair= IdentityHandler.getKeyPair();

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        File caFile = new File("/data/data/com.example.testaware/client2.pem");  //TODO: take in peer pem file received from peer, or get public key from peer
        InputStream inputStreamCertificate = null; //TODO close stream
        inputStreamCertificate = new BufferedInputStream(new FileInputStream(caFile));
        peerCertificate = (X509Certificate) certificateFactory.generateCertificate(inputStreamCertificate);
        String alias = peerCertificate.getSubjectX500Principal().getName();
        PublicKey peerPubKey= peerCertificate.getPublicKey();

        signPeerKey(peerPubKey, signerKeyPair);

        } catch (Exception e) {
            Log.i(LOG, e.toString());
            e.printStackTrace();
        }
        return signedKey;
    }

    public static void signPeerKey(PublicKey peerKey, KeyPair signerKeyPair) {

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

            Boolean valid= VerifyCredentials.verifyCredentials(signedKey, signerCertificate, peerCertificate);
            Log.i(LOG, "Verify is " +valid+ "!!!!!!!");
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }
}
