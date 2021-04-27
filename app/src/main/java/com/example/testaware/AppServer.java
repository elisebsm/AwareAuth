package com.example.testaware;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;

import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.testaware.activities.MainActivity;
import com.example.testaware.listeners.ConnectionListener;
import com.example.testaware.listeners.MessageReceivedObserver;
import com.example.testaware.listeners.OnMessageReceivedListener;
import com.example.testaware.listeners.OnSSLContextChangedListener;
import com.example.testaware.listeners.SSLContextedObserver;
import com.example.testaware.listitems.MessageListItem;
import com.example.testaware.models.Message;
import com.example.testaware.models.MessagePacket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private String LOG = "Log-App-Server";
    //private ObjectInputStream inputStream;
    //private ObjectOutputStream outputStream;   //TODO: use so client can also send messages

    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private boolean running;
    private Map<PublicKey, ConnectedClient> clients;
    private final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);
    private ExecutorService sendService = Executors.newSingleThreadExecutor();
    private String [] protocolGCM;
    private String [] protocolCHACHA;

    private List<ConnectionListener> connectionListeners;

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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public AppServer(SSLContext serverSSLContext, Network network){
        running = true;
        clients = new ConcurrentHashMap<>();
        protocolGCM = new String[1];
        protocolGCM [0]= Constants.SUPPORTED_CIPHER_GCM;

        protocolCHACHA = new String[1];
        protocolCHACHA [0]= Constants.SUPPORTED_CIPHER_CHACHA;


        connectionListeners = new ArrayList<>();

        Runnable serverTask = () -> {
            running  = true;
            try {
                serverSocket = (SSLServerSocket) serverSSLContext.getServerSocketFactory().createServerSocket(0  );
                localPort = serverSocket.getLocalPort();
                Log.d(LOG, "Port: "+ localPort);
                serverSocket.setEnabledCipherSuites(protocolCHACHA);
                Log.d(LOG, "Ciphers supported"+ Arrays.toString(protocolCHACHA));
                serverSocket.setNeedClientAuth(true);
                mainActivity.get().setServerPort(network);

               while (running) {
                    SSLSocket sslClientSocket = (SSLSocket) serverSocket.accept();
                    //addClient(sslClientSocket);
                    sslClientSocket.getPort();
                    Log.d(LOG, "client accepted");
                    inputStream = new DataInputStream(sslClientSocket.getInputStream());
                    outputStream = new DataOutputStream(sslClientSocket.getOutputStream());

                    client = new ClientHandler(inputStream, outputStream , sslClientSocket, connectionListeners );
                    Thread t = new Thread(client);
                    t.start();
                    Log.d(LOG, "Starting new Thread -");
                    outputStream.flush();

                    /*while(running){
                        if (inputStream != null ){

                            Log.d(LOG, "inputstream AppServer");
                            com.example.testaware.models.Message message = (com.example.testaware.models.Message) inputStream.readObject();
                            messageReceivedObserver.setMessage(message);
                            Log.d(LOG, "Setting mesage in observer");
                        }
                    }
*/
                  /*  while(running){
                        if (inputStream != null){

                            //AbstractPacket abstractPacket = (AbstractPacket) inputStream.readObject();
                            //onPacketReceived(abstractPacket);

                            String strMessageFromClient = String.valueOf(inputStream.readObject());  //FEIL
                            Log.d(LOG, "Reading message " + strMessageFromClient);

                            MessageListItem chatMsg = new MessageListItem(strMessageFromClient, "ipv6_other_user");
                           // MessageListItem chatMsg = new MessageListItem(strMessageFromClient, "ipv6_other_user");    //TODO: GET USERNAME FROM CHATLISTITEM
                        }
                    }*/
                }
            }  catch (IOException  e) {
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
        ConnectedClient clientTask = new ConnectedClient(this, sslClientSocket);
        clients.put(clientTask.getUserIdentity().getPublicKey(), clientTask);
        clientProcessingPool.submit(clientTask);
    }


    protected void removeClient(ConnectedClient connectedClient){
        if(clients.containsKey(connectedClient.getUserIdentity().getPublicKey())){
            connectedClient.stop();
            clients.remove(connectedClient.getUserIdentity().getPublicKey());
        }
    }


    public void stop(){
        /*for (ConnectedClient client: clients.values()){
            removeClient(client);
        }*/
        running = false;
        if(client!=null){

            client.setRunning(false);
        }
    }

    public void setListener(ConnectionListener listener){
        connectionListeners.add(listener);
    }

   /* private void onPacketReceived(AbstractPacket packet) {
        Contact from = new Contact(getServerIdentity());
        Log.d(LOG, packet.getClass().getSimpleName() + " from " + from.getCommonName());
        for(ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onPacket(from, packet);
        }
    }


    /*protected void onPacketReceived(ConnectedClient device, AbstractPacket packet){
        Contact from = new Contact(device.getUserIdentity()); // den som sender pakken
        Log.d(LOG, packet.getClass().getSimpleName() + " from " + from.getCommonName());
        mainActivity.get().getConnectionHandler().getAppClient().getConnectionListeners();

        for(ConnectionListener listeners: mainActivity.get().getConnectionHandler().getAppClient().getConnectionListeners()){
            listeners.onServerPacket(packet);
        }

        if (packet instanceof MessagePacket) {
            PublicKey to = ((MessagePacket)packet).getMessage().getTo(); // henter ut public key av meldingen dersom det er av type MessagePacket

            if(clients.containsKey(to)){
                ConnectedClient toClient = clients.get(to); // sjekker med listen av klienter for å finne match på public key
                Runnable packetForwardingTask = () -> {
                    toClient.send(packet);  // pakken sendes til klienten
                };
                Thread packetForwardingThred = new Thread(packetForwardingTask);
                packetForwardingThred.start();
            }
        }
    }*/



    public void sendMessage(String message){
        if(client != null){
            client.sendMessage(message);
        } else {
            Log.d(LOG, "Client is null");
        }
       /* if(outputStream == null){
            Log.d(LOG, "outputstream is null");
            return false;
        }
        Runnable sendMessageRunnable = () -> {
            try {
                MessagePacket messagePacket = (new MessagePacket(message));
                Log.d(LOG, "outputstream " + message);
                outputStream.writeObject(messagePacket);
                outputStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(LOG, "Exception in AppServer  in sendMessage()");
                running = false;
            }
        };
        sendService.submit(sendMessageRunnable);
        return true;*/
    }


}


