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
import com.scaledrone.lib.Room;
import com.scaledrone.lib.RoomListener;
import com.scaledrone.lib.Scaledrone;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    
    private EditText editChatText;
    private String ipv6Address;   //TODO: wrong, is supposed to be Inet6Adress not string
    private int port;

    private String LOG = "LOG-Test-Aware-Chat-Activity";
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private ArrayList<MessageListItem> messageList;
    private String strMessageFromClient;
    private Context context;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        this.context = this;
        Log.i(LOG, "Chat Activity on create started");
        editChatText = (EditText) findViewById(R.id.eTChatMsg);
        ipv6Address = getIntent().getStringExtra("IPV6_Address");

        messageList = new ArrayList<>();
        MessageListItem test = new MessageListItem("hello", "kirsten");
        messageList.add(test);

        mMessageRecycler = (RecyclerView) findViewById(R.id.recyclerChat);

        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));


        startServer();
        Log.i(LOG, "server started ");

        Button sendChatMsgbtn = findViewById(R.id.btnSendChatMsg);
        sendChatMsgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    sendChatMessage();
                    Log.i(LOG, "send btn pressed");
                }
            }
        });
    }






    private void sendChatMessage() {
        Runnable serverTask = new Runnable() {  //new thread for each client server conn, yes probably
            @Override
            public void run() {
                //initialize socket and input stream
                Socket socket = null;
                //DataInputStream in = null;   //dont need it yet
                Log.i(LOG, "sendChatMessage started ");
                String chatMessage = editChatText.getText().toString();
                if (chatMessage.length() >= 0) {
                    DataOutputStream outputStream = null;
                    try {

                        socket = new Socket(ipv6Address, 40699);   //TODO: just testing with port 40699


                        outputStream = new DataOutputStream(socket.getOutputStream());
                        outputStream.writeUTF(chatMessage);
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                MessageListItem chatMsg = new MessageListItem(chatMessage,"kirsten");
                messageList.add(chatMsg);

                //client send message here
                EditText textT = (EditText) findViewById(R.id.eTChatMsg);
                textT.getText().clear();//TODO: SAVE MESSAGE

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMessageAdapter.notifyDataSetChanged();

                    }
                });

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
                Inet6Address peerIpv6 = (Inet6Address) getIntent().getExtras().get("IPV6_Address");
                int clientPort = (int) getIntent().getExtras().get("Client_port");
                ServerSocket server;
                Socket socket = null;
                DataInputStream inputStream = null;
                //start server and wait for conn
                try {
                    server = new ServerSocket(40699);
                    Log.i(LOG, "server started ");
                    int port = 40699;
                    // server.getLocalPort();
                    Log.d(LOG, String.valueOf(port));

                    //TODOO: set port correctly
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

                        MessageListItem chatMsg = new MessageListItem(strMessageFromClient,"kirsten");
                        messageList.add(chatMsg);

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
