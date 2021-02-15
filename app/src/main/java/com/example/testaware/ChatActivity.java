package com.example.testaware;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testaware.adapters.MessageListAdapter;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet6Address;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.Enumeration;


public class ChatActivity extends AppCompatActivity {
    
    private EditText editChatText;

    private String LOG = "LOG-Test-Aware-Chat-Activity";
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;  //endret til test
    private ArrayList<MessageListItem> messageList;
    private String strMessageFromClient;
    private Context context;
    private static Inet6Address peerIpv6;

    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private User user;
    private String myIpvAddr;
    private Socket socket;
    private boolean running;




//client can send messages to server, this works. and other way around
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        this.context = this;
        myIpvAddr= getLocalIp();
        user= new User("Ser", myIpvAddr, true);          //change for every run, just foir testing

        peerIpv6 = MainActivity.getPeerIpv6();  //get peer ip from main

        //Log.i(LOG, "PeerIP"+peerIpv6.toString());

        //otherIpAddr = getIntent().getStringExtra()
        //currentUser = new User ("kirsten",getLocalIpV6(), true); //set new user
        //otherUser = new User("elise", peerIpv6.toString(), false);


        Log.i(LOG, "Chat Activity on create started");
        editChatText = (EditText) findViewById(R.id.eTChatMsg);

        messageList = new ArrayList<>();
        mMessageRecycler = (RecyclerView) findViewById(R.id.recyclerChat);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));

        final Button sendChatMsgbtn = findViewById(R.id.btnSendChatMsg);
        if(user.getName()=="Server") {
            startServer();
            sendChatMsgbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        sendMessage();
                        Log.i(LOG, "send btn pressed");
                    }
                }
            });
        }
        else{
            initClient();
            sendChatMsgbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        sendMessage();
                        Log.i(LOG, "send btn pressed");
                    }
                }
            });
        }

    }


    private void initClient() {
        Runnable serverTask = new Runnable() {  //new thread for each client server conn, yes probably
            @Override
            public void run() {

                //(Inet6Address) getIntent().getExtras().get("IPV6_Address");  //does not work
                int clientPort = (int) getIntent().getExtras().get("Client_port");  //use to get dynamic ports
                running= true;
                //initialize socket and input stream
                socket = null;
                Log.i(LOG, "sendChatMessage started ");
                String chatMessage = editChatText.getText().toString();




                    try {

                        socket = new Socket(peerIpv6, 40699);   //TODO: just testing with port 4069
                      //  if(socket ==null)continue;
                        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                        outputStream = new DataOutputStream(socket.getOutputStream());


                            outputStream.writeUTF("clientHello");
                            outputStream.flush();
                            Log.i(LOG, "ClientHello sent ");

                            MessageListItem chatMsg = new MessageListItem("clientHello", getLocalIp());
                            messageList.add(chatMsg);

                            EditText textT = (EditText) findViewById(R.id.eTChatMsg);
                            textT.getText().clear();




                           if (inputStream!=null){
                                receiveMessage();
                            }

                                                  

 /*
                            Log.d(LOG, "Reading message " + strMessageFromClient);

                            MessageListItem chatMsg = new MessageListItem(strMessageFromClient, "ipv6_other_user");    //TODO: GET USERNAME FROM CHATLISTITEM
                            messageList.add(chatMsg);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mMessageAdapter.notifyDataSetChanged();

                                }
                            });
                        }*/

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
             /*
                if(inputStream!=null){
                    receiveMessage();

                }
                if(outputStream!=null){
                    sendMessage();
                }  */


                }



        };

        Thread serverThread = new Thread(serverTask);
        serverThread.start();

    }
    //this works
    private void receiveMessage(){
        Runnable serverTask = new Runnable() {
            public void run() {
                running = true;

                try {
                    inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    while (running) {
                        strMessageFromClient = (String) inputStream.readUTF();
                        Log.d(LOG, "Reading message " + strMessageFromClient);
                        MessageListItem chatMsg = new MessageListItem(strMessageFromClient, "ipv6_other_user");    //TODO: GET USERNAME FROM CHATLISTITEM
                        messageList.add(chatMsg);
                    }


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMessageAdapter.notifyDataSetChanged();

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            };

            Thread serverThread = new Thread(serverTask);
            serverThread.start();
        }







    //runnable?
    private void sendMessage(){
       Runnable serverTask = new Runnable() {  //
       public void run() {
       String chatMessage = editChatText.getText().toString();
        try {
            outputStream = new DataOutputStream(socket.getOutputStream());



                outputStream.writeUTF(chatMessage);
                outputStream.flush();

                MessageListItem chatMsg = new MessageListItem(chatMessage, getLocalIp());
                messageList.add(chatMsg);

                EditText textT = (EditText) findViewById(R.id.eTChatMsg);
                textT.getText().clear();


        } catch (IOException e) {
            e.printStackTrace();
        }
        }
         };

         Thread serverThread = new Thread(serverTask);
         serverThread.start();

    }




    //only receives messages
    private void startServer(){
        Runnable serverTask = new Runnable(){  //new thread for each client server conn
            @Override
            public void run() {
                //initialize socket and input stream

                ServerSocket server;
              //  socket = null;
                //inputStream = null;
              //  outputStream =null;
                //start server and wait for conn

                try {
                    server = new ServerSocket(40699);

                    int port = 40699;
                    // server.getLocalPort();  //TODO: change to get dynamic ports
                    while (true) {
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

                        strMessageFromClient = (String) inputStream.readUTF();
                        Log.d(LOG, "Reading message from client"+ strMessageFromClient);

                        MessageListItem chatMsg = new MessageListItem(strMessageFromClient, "ipv6_other_user");    //TODO: GET USERNAME FROM CHATLISTITEM
                        messageList.add(chatMsg);

                        if(inputStream!= null){
                            receiveMessage();

                        }



                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                 mMessageAdapter.notifyDataSetChanged();

                            }
                        });

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
/*
                try {

                    outputStream = new DataOutputStream(socket.getOutputStream());
                    if(outputStream!=null) {

                        outputStream.writeUTF(chatMessage);
                        outputStream.flush();

                        MessageListItem chatMsg = new MessageListItem(chatMessage, getLocalIp());
                        messageList.add(chatMsg);

                        EditText textT = (EditText) findViewById(R.id.eTChatMsg);
                        textT.getText().clear();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


                try {
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



    //ipv6
    public String getLocalIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    System.out.println("ip1--:" + inetAddress);
                    System.out.println("ip2--:" + inetAddress.getHostAddress());

                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
                        String ipaddress = inetAddress.getHostAddress();
                        return ipaddress;
                    }


                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }



}
