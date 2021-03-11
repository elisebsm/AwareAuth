package com.example.testaware;

import android.content.Context;
import android.net.wifi.aware.WifiAwareManager;
import android.util.Log;

//import com.example.testaware.Contact;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class IdentityHandler {
    SSLContext sslContext;


    public static SSLContext getSSLContext(Context context){

        //FileInputStream inputStreamCertificate = context.openFileInput("rsacert.pem")


        try {

            //get client cert from keystore
            X509Certificate cert= ( X509Certificate) getCertificate(context);

            //get keypair of client from keystore
            KeyPair keyP = getKeyPair();


            // key manager
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

            //keystore containing certificate
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            //FileInputStream fileInputStream = new FileInputStream("KeyStore");
            FileInputStream fileInputStream = new FileInputStream("/data/data/com.example.testaware/files/keystore/keystore.p12"); //todo: get the right file (.p12 format? PKCS)
            keyStore.load(fileInputStream, "Master2021".toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
            keyManagerFactory.init(keyStore, null);
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

//trust manager
            //File caFile = getCA(context); //TODO change filepath
            File caFile = new File("/data/data/com.example.testaware/files/ca/root_ca.pem");

            InputStream inputStreamCertificate = null; //TODO close stream
            inputStreamCertificate = new BufferedInputStream(new FileInputStream(caFile));
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStreamCertificate);
            String certificateAlias = certificate.getSubjectX500Principal().getName();
            KeyStore keyStoreCA = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStoreCA.load(null);
            keyStoreCA.setCertificateEntry(certificateAlias, certificate);

            KeyManagerFactory keyManagerFactoryCA = KeyManagerFactory.getInstance("X509");
            keyManagerFactoryCA.init(keyStoreCA, null);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
            trustManagerFactory.init(keyStoreCA);
            TrustManager [] trustManagers = trustManagerFactory.getTrustManagers();

            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);
            return sslContext;
        } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }


    //TODO: tell the socket connection to use a socketfactory??
    // for http from android developer Tell the URLConnection to use a SocketFactory from our SSLContext

    private static File getCA(Context context){
        File [] files = context.getExternalFilesDirs("ca")[0].listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pem");
            }
        });
        //TODO: get ca file (.pem file)
        return Objects.requireNonNull(files)[0];

    }

    private static File getPKCS(Context context){
        File [] files = context.getExternalFilesDirs("keystore")[0].listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pk12");
            }
        });
        return null;
    }


    private static File [] getContactFile(Context context){
        return context.getExternalFilesDirs("contacts")[0].listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pem");
            }
        });
    }
/*
    public static ArrayList<Contact> getContacts(Context context){
        ArrayList<Contact> contacts = new ArrayList<>();
        for (File contactFile: getContactFile(context)){
            try {
                FileInputStream fileInputStream = new FileInputStream(contactFile);
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(fileInputStream);
                contacts.add(new Contact(x509Certificate));
            } catch (FileNotFoundException | CertificateException e) {
                e.printStackTrace();
            }

        }
        return contacts;
    }
    */


    public static KeyPair getKeyPair(){
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            //FileInputStream fileInputStream = new FileInputStream(getPKCS(context));
            FileInputStream fileInputStream = new FileInputStream("/data/data/com.example.testaware/files/keystore/test3/keystore.jks"); //todo: get the right file (.p12 format? PKCS)
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
                Key key = keyStore.getKey(alias, "elise123".toCharArray());
                if (key instanceof PrivateKey){
                    Certificate certificate = keyStore.getCertificate(alias);
                    PublicKey publicKey = certificate.getPublicKey();
                    return new KeyPair(publicKey, (PrivateKey) key);
                }
            }
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    //works, tested in main
    public static X509Certificate getCertificate(Context context) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            FileInputStream fileInputStream = new FileInputStream("/data/data/com.example.testaware/files/keystore/test3/keystore.jks"); //todo: get the right file (.p12 format? PKCS)
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

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        }

        return null;
    }






}
