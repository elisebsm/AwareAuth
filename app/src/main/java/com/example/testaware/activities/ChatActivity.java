package com.example.testaware.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testaware.AppClient;
import com.example.testaware.AppServer;
import com.example.testaware.ClientHandler;
import com.example.testaware.adapters.MessageListAdapter;

import com.example.testaware.listeners.SSLContextedObserver;
import com.example.testaware.listitems.MessageListItem;
import com.example.testaware.offlineAuth.PeerAuthServer;


import java.lang.ref.WeakReference;
import com.example.testaware.R;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.net.ssl.SSLContext;

import lombok.Getter;

import static java.lang.System.currentTimeMillis;

public class ChatActivity extends AppCompatActivity {

    private String LOG = "LOG-Test-Aware-Test-Chat-Activity";
    public static MessageListAdapter mMessageAdapter;

    public static ArrayList<MessageListItem> messageList;
    @Getter
    private Context context;

    @Getter
    private AppClient appClient;

    private AppServer appServer;
    private PeerAuthServer peerAuthServer;


    private static WeakReference<MainActivity> mainActivity;
    public static void updateActivityMain(MainActivity activity) {
        mainActivity = new WeakReference<>(activity);
    }

    private static WeakReference<ClientHandler> clientHandlerWeakReference;
    public static void updateActivityClientHandler(ClientHandler activity) {
        clientHandlerWeakReference = new WeakReference<>(activity);
    }

    private String role;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.context = this;

        int port = getIntent().getIntExtra("port", 1025);

        role = getIntent().getStringExtra("Role");
        int counterValue = getIntent().getIntExtra("counter", 0);
        TextView textView = findViewById(R.id.tvRole);
        textView.setText(role);

        SSLContextedObserver sslContextedObserver = mainActivity.get().getSslContextedObserver();
        SSLContext sslContext = sslContextedObserver.getSslContext();


        KeyPair keyPair = mainActivity.get().getKeyPair();

        if(role.equals("Client")){

            long clientStarted = currentTimeMillis();
            Log.d("TESTING-LOG-TIME-TLS-CLIENT-STARTED",  String.valueOf(clientStarted));
            //20.05        appClient = new AppClient(keyPair, sslContext, port, clientStarted, counterValue);
            appClient = new AppClient(keyPair, sslContext, 1025, clientStarted, counterValue);


            Log.d(LOG, "Port: " + port);

            Thread thread = new Thread(appClient);
            thread.start();

        }

        this.appServer  = mainActivity.get().getAppServer();
        this.peerAuthServer = mainActivity.get().getPeerAuthServer(); //TODO:


        setupUI();


    }

    private void setupUI(){
        EditText editChatText = findViewById(R.id.eTChatMsg);
        messageList = new ArrayList<>();
        RecyclerView mMessageRecycler = findViewById(R.id.recyclerChat);
        editChatText = findViewById(R.id.eTChatMsg);
        messageList = new ArrayList<>();
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));


        Button sendChatMsgbtn = findViewById(R.id.btnSendChatMsg);
        sendChatMsgbtn.setOnClickListener(v -> {
            long sendButtonPressed = currentTimeMillis();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                EditText messageText = findViewById(R.id.eTChatMsg);
                String messageToSend = messageText.getText().toString();
                sendMessage(messageToSend, sendButtonPressed);
                messageText.getText().clear();
            }
            Log.i(LOG, "Send btn pressed");
        });
    }




    public static void setChat(String message){
        MessageListItem chatMsg = new MessageListItem(message, "User2");
        mMessageAdapter.notifyDataSetChanged();
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



    private void sendMessage(String msg, long sendButtonPressed) {

        long sendingMessage = currentTimeMillis();
        Log.d("TESTING-LOG-TIME-TLS-SEND-MESSAGE-PRESSED",  String.valueOf(sendingMessage));

        Log.d(LOG, "Sending message: " + msg);
        MessageListItem chatMsg = new MessageListItem(msg, "Deg");    //TODO: GET USERNAME FROM CHATLISTITEM
        messageList.add(chatMsg);
        mMessageAdapter.notifyDataSetChanged();

        if(role.equals("Client")){
            appClient.sendMessage(msg);
        } else {
            if(mainActivity.get().getPeerAuthenticated().equals("true") ) {
                Log.d(LOG, "Sending message from peerAuthServer" + msg);
                if(peerAuthServer != null){
                    peerAuthServer.sendMessage(msg,sendingMessage );
                }
                else{
                    Log.d(LOG, "Peer auth server obj null");
                }
            }
            else {
                if(appServer != null) {
                    appServer.sendMessage(msg, sendingMessage);
                    Log.d(LOG, "No client connected");
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG, "ChatActivity onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
