package com.example.testaware;


import android.util.Log;

import com.example.testaware.activities.MainActivity;

import java.io.BufferedInputStream;
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

    private ObjectInputStream inputStream;
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

    @Override
    public void run() {
        //int clientPort = (int) getIntent().getExtras().get("Client_port");
        running = true;
        Log.d(LOG, "running: true");
        sslSocket = null;
        this.inet6Address = MainActivity.getPeerIpv6();
        /*try {
            this.inet6Address = (Inet6Address) Inet6Address.getLocalHost();
        } catch (UnknownHostException e) {
            this.inet6Address = (Inet6Address) Inet6Address.getLoopbackAddress();
        }*/
        try {
            Log.d(LOG, "Trying to init");
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            sslSocket = (SSLSocket) socketFactory.createSocket(inet6Address, Constants.SERVER_PORT);
            //SSLSession sslSession = sslSocket.getSession();
            /*for(ConnectionListener listener: connectionListeners){
                listener.onConnect();
            }*/
            inputStream = new ObjectInputStream(new BufferedInputStream(sslSocket.getInputStream()));
            outputStream = new DataOutputStream(sslSocket.getOutputStream());
            outputStream.writeUTF("clientHello");
            outputStream.flush();
            //TODO: send client hello message
            while(running){
//                if (inputStream != null){
//                    ReceivedPacket receivedPacket = (ReceivedPacket) inputStream.readObject();
//                    onPacketReceived(receivedPacket);
//                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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


    private boolean sendMessage(final Message message){
        if(outputStream == null){
            return false;
        }
        Runnable sendMessageRunnable = () -> {
            try {
                outputStream.writeUTF(String.valueOf(message)); //TODO: use it when button "send" is pressed?
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
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
