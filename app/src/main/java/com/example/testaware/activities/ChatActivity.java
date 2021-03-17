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
import android.widget.TextView;
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
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import lombok.Setter;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class ChatActivity extends AppCompatActivity {
        private Inet6Address peerIpv6;
        private EditText editChatText;
        private String LOG = "LOG-Test-Aware-Chat-Activity";
        private RecyclerView mMessageRecycler;
        public static MessageListAdapter mMessageAdapter;  //endret til test
        public static ArrayList<MessageListItem> messageList;
        private Context context;
        private DataInputStream inputStream;
        private DataOutputStream outputStream;
        private User user;
        private String myIpvAddr;
        private SSLSocket socket2;
        private boolean running;
        private Message message;
        private Socket socket;

    private ConnectivityManager       connectivityManager;
    private NetworkSpecifier networkSpecifier;
    private NetworkCapabilities networkCapabilities;
    private PeerHandle peerHandle;
    private WifiAwareNetworkInfo peerAwareInfo;


    private SSLContext sslContext;
    private KeyPair keyPair;

        private AppClient appClient;
        private AppServer appServer;

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

            this.sslContext = MainActivity.getSslContext();
            //this.sslContext = IdentityHandler.getSSLContext(this.context);
            this.keyPair = IdentityHandler.getKeyPair();
            peerIpv6 = MainActivity.getPeerIpv6();
            TextView textView = findViewById(R.id.tvRole);

            textView.setText("SERVER");
            //AppServer appServer = new AppServer(sslContext, Constants.SERVER_PORT);
            //Log.d(LOG, "SERVER: " + peerIpv6);

            //appClient = new AppClient(keyPair, sslContext);
            //textView.setText("CLIENT");
            //Log.d(LOG, "CLIENT: " + peerIpv6);

            /*if (MainActivity.isPublisher()){
                AppServer appServer = new AppServer(sslContext, Constants.SERVER_PORT);
            } else {
                AppClient appClient = new AppClient(keyPair, sslContext);
            }*/

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
            }*/
            Button sendChatMsgbtn = findViewById(R.id.btnSendChatMsg);
            sendChatMsgbtn.setOnClickListener(v -> {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    EditText messageText = findViewById(R.id.eTChatMsg);
                    String messageToSend = messageText.getText().toString();
                    if(appClient != null){
                        //try {
                            //Message message = new Message(messageToSend);
                            //appClient.sendMessage(message);
                            appClient.sendMessage(messageToSend);
                        } /*catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }*/
                    }
                    Log.i(LOG, "Send btn pressed");
                //}
            });
        }


        public static void setChat(String message){
            MessageListItem chatMsg = new MessageListItem(message, "ipv6_other_user");    //TODO: GET USERNAME FROM CHATLISTITEM
            messageList.add(chatMsg);
            mMessageAdapter.notifyDataSetChanged();
        }


        public void notifyMessageAdapter(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ChatActivity.mMessageAdapter.notifyDataSetChanged();
                }
            });
        }


        public static String getLocalIp() {
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


             /*private void receiveMessage() {
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
        }*/

       /* private void sendMessage(SSLSocket sslSocket) {
            //
            Runnable serverTask = () -> {
                String chatMessage = editChatText.getText().toString();
                //TODO: make Message object for input stream
                try {
                    outputStream = new DataOutputStream(sslSocket.getOutputStream());
                    outputStream.writeUTF(chatMessage);
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
        }*/


    private X509Certificate getPeerCertificate() {
        try {
            Certificate[] certs = socket2.getSession().getPeerCertificates();
            if(certs.length > 0 && certs[0] instanceof X509Certificate) {
                return (X509Certificate) certs[0];

            }
        } catch (SSLPeerUnverifiedException | NullPointerException ignored) {

        }


        return null;
    }
 /*   private X509Certificate chechPeerCertificate(){
            //TODO: chech if peerCert signed by CA, if not send to peerSigner if trusted. Prompt user yes/no. If yes, send to PeerSigner
        return cert;
    }

                          */
}
