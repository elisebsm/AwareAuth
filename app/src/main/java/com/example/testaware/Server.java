package com.example.testaware;


import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.testaware.activities.MainActivity;
import com.example.testaware.listeners.ConnectionListener;
import com.example.testaware.models.AbstractPacket;
import com.example.testaware.models.Contact;
import com.example.testaware.models.Message;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Inet6Address;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import lombok.Getter;

public class Server implements Runnable{

    private boolean running;
    @Getter
    private SSLSocket sslSocket;
    private SSLContext sslContext;

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private ExecutorService sendService = Executors.newSingleThreadExecutor();

    private String LOG = "LOG-Test-Aware-Client";
    @Getter
    private KeyPair keyPair;

    private Inet6Address inet6Address;

    public Server(KeyPair keyPair, SSLContext sslContext){
        this.keyPair = keyPair;
        this.sslContext = sslContext;
        this.inet6Address = MainActivity.getPeerIpv6();

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


    @RequiresApi(api = Build.VERSION_CODES.Q)
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


    private void receivedMessage(AbstractPacket packet) {
        Contact from = new Contact(getServerIdentity());
        Log.d(LOG, packet.getClass().getSimpleName() + " from " + from.getCommonName());
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void run() {
        running = true;
        sslSocket = null;

        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        try {
            while(running){
                sslSocket = (SSLSocket) socketFactory.createSocket(inet6Address, Constants.SERVER_PORT);

                outputStream = new ObjectOutputStream(sslSocket.getOutputStream());
                inputStream = new ObjectInputStream(new BufferedInputStream (sslSocket.getInputStream())); //FEIL java.io.EOFException
                outputStream.writeUTF("clientHello");
                outputStream.flush();

                while(running){
                    if (inputStream != null){
                        byte [] input = (byte[]) inputStream.readObject();
                        receivedMessage(input);

                        String strMessageFromClient = (String) inputStream.readObject();   //FEIL
                        Log.d(LOG, "Reading message " + strMessageFromClient);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Log.d(LOG, "Exception in Appclient  in run()");

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

}


