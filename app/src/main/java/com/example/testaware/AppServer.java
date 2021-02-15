package com.example.testaware;

import android.content.Context;
import android.util.Log;

import com.example.testaware.adapters.MessageListAdapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


//client can also instantiate connection.
//implements runnable in order to be extecuted by a thread. must implement run(). Intended for objects that need to execute code while they are active.
public class AppServer {


    private String LOG = "LOG-Test-Aware-App-Client";

    private MessageListAdapter mMessageAdapter;  //endret til test
    private ArrayList<MessageListItem> messageList;
    private String strMessageFromClient;
    private Context context;
    private static Inet6Address peerIpv6;
    private DataInputStream inputStream;  //TODO: change to objectinputstream??
    private DataOutputStream outputStream;   //TODO: use so client can also send messages

    private boolean running;

    private AppServer(Context context, Inet6Address peerIpv6){
        this.context=context;
        this.peerIpv6=peerIpv6;
    }


    //only receives messages
    private void startServer(){
        Runnable serverTask = new Runnable(){  //new thread for each client server conn
            @Override
            public void run() {
                //initialize socket and input stream
                running= true;
                ServerSocket server;
                Socket socket = null;
                DataInputStream inputStream = null;
                //start server and wait for conn
                try {
                    server = new ServerSocket(40699);

                    int port = 40699;
                    // server.getLocalPort();  //TODO: change to get dynamic ports
                    while (running) {
                        /*
                        portOnSystem = portToBytes(port);   //get port set by server, and send it to the client (publisher or subscriber)
                        if (publishDiscoverySession != null && peerHandle != null) { //client can either publisher or subscriber
                            publishDiscoverySession.sendMessage(peerHandle, MAC_ADDRESS_MESSAGE, portOnSystem);
                        } else if (subscribeDiscoverySession != null && peerHandle != null)  {
                            subscribeDiscoverySession.sendMessage(peerHandle, MAC_ADDRESS_MESSAGE, portOnSystem);
                        }
                        */
                        Log.d(LOG, "Server started, Waiting for client");
                        socket = server.accept();
                        Log.d(LOG, "Client accepted");
                        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                        outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                        strMessageFromClient = (String) inputStream.readUTF();
                        Log.d(LOG, "Reading message from client"+ strMessageFromClient);

                        MessageListItem chatMsg = new MessageListItem(strMessageFromClient, "ipv6_other_user");    //TODO: GET USERNAME FROM CHATLISTITEM
                        messageList.add(chatMsg);

                        //UI thread??
                        mMessageAdapter.notifyDataSetChanged();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                /*


                 */
                /*try {
                    socket.close();
                    in.close();
                    Log.d(LOG, "Closing conn");
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        };

        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }


}
