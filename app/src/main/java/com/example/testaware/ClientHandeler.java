package com.example.testaware;

import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.RequiresApi;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import lombok.Getter;

public class ClientHandeler extends Thread{

    private String LOG = "LOG-Test-Aware-Client-handeler";
    private static ObjectInputStream in;
    private static ObjectOutputStream out;

    private boolean running;

    public ClientHandeler(ObjectInputStream in, ObjectOutputStream out) {
        this.in = in;
        this.out = out;

        TestChatActivity.updateActivityClientHandler(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void run(){
        running = true;
        Log.d(LOG, "Clienthandeler running");
         try {
             while (running) {
                    Object messageFromClient = in.readObject();
                    Log.d(LOG, "Reading message " + messageFromClient);
                    //TestChatActivity.setChat(strMessageFromClient, "ipv6_other_user");
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

   public boolean sendMessage(String message){
        if(out == null){
            Log.d(LOG, "outputstream is null");
            return false;
        }
        Runnable sendMessageRunnable = () -> {
            try {
                Log.d(LOG, "outputstream " + message);
                out.writeObject(message);
                out.flush();

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
}
