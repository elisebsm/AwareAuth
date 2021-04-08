package com.example.testaware;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.testaware.listeners.ConnectionListener;
import com.example.testaware.models.AbstractPacket;
import com.example.testaware.models.Contact;
import com.example.testaware.models.Message;
import com.example.testaware.models.MessagePacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import lombok.Getter;

public class ClientHandler extends Thread  {

    private String LOG = "LOG-Test-Aware-Client-handler";
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private boolean running;

    private SSLSocket sslSocket;

    @Getter
    private List<ConnectionListener> connectionListeners;


    public ClientHandler(ObjectInputStream in, ObjectOutputStream out, SSLSocket sslSocket, List<ConnectionListener> listener)  {
        this.in = in;
        this.out = out;
        this.sslSocket = sslSocket;

        TestChatActivity.updateActivityClientHandler(this);
        connectionListeners = listener;
        //connectionListeners.add(listener);
    }


    @Override
    public void run(){
        running = true;
         try {
             while (running) {
                 Log.d(LOG, "inputstream ClientHandler");


                 AbstractPacket abstractPacket = (AbstractPacket) in.readObject();
                 MessagePacket messagePacket = (MessagePacket) abstractPacket;
                 Message message = messagePacket.getMessage() ;

                 Log.d(LOG, message.toString());
                 String plainText = message.getPlaintext(IdentityHandler.getKeyPair().getPrivate());
                 Log.d(LOG, "Plaintext:" +  plainText);
                 onPacket(message);
                 new Handler(Looper.getMainLooper()).post(()-> {
                     TestChatActivity.setChat(message);
                 });
            }

        } catch (IOException | ClassNotFoundException e) {
             e.printStackTrace();
         }
    }

    //TODO: close socket

   /* @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void setOutputStream(String msgToSend){
        try {
            out.writeObject(msgToSend);
            out.flush();
          //TODO: set  ChatActivity.setChat(msgToSend, ChatActivity.getLocalIp());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    private ExecutorService sendService = Executors.newSingleThreadExecutor();

    public boolean sendMessage(Message message){
        if(out == null){
            Log.d(LOG, "outputstream is null");
            return false;
        }
        Runnable sendMessageRunnable = () -> {
            try {
                MessagePacket messagePacket = (new MessagePacket(message));
                Log.d(LOG, "outputstream " + message);
                out.writeObject(messagePacket);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(LOG, "Exception in AppServer  in sendMessage()");
                running = false;
            }
        };
        sendService.submit(sendMessageRunnable);
        return true;
    }

 /*  public boolean sendMessage(String message){
        if(out == null){
            Log.d(LOG, "outputstream is null");
            return false;
        }
        Runnable sendMessageRunnable = () -> {
            try {
                Log.d(LOG, "outputstream " + message);
                out.writeObject(message);
                out.flush();

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

    private void onPacket(Message message) {
        Contact from = new Contact(getServerIdentity());
        Log.d(LOG,   "on packet" + " from " + from.getCommonName());
        for(ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onPacket(message);
/*  TORSDAG       for(ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onPacket(from, packet);
        }*/
        }
    }


}
