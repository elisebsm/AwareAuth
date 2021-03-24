package com.example.testaware;


import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.testaware.activities.MainActivity;
import com.example.testaware.listeners.ConnectionListener;
import com.example.testaware.listitems.MessageListItem;
import com.example.testaware.models.AbstractPacket;
import com.example.testaware.models.Contact;
import com.example.testaware.models.Message;
import com.example.testaware.models.MessagePacket;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Inet6Address;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
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

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private ExecutorService sendService = Executors.newSingleThreadExecutor();

    @Getter
    private List<ConnectionListener> connectionListeners;

    private String LOG = "LOG-Test-Aware-Client";
    @Getter
    private KeyPair keyPair;

    private Inet6Address inet6Address;

    private int port;

    public AppClient(KeyPair keyPair, SSLContext sslContext){
        this.keyPair = keyPair;
        this.sslContext = sslContext;
        this.inet6Address = MainActivity.getPeerIpv6();

        //Thread thread = new Thread(this);
        //thread.start();

        connectionListeners = new ArrayList<>();
    }


    private X509Certificate getServerIdentity() {
        try {
            Certificate[] certs = sslSocket.getSession().getPeerCertificates();
            if(certs.length > 0 && certs[0] instanceof X509Certificate) {
                return (X509Certificate) certs[0];
            }
        } catch (SSLPeerUnverifiedException | NullPointerException ignored) {
            ignored.printStackTrace();

        }
        return null;
    }

    /*boolean send(AbstractPacket packet) {
        if (outputStream == null) return false;
        Runnable runnable = () -> {
            try {
                outputStream.writeObject(packet);
                outputStream.flush();
            } catch (IOException e) {
                Log.e(LOG, e.getMessage());
                running = false;
            }
        };

        sendService.submit(runnable);
        return true;
    }*/


   /* @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean sendMessage(Message message){
        if(outputStream == null){
            Log.d(LOG, "outputstream is null");
            return false;
        }
        Runnable sendMessageRunnable = () -> {
            try {
                Log.d(LOG, "outputstream " + message);
                outputStream.writeObject(message);
                outputStream.flush();

                *//*MessageListItem chatMsg = new MessageListItem(message, ChatActivity.getLocalIp()); //TODO
                ChatActivity.messageList.add(chatMsg);

                //EditText textT = (EditText) findViewById(R.id.eTChatMsg);
                //textT.getText().clear();*//*
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(LOG, "Exception in Appclient  in sendMessage()");
                running = false;
            }
        };
        sendService.submit(sendMessageRunnable);
        return true;
    }*/

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean sendMessage(String message){
        if(outputStream == null){
            Log.d(LOG, "outputstream is null");
            return false;
        }
        Runnable sendMessageRunnable = () -> {
            try {
                Log.d(LOG, "outputstream " + message);
                outputStream.writeObject(message);
                outputStream.flush();

                /*MessageListItem chatMsg = new MessageListItem(message, ChatActivity.getLocalIp()); //TODO
                ChatActivity.messageList.add(chatMsg);

                //EditText textT = (EditText) findViewById(R.id.eTChatMsg);
                //textT.getText().clear();*/
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(LOG, "Exception in Appclient  in sendMessage()");
                running = false;
            }
        };
        sendService.submit(sendMessageRunnable);
        return true;
    }


    private void onPacketReceived(AbstractPacket packet) {
        Contact from = new Contact(getServerIdentity());
        Log.d(LOG, packet.getClass().getSimpleName() + " from " + from.getCommonName());
        for(ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onPacket(from, packet);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void run() {
        running = true;
        sslSocket = null;

        this.port = Constants.SERVER_PORT;

        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        try {
            while(running){
                sslSocket = (SSLSocket) socketFactory.createSocket(inet6Address, Constants.SERVER_PORT);

            for(ConnectionListener listener: connectionListeners){
                listener.onConnect();
            }
                outputStream = new ObjectOutputStream(sslSocket.getOutputStream());
                inputStream = new ObjectInputStream(new BufferedInputStream (sslSocket.getInputStream())); //FEIL java.io.EOFException
                outputStream.writeUTF("clientHello");
                outputStream.flush();

                while(running){
                    if (inputStream != null){

                        AbstractPacket abstractPacket = (AbstractPacket) inputStream.readObject();
                        onPacketReceived(abstractPacket);


                        String strMessageFromClient = (String) inputStream.readObject();   //FEIL
                        Log.d(LOG, "Reading message " + strMessageFromClient);
                        //ChatActivity.setChat(strMessageFromClient);
                        AbstractPacket receivedPacket = (AbstractPacket) inputStream.readObject();
                        onPacketReceived(receivedPacket);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Log.d(LOG, "Exception in Appclient  in run()");
            for (ConnectionListener connectionListener: connectionListeners){
                connectionListener.onDisconnect();
            }
            if(sslSocket != null){
                try {
                    sslSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                //SystemClock.sleep(2000);
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



    void registerConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }
}


