package com.example.testaware;

import com.example.testaware.activities.MainActivity;
import com.example.testaware.adapters.MessageListAdapter;
import com.example.testaware.listitems.MessageListItem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Inet6Address;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;


//client can also instantiate connection.
//implements runnable in order to be extecuted by a thread. must implement run(). Intended for objects that need to execute code while they are active.
public class AppServer {


    private static Inet6Address peerIpv6;
    private static WeakReference<MainActivity> mainActivity; //TODO what is this??
    private String LOG = "LOG-Test-Aware-App-Client";
    private MessageListAdapter mMessageAdapter;  //endret til test
    private ArrayList<MessageListItem> messageList;
    private String strMessageFromClient;
    private SSLContext sslContext;
    private DataInputStream inputStream;  //TODO: change to objectinputstream??
    private DataOutputStream outputStream;   //TODO: use so client can also send messages
    private boolean running;
    private Map<PublicKey, ConnectedDevice> clients;

    private final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);



    public AppServer(SSLContext serverSSLContext, int serverPort){
        //this.sslContext = serverSSLContext;
        //this.peerIpv6 = peerIpv6;

        running = true;

        Runnable serverTask = () -> {  //new thread for each client server conn
            SSLServerSocket serverSocket;
            running  = true;

            try {
                serverSocket = (SSLServerSocket) serverSSLContext.getServerSocketFactory().createServerSocket(serverPort);
                serverSocket.setNeedClientAuth(true);

                while (running) {
                    SSLSocket sslClientSocket = (SSLSocket) serverSocket.accept();
                    addClient(sslClientSocket);
                }
            } catch (IOException  e) {
                e.printStackTrace();
            }
            //TODO: close socket
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }


    public static void updateActivity(MainActivity activity) {
        mainActivity = new WeakReference<>(activity);
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


    protected void onPacketReceived(ConnectedDevice device, ReceivedPacket packet){
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
    }
}
