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
import java.security.KeyPair;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;

import lombok.Getter;


public class ChatActivity extends AppCompatActivity {

    private final String LOG = "LOG-Test-Aware-Test-Chat-Activity";
    public static MessageListAdapter mMessageAdapter;

    public static ArrayList<MessageListItem> messageList;
    @Getter
    private Context context;

    @Getter
    private AppClient appClient;

    private AppServer appServer;
    private PeerAuthServer peerAuthServer;
    private String role;


    private static WeakReference<ClientHandler> clientHandlerWeakReference;
    public static void updateActivityClientHandler(ClientHandler activity) {
        clientHandlerWeakReference = new WeakReference<>(activity);
    }

    private static WeakReference<MainActivity> mainActivity;
    public static void updateActivityMain(MainActivity activity) {
        mainActivity = new WeakReference<>(activity);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.context = this;

        int port = getIntent().getIntExtra("port", 1025);

        role = getIntent().getStringExtra("Role");
        TextView textView = findViewById(R.id.tvRole);
        textView.setText(role);

        SSLContextedObserver sslContextedObserver = mainActivity.get().getSslContextedObserver();
        SSLContext sslContext = sslContextedObserver.getSslContext();


        KeyPair keyPair = mainActivity.get().getKeyPair();

        if(role.equals("Client")){
            appClient = new AppClient(keyPair, sslContext, 1025);

            Thread thread = new Thread(appClient);
            thread.start();

        }

        this.appServer  = mainActivity.get().getAppServer();
        this.peerAuthServer = mainActivity.get().getPeerAuthServer();

        setupUI();

        TextView username = findViewById(R.id.tvName);
        username.setText("User2");
    }

    private void setupUI(){
        messageList = new ArrayList<>();
        RecyclerView mMessageRecycler = findViewById(R.id.recyclerChat);
        messageList = new ArrayList<>();
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));


        Button sendChatMsgbtn = findViewById(R.id.btnSendChatMsg);
        sendChatMsgbtn.setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                EditText messageText = findViewById(R.id.eTChatMsg);
                String messageToSend = messageText.getText().toString();
                sendMessage(messageToSend);
                messageText.getText().clear();
            }
            Log.i(LOG, "Send btn pressed");
        });
    }


    public static void setChat(String message){
        MessageListItem chatMsg = new MessageListItem(message, "User2");
        messageList.add(chatMsg);
        mMessageAdapter.notifyDataSetChanged();
    }



    private void sendMessage(String msg) {
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
                    peerAuthServer.sendMessage(msg);
                }
                else{
                    Log.d(LOG, "Peer auth server obj null");
                }
            }
            else {
                if(appServer != null) {
                    appServer.sendMessage(msg);
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
