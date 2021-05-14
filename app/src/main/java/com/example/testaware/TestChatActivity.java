package com.example.testaware;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testaware.activities.MainActivity;
//import com.example.testaware.adapters.MessageAdapter;
import com.example.testaware.adapters.MessageListAdapter;

import com.example.testaware.listeners.SSLContextedObserver;
import com.example.testaware.listitems.MessageListItem;
import com.example.testaware.models.AbstractPacket;
import com.example.testaware.models.Contact;
import com.example.testaware.models.Message;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.net.ssl.SSLContext;

import lombok.Getter;

import static java.lang.System.currentTimeMillis;

public class TestChatActivity extends AppCompatActivity {

    private Inet6Address peerIpv6;
    private EditText editChatText;
    private String LOG = "LOG-Test-Aware-Test-Chat-Activity";
    private RecyclerView mMessageRecycler;
    public static MessageListAdapter mMessageAdapter;

    //public  ArrayList<Message> messageList;     // øystein sin adapter
   public static ArrayList<MessageListItem> messageList;
    private Context context;
    private User user;
    private String myIpvAddr;


    private SSLContext sslContext;
    private KeyPair keyPair;

    @Getter
    private AppClient appClient;

    private AppServer appServer;


    private ConnectivityManager connectivityManager;

    private static WeakReference<MainActivity> mainActivity;
    public static void updateActivityMain(MainActivity activity) {
        mainActivity = new WeakReference<>(activity);
    }

    private static WeakReference<ClientHandler> clientHandlerWeakReference;
    public static void updateActivityClientHandler(ClientHandler activity) {
        clientHandlerWeakReference = new WeakReference<>(activity);
    }

    private Contact contact;
    private String role;
    private int port;
    private int counterValue = 0;


    private Thread thread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.context = this;

        myIpvAddr = getLocalIp();
        port = getIntent().getIntExtra("port", 1025);

        role = getIntent().getStringExtra("Role");
        counterValue = getIntent().getIntExtra("counter", 0);
        TextView textView = findViewById(R.id.tvRole);
        textView.setText(role);

        SSLContextedObserver sslContextedObserver = mainActivity.get().getSslContextedObserver();
        this.sslContext = sslContextedObserver.getSslContext();
        this.connectivityManager = sslContextedObserver.getConnectivityManager();


        this.keyPair = mainActivity.get().getKeyPair();
        peerIpv6 = MainActivity.getPeerIpv6();

        if(role.equals("Client")){

            long clientStarted = currentTimeMillis();
            Log.d("TESTING-LOG-TIME-TLS-CLIENT-STARTED",  String.valueOf(clientStarted));
            appClient = new AppClient(keyPair, sslContext, port, clientStarted, counterValue);


            Log.d(LOG, "Port: " + port);
            mainActivity.get().getConnectionHandler().setAppClient(appClient);

            thread = new Thread(appClient);
            thread.start();


            //long clientThreadStarted = currentTimeMillis();
            //Log.d("TESTING-LOG-TIME-TLS-CLIENT-THREAD",  String.valueOf(clientThreadStarted));
        }

        this.appServer  = mainActivity.get().getConnectionHandler().getAppServer();

        Intent intent = getIntent();
        //contact = (Contact) intent.getSerializableExtra("contact");
        setupUI();

        TextView username = findViewById(R.id.tvName);
        username.setText("User2");


    }

    private void setupUI(){
        editChatText = findViewById(R.id.eTChatMsg);
        messageList = new ArrayList<>();
        mMessageRecycler = findViewById(R.id.recyclerChat);
        editChatText = findViewById(R.id.eTChatMsg);
        messageList = new ArrayList<>();
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));




       /* mMessageAdapter = new MessageAdapter(this, R.layout.other_message, messageList, keyPair);
        ListView listView = findViewById(R.id.lvMessages);
        listView.setAdapter(mMessageAdapter); */


        Button sendChatMsgbtn = findViewById(R.id.btnSendChatMsg);
        sendChatMsgbtn.setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                EditText messageText = findViewById(R.id.eTChatMsg);
                String messageToSend = messageText.getText().toString();
                sendMessage(messageToSend);
                messageText.getText().clear();
               /* if(appClient != null){
                   //appClient.sendMessage(messageToSend);
                    //sendMessage(messageToSend);
                }
                else{
                    //sendMessage(messageToSend);
                    //clientHandelerWeakReference.sendMessage(messageToSend);
                    try {
                        WeakReference<ClientHandeler> clientHandeler = clientHandelerWeakReference;
                        String [] msg = new String[0];
                        msg [0] = messageToSend;
                        clientHandelerWeakReference.getClass().getMethod("sendMessage", msg);
                        Log.i(LOG, "Send MEssage Client Handler");
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }*/
            }
            Log.i(LOG, "Send btn pressed");
        });
    }




    public static void setChat(String message, int counterValueStatic){
        MessageListItem chatMsg = new MessageListItem(message, "User2");    //TODO: GET USERNAME FROM CHATLISTITEM
        messageList.add(chatMsg);
        mMessageAdapter.notifyDataSetChanged();

        long settingMessage = currentTimeMillis();
        //Log.d("TESTING-LOG-TIME-TLS-MESSAGE-SET",  String.valueOf(settingMessage));

        BufferedWriter writer = null;
        try {
            String outputText = String.valueOf(settingMessage);
            writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/messageReceived", true));
            writer.append("Counter:" + counterValueStatic);
            writer.append("\n");
            writer.append(outputText);
            writer.append("\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //mMessageAdapter.add(message);
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


 /*   private X509Certificate chechPeerCertificate(){
            //TODO: chech if peerCert signed by CA, if not send to peerSigner if trusted. Prompt user yes/no. If yes, send to PeerSigner
        return cert;
    }
*/

    private void sendMessage(String msg) {

        long sendingMessage = currentTimeMillis();
        Log.d("TESTING-LOG-TIME-TLS-SEND-MESSAGE-PRESSED",  String.valueOf(sendingMessage));




        Log.d(LOG, "Sending message: " + msg);
        MessageListItem chatMsg = new MessageListItem(msg, "Deg");    //TODO: GET USERNAME FROM CHATLISTITEM
        messageList.add(chatMsg);
        mMessageAdapter.notifyDataSetChanged();
       /* final Message message;
        message = new Message(
                contact.getCertificate().getPublicKey(),
                keyPair.getPublic(),
                msg,
                keyPair.getPrivate());
        mMessageAdapter.add(message);  */


        //mainActivity.get().getConnectionHandler().sendMessage(message);
        if(role.equals("Client")){
            appClient.sendMessage(msg, sendingMessage);
        } else {
            appServer.sendMessage(msg, sendingMessage);
        }
    }

    @Override
    protected void onStop() {

        //TODO close client/server
        super.onStop();
        /*try {
            appClient.getSslSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        Log.d(LOG, "ChatActivity onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TESTING-LOG-TIME-TLS-onDestroy", "App destroyed");
        //TODO: remove peer from list
    }
}
