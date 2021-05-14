package com.example.testaware;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.testaware.activities.MainActivity;
import com.example.testaware.models.AbstractPacket;
import com.example.testaware.models.Contact;
import com.example.testaware.models.Message;
import com.example.testaware.models.MessagePacket;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
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
import lombok.Setter;

import static java.lang.System.currentTimeMillis;

public class ClientHandler extends Thread  {

    private String LOG = "Log-Client-handler";
    private DataInputStream in;
    private DataOutputStream out;

    @Getter
    @Setter
    private boolean running;

    private SSLSocket sslSocket;



    private int counterValue;

    public ClientHandler(DataInputStream in, DataOutputStream out, SSLSocket sslSocket, int counterValue)  {
        this.in = in;
        this.out = out;
        this.sslSocket = sslSocket;
        this.counterValue = counterValue;

        TestChatActivity.updateActivityClientHandler(this);

        //connectionListeners.add(listener);
    }


    @Override
    public void run(){
        running = true;
         try {
             while (running) {
                 Log.d(LOG, "inputstream ClientHandler");


                 /*AbstractPacket abstractPacket = (AbstractPacket) in.readObject();
                 MessagePacket messagePacket = (MessagePacket) abstractPacket;
                 Message message = messagePacket.getMessage() ;
                 String plainText = message.getPlaintext(IdentityHandler.getKeyPair().getPrivate());
                 Log.d(LOG, "Plaintext:" +  plainText);*/

                 String message = in.readUTF();
                 long readinMessageAtServer = currentTimeMillis();
                 Log.d("TESTING-LOG-TIME-TLS-MESSAGE-INPUTSTREAM-SERVER",  String.valueOf(readinMessageAtServer));
                 Log.d(LOG, message);

                 new Handler(Looper.getMainLooper()).post(()-> {
                     TestChatActivity.setChat(message, counterValue);
                 });
            }

        } catch (IOException e) {
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

    public boolean sendMessage(String message, long sendingMessageTime){
        if(out == null){
            Log.d(LOG, "outputstream is null");
            return false;
        }
        Runnable sendMessageRunnable = () -> {
            try {
                /*MessagePacket messagePacket = (new MessagePacket(message));
                Log.d(LOG, "outputstream " + message);
                out.writeObject(messagePacket);
                out.flush();
                Log.d(LOG, "I just flushed");*/

                out.writeUTF(message);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(LOG, "Exception in AppServer  in sendMessage()");
                running = false;
            }
        };
        sendService.submit(sendMessageRunnable);

        BufferedWriter writer = null;
        try {
            String outputText = String.valueOf(sendingMessageTime);
            writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/messageSentServer", true));
            writer.append(outputText);
            writer.append("\n");
            writer.append("----------------------------------------------------");
            writer.append("\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
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
}
