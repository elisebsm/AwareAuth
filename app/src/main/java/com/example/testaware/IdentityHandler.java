package com.example.testaware;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class IdentityHandler {
    SSLContext sslContext;


    public static SSLContext getSSLContext(Context context) {
        //FileInputStream inputStreamCertificate = context.openFileInput("rsacert.pem")
            try {
                //keystore containing certificate
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                FileInputStream fileInputStream = new FileInputStream(Constants.KEYSTORE_PATH);
                keyStore.load(fileInputStream, "elise123".toCharArray());

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
                keyManagerFactory.init(keyStore, "elise123".toCharArray());

                KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();



                X509Certificate rootCertificate = (X509Certificate) keyStore.getCertificate("root");
                String certificateAlias = rootCertificate.getSubjectX500Principal().getName();
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null);

                trustStore.setCertificateEntry(certificateAlias, rootCertificate);

                KeyManagerFactory trustKeyManagerFactory = KeyManagerFactory.getInstance("X509");
                trustKeyManagerFactory.init(trustStore, "elise123".toCharArray());
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
                trustManagerFactory.init(trustStore);
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

                SSLContext sslContext = SSLContext.getInstance("TLS");
                //SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
                sslContext.init(keyManagers, trustManagers, null);


                return sslContext;
            } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
                e.printStackTrace();
            }
            return null;
    }




    public static KeyPair getKeyPair(){
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            FileInputStream fileInputStream = new FileInputStream( new File(Constants.KEYSTORE_PATH));
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
            FileInputStream fileInputStream = new FileInputStream(Constants.KEYSTORE_PATH);
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
