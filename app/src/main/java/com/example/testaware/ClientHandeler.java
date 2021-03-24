package com.example.testaware;

import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.testaware.activities.ChatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

public class ClientHandeler extends Thread{

    private String LOG = "LOG-Test-Aware-Client-handeler";
    private static ObjectInputStream in;
    private static ObjectOutputStream out;

    public ClientHandeler(ObjectInputStream in, ObjectOutputStream out) {
        this.in = in;
        this.out = out;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void run(){
        boolean running = true;

            Object strMessageFromClient = null;
            try {
                while (running) {
                    strMessageFromClient = in.readObject();
                    Log.d(LOG, "Reading message " + strMessageFromClient);
                    //TODO: set ChatActivity.setChat(msgToSend, "ipv6_other_user");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

    }
    //TODO: close socket

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void setOutputStream(String msgToSend){
        try {
            out.writeUTF(msgToSend);
            out.flush();
          //TODO: set  ChatActivity.setChat(msgToSend, ChatActivity.getLocalIp());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
