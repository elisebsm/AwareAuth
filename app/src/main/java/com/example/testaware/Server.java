package com.example.testaware;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.testaware.activities.ChatActivity;
import com.example.testaware.activities.MainActivity;
import com.example.testaware.adapters.MessageListAdapter;
import com.example.testaware.listitems.MessageListItem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Inet6Address;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

public class Server {


    private static Inet6Address peerIpv6;
    private static WeakReference<MainActivity> mainActivity; //TODO what is this??
    private String LOG = "LOG-Test-Aware-Server";
    private MessageListAdapter mMessageAdapter;  //endret til test
    private ArrayList<MessageListItem> messageList;
    private String strMessageFromClient;
    private SSLContext sslContext;
    private DataInputStream inputStream;  //TODO: change to objectinputstream??
    private DataOutputStream outputStream;   //TODO: use so client can also send messages
    private boolean running;
    private Map<PublicKey, ConnectedDevice> clients;

    private final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);
    private KeyPair keyPair;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Server(SSLContext sslContext, int serverPort){
        running = true;

       // this.sslContext = MainActivity.getSslContext();
        //this.sslContext = IdentityHandler.getSSLContext(this.context);
        this.keyPair = IdentityHandler.getKeyPair();

            running  = true;
            try {
                SSLServerSocket serverSocket = (SSLServerSocket) sslContext.getServerSocketFactory().createServerSocket(serverPort);
                serverSocket.setNeedClientAuth(true);
                while (running) {
                    SSLSocket sslClientSocket = (SSLSocket) serverSocket.accept();
                    sslClientSocket.startHandshake();

                    Log.d(LOG, "Server accept !!");
                    //addClient(sslClientSocket);

                    inputStream = new DataInputStream(new BufferedInputStream(sslClientSocket.getInputStream()));
                  //  outputStream = new DataOutputStream(new BufferedOutputStream(sslClientSocket.getOutputStream());
                    // create a new thread object
                    Thread t = new ClientHandeler(serverPort, inputStream, outputStream);

                    // Invoking the start() method
                    t.start();

                    Log.d(LOG, "Starting new Thread -");
/*
                    //TODO: send client hello message
                    while(running){
                        if (inputStream != null){
                            String strMessageFromClient = inputStream.readUTF();  //FEIL
                            Log.d(LOG, "Reading message " + strMessageFromClient);

                            TestChatActivity.setChat(strMessageFromClient, "ipv6_other_user");

                        }
                    }*/
                }
            } catch (IOException e) {
                Log.d(LOG, Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();

                Log.d(LOG, "Exception in AppServer in constructor");
            }
            //TODO: close socket


    }


    public static void updateActivity(MainActivity activity) {
        mainActivity = new WeakReference<>(activity);
    }

/*
    protected void addClient(SSLSocket sslClientSocket){
        ConnectedDevice connectedDevice = new ConnectedDevice(this, sslClientSocket);
        //clients.put(connectedDevice.getUserIdentity().getPublicKey(), connectedDevice);
        //clientProcessingPool.submit(connectedDevice);
    }
*/

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


  /*  protected void onPacketReceived(ConnectedDevice device, ReceivedPacket packet){
        Contact from = new Contact(device.getUserIdentity()); // den som sender pakken
        for(ConnectionListener listeners: mainActivity.get().getWifiAwareConnectionManager().getAppClient().getConnectionListeners()){
            listeners.onServerPacket(packet);
        }

        if (packet instanceof MessageObject) { //TODO: Message or MessagePacket?
            PublicKey to = ((MessageObject)packet).getMessage().getTo();

            if(clients.containsKey(to)){
                ConnectedDevice forwardTo = clients.get(to);
                Runnable packetForwardingTask = () -> {
                    forwardTo.sendMessage(packet);
                };
                Thread packetForwardingThred = new Thread(packetForwardingTask);
                packetForwardingThred.start();
            }
        }
    }*/
}
