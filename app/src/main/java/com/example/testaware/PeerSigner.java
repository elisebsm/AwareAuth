package com.example.testaware;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.IdentityHashMap;

public class PeerSigner {

    //Does not have SSL context yet
    //HaveCSR from peer
    //goes in here if the certificate provided to peer is self-signed and not signed by root-ca




    private X509Certificate peerCertificate;
    private String test;


    public PeerSigner(String test){ //TODO: change to X509Certificate peerCertificate
        this.test= test;

    }

    public static void peerSign() throws IOException, CertificateException {
        //get own Certificate, to send later
        X509Certificate signerCertificate= getCertificate();

        //get own keyst for signing
        KeyPair signerKeyPair= getKeyPair();


        //TODO: just for testing - get file sent from peer later.
        //get peer self signed certificate,

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        File caFile = new File("/data/data/com.example.testaware/keystore/client2.pem");
        InputStream inputStreamCertificate = null; //TODO close stream
        inputStreamCertificate = new BufferedInputStream(new FileInputStream(caFile));
        X509Certificate peerCertificate = (X509Certificate) certificateFactory.generateCertificate(inputStreamCertificate);

        String alias = peerCertificate.getSubjectX500Principal().getName();

        // get public key
        PublicKey peerPubKey= peerCertificate.getPublicKey();


        signPeerKey(peerPubKey, signerKeyPair);


        //and add peerCert to trust store
      /*  KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream fis = new FileInputStream("/data/data/com.example.testaware/keystore/trust.store");
        trustStore.load(fis, "elise123".toCharArray());
        trustStore.setCertificateEntry(alias, peerCertificate);
*/
    }

    public static void signPeerKey(PublicKey peerKey, KeyPair signerKeyPair){

        try {
            File keyFile= new File("signedKey.txt");
            if (keyFile.createNewFile()) {

                //FileWriter myWriter = new FileWriter("signedKey.txt");

                PrivateKey privKey = signerKeyPair.getPrivate();

                Signature ecdsa = Signature.getInstance("SHA256withECDSA");

                ecdsa.initSign(privKey);

                String str = peerKey.toString();;
                byte[] strByte = str.getBytes("UTF-8");
                ecdsa.update(strByte);

                byte[] realSig = ecdsa.sign();
                String signature= new BigInteger(1, realSig).toString(16);
                System.out.println("Signature: " + signature);

                ecdsa.initVerify(signerKeyPair.getPublic());
                ecdsa.update(realSig);
                if (ecdsa.verify(realSig))
                    System.out.println("valid");
                else
                    System.out.println("invalid!!!!");
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }
/*
    public Boolean verifySignatureOnKey(String signature) throws NoSuchAlgorithmException, InvalidKeyException {

        //Signature ecdsa = Signature.getInstance("SHA256withECDSA");
        // Validation
        ecdsa.initVerify(signerKeyPair.getPublic());
        ecdsa.update(signature);
        if (ecdsa.verify(signature))
            System.out.println("valid");
        else
            System.out.println("invalid!!!!");

    }
*/

    public static KeyPair getKeyPair(){
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            //FileInputStream fileInputStream = new FileInputStream(getPKCS(context));

            FileInputStream fileInputStream = new FileInputStream( new File("/data/data/com.example.testaware/keystore/keystore.jks"));

            keyStore.load(fileInputStream, "elise123".toCharArray());
            Enumeration<String> stringEnumeration = keyStore.aliases();
            String alias = "";
            boolean isAliasWithPrivateKey = false;

            while(stringEnumeration.hasMoreElements()){
                alias = stringEnumeration.nextElement();
                if(isAliasWithPrivateKey = keyStore.isKeyEntry(alias)){
                    break;
                }
            }
            if(isAliasWithPrivateKey) {
                Key key = keyStore.getKey(alias, new char[0]);
                if (key instanceof PrivateKey){
                    Certificate certificate = keyStore.getCertificate(alias);
                    PublicKey publicKey = certificate.getPublicKey();
                    return new KeyPair(publicKey, (PrivateKey) key);
                }
            }
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        //return Objects.requireNonNull(files)[0];
        return null;
    }


    public static X509Certificate getCertificate() {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            FileInputStream fileInputStream = new FileInputStream("/data/data/com.example.testaware/keystore/keystore.jks"); //todo: get the right file (.p12 format? PKCS)
            keyStore.load(fileInputStream, "elise123".toCharArray());

            Enumeration<String> es = keyStore.aliases();
            String alias = "";

            boolean isAliasWithPrivateKey = false;

            while (es.hasMoreElements()) {
                alias = es.nextElement();
                if (isAliasWithPrivateKey = keyStore.isKeyEntry(alias)) {
                    break;
                }
            }
            if (isAliasWithPrivateKey) {
                Key key = keyStore.getKey(alias, "elise123".toCharArray());
                if (key instanceof PrivateKey) {
                    return (X509Certificate) keyStore.getCertificate(alias);
                }
            }

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        return null;
    }


}
