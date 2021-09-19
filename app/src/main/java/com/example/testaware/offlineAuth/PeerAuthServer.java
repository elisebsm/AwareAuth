package com.example.testaware.offlineAuth;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;


import com.example.testaware.ClientHandler;
import com.example.testaware.Constants;
import com.example.testaware.activities.MainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.PublicKey;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import lombok.Getter;

/**this class start server on diff port than other server, so one server that accepts connections to
 * clients who dont have certificates (but are authenticated by peer)**/
public class PeerAuthServer {

    private String LOG = "Log-Test-Aware-No-Auth-App-Server";
    private DataInputStream inputStream;
    private DataOutputStream outputStream;   //TODO: use so client can also send messages
    private boolean running;
    private ClientHandler noAuthClient;
    private SSLSocket sslClientSocket;
    private boolean userPeerAuth=false;
    private final String [] tlsVersion;

    @Getter
    private static WeakReference<MainActivity> mainActivity;

    public static void updateActivity(MainActivity activity) {
        mainActivity = new WeakReference<>(activity);
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public PeerAuthServer(SSLContext serverSSLContext,  PublicKey requestingClientKey){
        running = true;
        int serverPort= Constants.SERVER_PORT_NO_AUTH;

        String[] protocolGCM = new String[1];
        protocolGCM[0]= Constants.SUPPORTED_CIPHER_GCM;

        String[] protocolCHACHA = new String[1];
        protocolCHACHA[0]= Constants.SUPPORTED_CIPHER_CHACHA;

        tlsVersion = new String[1];
        tlsVersion [0] = "TLSv1.2";

        Runnable serverTask = () -> {
            running  = true;
            try {
                SSLServerSocket serverSocket = (SSLServerSocket) serverSSLContext.getServerSocketFactory().createServerSocket(serverPort);  //TODO: change to 0 ?
                serverSocket.setEnabledProtocols(tlsVersion);
                serverSocket.setEnabledCipherSuites(protocolCHACHA);

                while (running) {
                    sslClientSocket = (SSLSocket) serverSocket.accept();

                    if (VerifyUser.isAuthenticatedUser(requestingClientKey)) {
                        Log.d(LOG, "User is PA. Accepting connection ");
                        userPeerAuth = true;
                    }
                    if(userPeerAuth) {
                        inputStream = new DataInputStream(new BufferedInputStream(sslClientSocket.getInputStream()));
                        outputStream = new DataOutputStream(new BufferedOutputStream(sslClientSocket.getOutputStream()));

                        noAuthClient = new ClientHandler(inputStream, outputStream);
                        Thread t = new Thread(noAuthClient);
                        t.start();
                        outputStream.flush();
                    }
                    else{
                          sslClientSocket.close();
                          Log.d(LOG, "Server socket closing, user not peer authenticated");
                          running =false;
                     }
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
            };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }


    public void sendMessage(String message){
        if(noAuthClient != null){
            noAuthClient.sendMessage(message);
        }
    }
}