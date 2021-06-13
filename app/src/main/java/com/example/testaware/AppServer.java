package com.example.testaware;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
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

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import lombok.Getter;


//client can also instantiate connection.
//implements runnable in order to be extecuted by a thread. must implement run(). Intended for objects that need to execute code while they are active.
public class AppServer {

    private final String LOG = "Log-App-Server";

    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private boolean running;
    private final String [] tlsVersion;
    private SSLSocket sslClientSocket;  //remove
    private boolean userCertificateCorrect =true;



    @Getter
    private static WeakReference<MainActivity> mainActivity;

    public static void updateActivity(MainActivity activity) {
        mainActivity = new WeakReference<>(activity);
    }

    private ClientHandler client;

    @Getter
    private SSLServerSocket serverSocket;

    @Getter
    private int localPort;

    private int counterValue = 0;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public AppServer(SSLContext serverSSLContext, Network network){
        running = true;
        String[] protocolGCM = new String[1];
        protocolGCM[0]= Constants.SUPPORTED_CIPHER_GCM;

        String[] protocolCHACHA = new String[1];
        protocolCHACHA[0]= Constants.SUPPORTED_CIPHER_CHACHA;

        tlsVersion = new String[1];
        tlsVersion [0] = "TLSv1.2";



        counterValue = mainActivity.get().getCountervalue();


        Runnable serverTask = () -> {
            running  = true;
            try {
                serverSocket = (SSLServerSocket) serverSSLContext.getServerSocketFactory().createServerSocket(1025);
                serverSocket.setEnabledProtocols(tlsVersion);
                Log.d(LOG, "Port: "+ localPort);

               /* serverSocket = (SSLServerSocket) serverSSLContext.getServerSocketFactory().createServerSocket(1025  );
                //localPort = serverSocket.getLocalPort();
                serverSocket.setEnabledProtocols(tlsVersion);*/

                serverSocket.setEnabledCipherSuites(protocolCHACHA);
                serverSocket.setNeedClientAuth(true);


               while (running) {
                    sslClientSocket = (SSLSocket) serverSocket.accept();
                    serverSocket.getEnabledCipherSuites();
                    sslClientSocket.getPort();

                    sslClientSocket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
                        @Override
                        public void handshakeCompleted(HandshakeCompletedEvent event) {
                            if(event.getSession().isValid() ){
                                Log.d(LOG, "Handshake completed");
                                X509Certificate peerCert = getClientIdentity();
                                if(userCertificateCorrect && peerCert != null) {
                                    addPeerAuthInfo(peerCert);
                                }
                            }
                        }
                    });


                    Log.d(LOG, "client accepted");

                    inputStream = new DataInputStream(sslClientSocket.getInputStream());
                    outputStream = new DataOutputStream(sslClientSocket.getOutputStream());

                    client = new ClientHandler(inputStream, outputStream , sslClientSocket, counterValue );
                    Thread t = new Thread(client);
                    t.start();
                    Log.d(LOG, "Starting new Thread -");
                    outputStream.flush(); //TODO: check if we need this, is flused in clienthandeler
               }
            }  catch (IOException  e) {


                e.printStackTrace();
                Log.d(LOG, "Exception in AppServer in constructor");
            }
            //TODO: close socket
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }


    public void stop(){
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(client!=null){
            client.setRunning(false);
        }
    }


    public void sendMessage(String message, long sendingMessageTime){
        if(client != null){
            client.sendMessage(message, sendingMessageTime);
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
     //   ArrayList<String> listOfSingedStrings = PeerSigner.getTmpPeerAuthInfo(true);
       /*  ArrayList<String> listOfTrustedAuthenticators = PeerSigner.getTmpPeerAuthInfo(false);
        if(listOfSingedStrings!= null) {
            for (int i = 0; i < listOfSingedStrings.size(); i++) {
                PeerSigner.saveSignedKeyToFile(listOfSingedStrings.get(i));
            }
        }

        if(listOfTrustedAuthenticators != null){
            for (int i = 0; i < listOfTrustedAuthenticators.size(); i++) {
                PublicKey pubKeyDecoded = Decoder.getPubKeyGenerated(listOfTrustedAuthenticators.get(i));
                VerifyUser.setValidatedAuthenticator(pubKeyDecoded);
            }
            PeerSigner.deleteTmpFile();
        }

         */
    }


}


