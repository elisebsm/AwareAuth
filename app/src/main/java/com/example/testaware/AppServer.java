package com.example.testaware;

import android.os.Build;

import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.testaware.activities.MainActivity;

import com.example.testaware.offlineAuth.PeerSigner;
import com.example.testaware.offlineAuth.VerifyUser;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;


import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import lombok.Getter;

/** Client can also instantiate connection.
    Implements runnable in order to be extecuted by a thread. must implement run().
    Intended for objects that need to execute code while they are active.

    Decides the cipher suite used with the variables protocolGCM and protocolCHACHA. In order for
    for this to work the TLS version must be 1.2.

 **/
public class AppServer {

    private final String LOG = "Log-App-Server";

    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private boolean running;
    private final String [] tlsVersion;
    private SSLSocket sslClientSocket;
    private boolean userCertificateCorrect =true;

    @Getter
    private static WeakReference<MainActivity> mainActivity;

    public static void updateActivity(MainActivity activity) {
        mainActivity = new WeakReference<>(activity);
    }

    private ClientHandler client;

    @Getter
    private SSLServerSocket serverSocket;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public AppServer(SSLContext serverSSLContext){
        running = true;
        String[] protocolGCM = new String[1];
        protocolGCM[0]= Constants.SUPPORTED_CIPHER_GCM;

        String[] protocolCHACHA = new String[1];
        protocolCHACHA[0]= Constants.SUPPORTED_CIPHER_CHACHA;

        tlsVersion = new String[1];
        tlsVersion [0] = "TLSv1.2";

        Runnable serverTask = () -> {
            running  = true;
            try {
                serverSocket = (SSLServerSocket) serverSSLContext.getServerSocketFactory().createServerSocket(1025);
                serverSocket.setEnabledProtocols(tlsVersion);

                serverSocket.setEnabledCipherSuites(protocolCHACHA);
                serverSocket.setNeedClientAuth(true);

               while (running) {
                    sslClientSocket = (SSLSocket) serverSocket.accept();
                    serverSocket.getEnabledCipherSuites();
                    sslClientSocket.getPort();

                    sslClientSocket.addHandshakeCompletedListener(event -> {
                        if(event.getSession().isValid() ){
                            Log.d(LOG, "Handshake completed");
                            X509Certificate peerCert = getClientIdentity();
                            if(userCertificateCorrect && peerCert != null) {
                                addPeerAuthInfo(peerCert);
                            }
                        }
                    });
                    Log.d(LOG, "Client accepted");

                    inputStream = new DataInputStream(sslClientSocket.getInputStream());
                    outputStream = new DataOutputStream(sslClientSocket.getOutputStream());

                    client = new ClientHandler(inputStream, outputStream);
                    Thread t = new Thread(client);
                    t.start();
                    Log.d(LOG, "Starting new Thread -");
                    outputStream.flush();
               }
            }  catch (IOException  e) {
                e.printStackTrace();
                Log.d(LOG, "Exception in AppServer in constructor");
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }


    public void sendMessage(String message){
        if(client != null){
            client.sendMessage(message);
        } else {
            Log.d(LOG, "Client is null");
        }
    }


    private X509Certificate getClientIdentity() {
        try {
            Certificate[] certs = sslClientSocket.getSession().getPeerCertificates();
            if(certs.length > 0 && certs[0] instanceof X509Certificate) {
                return (X509Certificate) certs[0];
            }
        } catch (SSLPeerUnverifiedException | NullPointerException ignored) {
            ignored.printStackTrace();
            userCertificateCorrect =false;
            Log.d(LOG, "Cert not valid");
        }
        return null;
    }


    private void addPeerAuthInfo(X509Certificate peerCert){
        PublicKey peerPubKey= peerCert.getPublicKey();
        VerifyUser.setValidatedAuthenticator(peerPubKey);
        PeerSigner.deleteTmpFile();
    }
}