package com.example.testaware.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.example.testaware.R;



import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testaware.User;
import com.example.testaware.adapters.MessageListAdapter;
import com.example.testaware.listitems.MessageListItem;


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

    private boolean isClientRunning;
    private boolean isFirstMessage;
    private ServerSocket server;
    private int clientPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        this.context = this;


        myIpvAddr = getLocalIp();
        user = new User("Server", myIpvAddr, true);          //change for every run, just foir testing

        peerIpv6 = MainActivity.getPeerIpv6();  //get peer ip from main
        clientPort = (int) getIntent().getExtras().get("Client_port");       //     TODO: use this

        Log.i(LOG, "Chat Activit()create started");
        editChatText = (EditText) findViewById(R.id.eTChatMsg);

        messageList = new ArrayList<>();
        mMessageRecycler = (RecyclerView) findViewById(R.id.recyclerChat);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));

        final Button sendChatMsgbtn = findViewById(R.id.btnSendChatMsg);

        isFirstMessage = true;
        startServer(); //TODO: starte i main
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
    private void clientHello() {
        Runnable clientTask = new Runnable() {  //new thread for each client server conn, yes probably
            @Override
            public void run() {
                running= true;

                socket = null;
                Log.i(LOG, "sendChatMessage started ");
                String chatMessage = editChatText.getText().toString();

                try {

                    socket = new Socket(peerIpv6, 40699);   //TODO: just testing with port 4069
                    inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    outputStream = new DataOutputStream(socket.getOutputStream());
                    outputStream.writeUTF("clientHello");
                    outputStream.flush();
                    Log.i(LOG, "ClientHello sent ");

                    MessageListItem chatMsg= new MessageListItem("clientHello", getLocalIp());
                    messageList.add(chatMsg);

                    EditText textT = (EditText) findViewById(R.id.eTChatMsg);
                    textT.getText().clear();

                    if (inputStream!=null){
                        receiveMessage();
                    }

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

        Thread clientThread = new Thread(clientTask);
        clientThread.start();

    }

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

    private void sendMessage(){
        Runnable serverTask = new Runnable() {  //
            public void run() {
                if(isFirstMessage && messageList.isEmpty()) { //Have not received or sent any messages
                    clientHello();  //TODO: fikse sånn at ikke begge blir klient, ha en send message for client og en for server mtp port også
                    isFirstMessage = false;
                }

                String chatMessage = editChatText.getText().toString();
                try {
                    socket = new Socket(peerIpv6, 40699);
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

    private void startServer(){
        Runnable serverTask = new Runnable(){  //new thread for each client server conn
            @Override
            public void run() {

                //  socket = null;
                //inputStream = null;
                //  outputStream =null;
                //start server and wait for conn

                try {
                    server = new ServerSocket(40698);

                    //int port = 40699;
                    while (true) {
                        Log.d(LOG, "Server started, Waiting for client");
                        socket = server.accept();
                        Log.d(LOG, "Client accepted");
                        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                        outputStream = new DataOutputStream(socket.getOutputStream());

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
