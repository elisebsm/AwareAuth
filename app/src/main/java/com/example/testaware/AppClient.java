package com.example.testaware;


import android.os.Build;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import com.example.testaware.activities.ChatActivity;
import com.example.testaware.activities.MainActivity;
import com.example.testaware.listitems.MessageListItem;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import lombok.Getter;

public class AppClient implements Runnable{

    private boolean running;
    @Getter
    private SSLSocket sslSocket;
    private SSLContext sslContext;

    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private ExecutorService sendService = Executors.newSingleThreadExecutor();
    @Getter
    //private List<ConnectionListener> connectionListeners;

    private String LOG = "LOG-Test-Aware-Client";
    @Getter
    private KeyPair keyPair;

    private Inet6Address inet6Address;

    public AppClient(KeyPair keyPair, SSLContext sslContext){
        this.keyPair = keyPair;
        this.sslContext = sslContext;
        Thread thread = new Thread(this);
        thread.start();
        //connectionListeners = new ArrayList<>();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void run() {
        //int clientPort = (int) getIntent().getExtras().get("Client_port");
        running = true;
        Log.d(LOG, "running: true");
        sslSocket = null;
        this.inet6Address = MainActivity.getPeerIpv6();
        Log.d(LOG, "Peer ipvg: " + inet6Address);
        /*try {
            this.inet6Address = (Inet6Address) Inet6Address.getLocalHost();
        } catch (UnknownHostException e) {
            this.inet6Address = (Inet6Address) Inet6Address.getLoopbackAddress();
        }*/

        Log.d(LOG, "Trying to init");
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        try {
            while(running){
                if(inet6Address != null){
                    sslSocket = (SSLSocket) socketFactory.createSocket(inet6Address, Constants.SERVER_PORT);

                    Log.d(LOG, "Connected to " + inet6Address.getHostName());
                } else {
                    Log.d(LOG, "Trying to create Socket but inte6Adrres is NULL");
                }

            /*for(ConnectionListener listener: connectionListeners){
                listener.onConnect();
            }*/
                //inputStream = new ObjectInputStream(new BufferedInputStream(sslSocket.getInputStream()));


                outputStream = new DataOutputStream(sslSocket.getOutputStream());
                inputStream = new DataInputStream(sslSocket.getInputStream());
                Log.d(LOG, "inputstream created");
                outputStream.writeUTF("clientHello");
                outputStream.flush();
                //TODO: send client hello message
                while(running){
                    if (inputStream != null){
                        String strMessageFromClient = (String) inputStream.readUTF();   //FEIL
                        Log.d(LOG, "Reading message " + strMessageFromClient);
                        ChatActivity.setChat(strMessageFromClient);
                        //ReceivedPacket receivedPacket = (ReceivedPacket) inputStream.readObject();
                        //onPacketReceived(receivedPacket);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(LOG, "Exception in Appclient  in run()");
            /*for (ConnectionListener connectionListener: connectionListeners){
                connectionListener.onDisconnect();
            }*/
            if(sslSocket != null){
                try {
                    sslSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private X509Certificate getPeerIdentity()  {
        Certificate[] certificates = new Certificate[0];
        try {
            certificates = sslSocket.getSession().getPeerCertificates();
        } catch (SSLPeerUnverifiedException e) {
            e.printStackTrace();
        }
        if (certificates.length > 0 && certificates[0] instanceof X509Certificate){
            return (X509Certificate) certificates[0];
        }
        return null;
    }


    private ArrayList<MessageListItem> messageList;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean sendMessage(String message){
        if(outputStream == null){
            Log.d(LOG, "outputstream is null");
            return false;
        }
        Runnable sendMessageRunnable = () -> {
            try {
                Log.d(LOG, "outputstream send message runnable");
                outputStream.writeUTF(message);
                outputStream.flush();

                MessageListItem chatMsg = new MessageListItem(message, ChatActivity.getLocalIp()); //TODO
                ChatActivity.messageList.add(chatMsg);

                //EditText textT = (EditText) findViewById(R.id.eTChatMsg);
                //textT.getText().clear();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(LOG, "Exception in Appclient  in sendMessage()");
            }
        };
        sendService.submit(sendMessageRunnable);
        return true;
    }

   /* public void onPacketReceived(ReceivedPacket packet){
        Contact from = new Contact(getPeerIdentity());
        for(ConnectionListener connectionListener: connectionListeners){
            connectionListener.onReceivedPacket(from, packet);
        }
    }

    void registerConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }*/
}


