package com.example.testaware.offlineAuth;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;


import com.example.testaware.ClientHandler;
import com.example.testaware.ConnectedClient;
import com.example.testaware.Constants;
import com.example.testaware.activities.MainActivity;

import com.example.testaware.listitems.MessageListItem;
import com.example.testaware.models.AbstractPacket;
import com.example.testaware.models.Contact;
import com.example.testaware.models.Message;
import com.example.testaware.models.MessagePacket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.security.PublicKey;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import lombok.Getter;

//this class start server on diff port than other server, so one server that accepts connections to clients who dont have certificates (but are authenticated by peer)
public class PeerAuthServer {

    private String LOG = "LOG-Test-Aware-No-Auth-App-Server";
    private DataInputStream inputStream;
    private DataOutputStream outputStream;   //TODO: use so client can also send messages
    private boolean running;
    private Map<PublicKey, ConnectedClient> clients;
    private final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);
    private ExecutorService sendService = Executors.newSingleThreadExecutor();
    private String [] protocol;
    private ClientHandler noAuthClient;


    @Getter
    private static WeakReference<MainActivity> mainActivity;


    public static void updateActivity(MainActivity activity) {
        mainActivity = new WeakReference<>(activity);
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public PeerAuthServer(SSLContext serverSSLContext, int serverPort,  String pubKey){  //use SERVER_PORT_NO_AUTH
        running = true;
        clients = new ConcurrentHashMap<>();
        protocol= new String[1];
        protocol [0]= Constants.SUPPORTED_CIPHER_GCM;

        Runnable serverTask = () -> {
            running  = true;
            try {
                SSLServerSocket serverSocket = (SSLServerSocket) serverSSLContext.getServerSocketFactory().createServerSocket(serverPort);
                serverSocket.setEnabledCipherSuites(protocol);  // no need for client auth

                while (running) {
                    SSLSocket sslClientSocket = (SSLSocket) serverSocket.accept();
                    String connectedPeerIP = sslClientSocket.getInetAddress().getHostAddress();
                    //check if key and ip is in auth list
                   // if (VerifyUser.isAuthenticatedUser(pubKey, connectedPeerIP)) {  //not sure if this is nessesary
                    if(true){
                        //addClient(sslClientSocket);
                        Log.d(LOG, "Peer auth client accepted");
                        inputStream = new DataInputStream(new BufferedInputStream(sslClientSocket.getInputStream()));
                        outputStream = new DataOutputStream(new BufferedOutputStream(sslClientSocket.getOutputStream()));

                        noAuthClient = new ClientHandler(inputStream, outputStream,sslClientSocket);
                        Thread t = new Thread(noAuthClient);
                        t.start();
                        Log.d(LOG, "Starting new peer auth client Thread -");

                    }
                    else{
                        //stop conn if user not authenticated
                        sslClientSocket.close();
                        running =false;
                    }
                }
                 //   serverSocket.close();  //TODO: close socket
                }  catch (IOException e) {
                    Log.d(LOG, Objects.requireNonNull(e.getMessage()));
                    e.printStackTrace();
                    Log.d(LOG, "Exception in AppServer in constructor");
                }


            };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }

/*
    protected void addClient(SSLSocket sslClientSocket){
        ConnectedClient clientTask = new ConnectedClient(this, sslClientSocket);
        clients.put(clientTask.getUserIdentity().getPublicKey(), clientTask);
        clientProcessingPool.submit(clientTask);
    }
*/

    protected void removeClient(ConnectedClient connectedClient){
        if(clients.containsKey(connectedClient.getUserIdentity().getPublicKey())){
            connectedClient.stop();
            clients.remove(connectedClient.getUserIdentity().getPublicKey());
        }
    }


    public void stop(){
        for (ConnectedClient client: clients.values()){
            removeClient(client);
        }
        running = false;
    }



    public void sendMessage(String message){
        noAuthClient.sendMessage(message);

    }


}


