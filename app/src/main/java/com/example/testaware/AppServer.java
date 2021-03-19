package com.example.testaware;

import android.os.Build;
import android.transition.ChangeTransform;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.testaware.activities.ChatActivity;
import com.example.testaware.activities.MainActivity;
import com.example.testaware.adapters.MessageListAdapter;
import com.example.testaware.listeners.ConnectionListener;
import com.example.testaware.listitems.MessageListItem;
import com.example.testaware.models.AbstractPacket;
import com.example.testaware.models.Contact;
import com.example.testaware.models.MessagePacket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.net.Inet6Address;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import lombok.Getter;


//client can also instantiate connection.
//implements runnable in order to be extecuted by a thread. must implement run(). Intended for objects that need to execute code while they are active.
public class AppServer {


    private String LOG = "LOG-Test-Aware-App-Server";
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;   //TODO: use so client can also send messages
    private boolean running;
    private Map<PublicKey, ConnectedDevice> clients;

    private final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);


    @Getter
    private static WeakReference<MainActivity> mainActivity; //Spørsmål what is this??

    public static void updateActivity(MainActivity activity) {
        mainActivity = new WeakReference<>(activity);
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public AppServer(SSLContext serverSSLContext, int serverPort){
        running = true;
        clients = new ConcurrentHashMap<>();

        Runnable serverTask = () -> {
            running  = true;
            try {
                SSLServerSocket serverSocket = (SSLServerSocket) serverSSLContext.getServerSocketFactory().createServerSocket(serverPort);
                serverSocket.setNeedClientAuth(true);
                while (running) {
                    SSLSocket sslClientSocket = (SSLSocket) serverSocket.accept();
                    //addClient(sslClientSocket);

                    Log.d(LOG, "client accepted");
                    inputStream = new ObjectInputStream(new BufferedInputStream(sslClientSocket.getInputStream()));
                    outputStream = new ObjectOutputStream(new BufferedOutputStream(sslClientSocket.getOutputStream()));
                    Thread t = new ClientHandeler(serverPort, inputStream, outputStream);
                    t.start();
                    Log.d(LOG, "Starting new Thread -");
                    /*
                    while(running){
                        if (inputStream != null){
                            String strMessageFromClient = (String) inputStream.readObject();  //FEIL
                            Log.d(LOG, "Reading message " + strMessageFromClient);

                            MessageListItem chatMsg = new MessageListItem(strMessageFromClient, "ipv6_other_user");    //TODO: GET USERNAME FROM CHATLISTITEM
                        }
                    }*/
                }
            } catch (IOException e) {
               Log.d(LOG, Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
                Log.d(LOG, "Exception in AppServer in constructor");
            }
            //TODO: close socket
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }



    protected void addClient(SSLSocket sslClientSocket){
        ConnectedDevice connectedDevice = new ConnectedDevice(this, sslClientSocket);
        clients.put(connectedDevice.getUserIdentity().getPublicKey(), connectedDevice);
        clientProcessingPool.submit(connectedDevice);
    }


    protected void removeClient(ConnectedDevice connectedDevice){
        if(clients.containsKey(connectedDevice.getUserIdentity().getPublicKey())){
            connectedDevice.stop();
            clients.remove(connectedDevice.getUserIdentity().getPublicKey());
        }
    }


    public void stop(){
        for (ConnectedDevice device: clients.values()){
            removeClient(device);
        }
        running = false;
    }

/*
    protected void onPacketReceived(ConnectedDevice device, AbstractPacket packet){
        Contact from = new Contact(device.getUserIdentity()); // den som sender pakken
        Log.d(LOG, packet.getClass().getSimpleName() + " from " + from.getCommonName());
        mainActivity.get().getConnectionHandler().getAppClient().getConnectionListeners();

        for(ConnectionListener listeners: mainActivity.get().getConnectionHandler().getAppClient().getConnectionListeners()){
            listeners.onServerPacket(packet);
        }

        if (packet instanceof MessagePacket) { //TODO: Message or MessagePacket?
            PublicKey to = ((MessagePacket)packet).getMessage().getTo(); // henter ut public key av meldingen dersom det er av type MessagePacket

            if(clients.containsKey(to)){
                ConnectedDevice toClient = clients.get(to); // sjekker med listen av klienter for å finne match på public key
                Runnable packetForwardingTask = () -> {
                    toClient.send(packet);  // pakken sendes til klienten
                };
                Thread packetForwardingThred = new Thread(packetForwardingTask);
                packetForwardingThred.start();
            }
        }
    }
    */

}


