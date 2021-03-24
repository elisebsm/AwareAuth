package com.example.testaware;

import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.testaware.activities.ChatActivity;

import java.io.BufferedInputStream;
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
    private int serverPort;
    private static ObjectInputStream in;
    private static ObjectOutputStream out;

    public ClientHandeler(int serverPort, ObjectInputStream in, ObjectOutputStream out) {
        this.serverPort = serverPort;
        this.in = in;
        this.out = out;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void run(){
        Boolean running = true;

        while (running) {
            try {
                if (in != null) {
                    String strMessageFromClient = in.readUTF();  //FEIL
                    Log.d(LOG, "Reading message " + strMessageFromClient);
                    TestChatActivity.setChat(strMessageFromClient, "ipv6_other_user");
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //TODO: close socket

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void setOutputStream(String msgToSend){
        try {
            out.writeUTF(msgToSend);
            out.flush();
          //  ChatActivity.setChat(msgToSend, ChatActivity.getLocalIp());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
