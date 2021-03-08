package com.example.testaware;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.sql.ConnectionEventListener;

public class IdentityHandler {
    SSLContext sslContext;

    public SSLContext getSSLContext(Context context) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException {

        //FileInputStream inputStreamCertificate = context.openFileInput("rsacert.pem")

// key manager
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType); //TODO keystoresype
        FileInputStream fileInputStream = new FileInputStream("KeyStore"); //todo: get the right file (.p12 format? PKCS)
        keyStore.load(fileInputStream, null); //TOdO: set password

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
        keyManagerFactory.init(keyStore, null);
        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

//trust manager
        File caFile = getCA(context);
        InputStream inputStreamCertificate = new BufferedInputStream(new FileInputStream(caFile)); //TODO close stream
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStreamCertificate);
        String certificateAlias = certificate.getSubjectX500Principal().getName();
        KeyStore keyStoreCA = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStoreCA.load(null);
        keyStoreCA.setCertificateEntry(certificateAlias, certificate);

        KeyManagerFactory keyManagerFactoryCA = KeyManagerFactory.getInstance("X509");
        keyManagerFactory.init(keyStoreCA, null);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        trustManagerFactory.init(keyStoreCA);
        TrustManager [] trustManagers = trustManagerFactory.getTrustManagers();

        // Create an SSLContext that uses our TrustManager
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, null);
        return sslContext;
    }

    //TODO: tell the socket connection to use a socketfactory??
    // for http from android developer Tell the URLConnection to use a SocketFactory from our SSLContext

    private File getCA(Context context){
        //TODO: get ca file (.pem file)
        return null;
    }
}
