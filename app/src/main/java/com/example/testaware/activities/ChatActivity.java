package com.example.testaware.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.aware.PeerHandle;
import android.net.wifi.aware.PublishDiscoverySession;
import android.net.wifi.aware.SubscribeDiscoverySession;
import android.net.wifi.aware.WifiAwareNetworkInfo;
import android.net.wifi.aware.WifiAwareNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testaware.AppClient;
import com.example.testaware.AppServer;
import com.example.testaware.Constants;
import com.example.testaware.IdentityHandler;
import com.example.testaware.Message;
import com.example.testaware.listitems.MessageListItem;
import com.example.testaware.R;
import com.example.testaware.User;
import com.example.testaware.adapters.MessageListAdapter;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet6Address;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class ChatActivity extends AppCompatActivity {
        private Inet6Address peerIpv6;
        private EditText editChatText;
        private String LOG = "LOG-Test-Aware-Chat-Activity";
        private RecyclerView mMessageRecycler;
        private MessageListAdapter mMessageAdapter;  //endret til test
        private ArrayList<MessageListItem> messageList;
        private String messageFromClient;
        private Context context;
        private DataInputStream inputStream;
        private DataOutputStream outputStream;
        private User user;
        private String myIpvAddr;
        private SSLSocket socket2;
        private boolean running;
        private Message message;
        private AppServer appServer;
        private Socket socket;

    private ConnectivityManager       connectivityManager;
    private NetworkSpecifier networkSpecifier;
    private NetworkCapabilities networkCapabilities;
    private PeerHandle peerHandle;
    private WifiAwareNetworkInfo peerAwareInfo;


    private SSLContext sslContext;
    private KeyPair keyPair;

    //TODO: change to get dynamic ports

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chat);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            this.context = this;
            setupUI();

            myIpvAddr = getLocalIp();

            user = new User("Server", myIpvAddr, true); //TODO: decide who will be server and client

            this.sslContext = IdentityHandler.getSSLContext(this.context);
            this.keyPair = IdentityHandler.getKeyPair(this.context);


            AppServer appServer = new AppServer(sslContext, Constants.SERVER_PORT);
            //AppClient appClient = new AppClient(keyPair, sslContext);

            /*if (MainActivity.isPublisher()){
                AppServer appServer = new AppServer(sslContext, Constants.SERVER_PORT);
            } else {
                AppClient appClient = new AppClient(keyPair, sslContext);
            }*/

            //peerIpv6 = MainActivity.getPeerIpv6();
            //publishDiscoverySession = MainActivity.getPublishDiscoverySession();
            //subscribeDiscoverySession = MainActivity.getSubscribeDiscoverySession();


            int position = getIntent().getIntExtra("position", 0);
            //List<PeerHandle> peerHandleList = MainActivity.getPeerHandleList();
            //peerHandle = peerHandleList.get(position);


            //requestWiFiConnection();


        }

        private void setupUI(){
            editChatText = findViewById(R.id.eTChatMsg);
            messageList = new ArrayList<>();
            mMessageRecycler = findViewById(R.id.recyclerChat);
            mMessageAdapter = new MessageListAdapter(this, messageList);
            mMessageRecycler.setAdapter(mMessageAdapter);
            mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));

            /*final Button sendChatMsgbtn = findViewById(R.id.btnSendChatMsg);
            if (MainActivity.isPublisher()) {
                startServer();
            } else {
                initClient();
            }
            sendChatMsgbtn.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    sendMessage();
                    Log.i(LOG, "send btn pressed");
                }
            });*/
        }

        private void initClient() {
            Runnable serverTask = () -> {
                //int clientPort = (int) getIntent().getExtras().get("Client_port");
                running = true;

                socket = null;
                Log.i(LOG, "sendChatMessage started ");
                String chatMessage = editChatText.getText().toString();
                try {
                    SocketFactory socketFactory = SSLSocketFactory.getDefault();
                    socket = socketFactory.createSocket(peerIpv6, Constants.SERVER_PORT);
                    //SSLSession sslSession = socket.getSession();

                    //socket = new Socket(peerIpv6, Constants.SERVER_PORT);
                    inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    outputStream = new DataOutputStream(socket.getOutputStream());
                    outputStream.writeUTF("clientHello");
                    outputStream.flush();
                    Log.i(LOG, "ClientHello sent ");

                    MessageListItem chatMsg = new MessageListItem("clientHello", getLocalIp());
                    messageList.add(chatMsg);

                    EditText textT = (EditText) findViewById(R.id.eTChatMsg);
                    textT.getText().clear();

                    if (inputStream != null) {
                        receiveMessage();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            Thread serverThread = new Thread(serverTask);
            serverThread.start();
        }


        private void receiveMessage() {
            Runnable serverTask = new Runnable() {
                public void run() {
                    running = true;
                    try {
                        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                        while (running) {
                            messageFromClient = inputStream.readUTF();
                            //String cipherTextFromClient = (String) inputStream.readUTF();
                            //messageFromClient = message.decryptMessage(cipherTextFromClient.getBytes());
                            Log.d(LOG, "Reading message " + messageFromClient);
                            MessageListItem chatMsg = new MessageListItem(messageFromClient, "ipv6_other_user");    //TODO: GET USERNAME FROM CHATLISTITEM
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

        private void sendMessage() {
            //
            Runnable serverTask = () -> {
                String chatMessage = editChatText.getText().toString();
                //TODO: make Message object for input stream
                try {
                    outputStream = new DataOutputStream(socket.getOutputStream());
                    outputStream.writeUTF(String.valueOf(chatMessage));
                    outputStream.flush();
                    MessageListItem chatMsg = new MessageListItem("Test:" + chatMessage , getLocalIp());
                    messageList.add(chatMsg);
                    EditText textT = findViewById(R.id.eTChatMsg);
                    textT.getText().clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            Thread serverThread = new Thread(serverTask);
            serverThread.start();
        }

        private void startServer() {
            //new thread for each client server conn
            //TODO: close socket
            Runnable serverTask = () -> {
                ServerSocket server;
                try {
                    server = new ServerSocket(40699);
                    int port = 40699;
                    // server.getLocalPort();
                    while (true) {
                        Log.d(LOG, "Server started, Waiting for client");
                        socket = server.accept();
                        Log.d(LOG, "Client accepted");
                        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                        String strMessageFromClient = inputStream.readUTF();
                        Log.d(LOG, "Reading message from client"+ strMessageFromClient);

                        MessageListItem chatMsg = new MessageListItem(strMessageFromClient, "ipv6_other_user");    //TODO: GET USERNAME FROM CHATLISTITEM
                        messageList.add(chatMsg);

                        if(inputStream!= null){
                            receiveMessage();
                        }
                        runOnUiThread(() -> mMessageAdapter.notifyDataSetChanged());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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
