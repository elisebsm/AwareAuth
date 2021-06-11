package com.example.testaware;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.testaware.activities.ChatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLPeerUnverifiedException;
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

        ChatActivity.updateActivityClientHandler(this);

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

                 sendMessage("Hei!!", 0);

                 new Handler(Looper.getMainLooper()).post(()-> {
                     ChatActivity.setChat(message);
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
                out.writeUTF(message);
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


}
