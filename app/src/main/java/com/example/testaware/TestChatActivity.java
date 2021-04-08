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
import androidx.recyclerview.widget.RecyclerView;

import com.example.testaware.activities.MainActivity;
import com.example.testaware.adapters.MessageAdapter;
import com.example.testaware.listeners.ConnectionListener;
import com.example.testaware.listeners.SSLContextedObserver;
import com.example.testaware.models.AbstractPacket;
import com.example.testaware.models.Contact;
import com.example.testaware.models.Message;

import java.lang.ref.WeakReference;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.net.ssl.SSLContext;

import lombok.Getter;

public class TestChatActivity extends AppCompatActivity {

    private Inet6Address peerIpv6;
    private EditText editChatText;
    private String LOG = "LOG-Test-Aware-Test-Chat-Activity";
    private RecyclerView mMessageRecycler;
    public static MessageAdapter mMessageAdapter;

    public  ArrayList<Message> messageList;     // Endret for øystein sin adapter
   // public static ArrayList<MessageListItem> messageList;
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
    String role;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.context = this;

        myIpvAddr = getLocalIp();


        SSLContextedObserver sslContextedObserver = mainActivity.get().getSslContextedObserver();
        this.sslContext = sslContextedObserver.getSslContext();
        this.connectivityManager = sslContextedObserver.getConnectivityManager();


        this.keyPair = mainActivity.get().getKeyPair();
        peerIpv6 = MainActivity.getPeerIpv6();
        TextView textView = findViewById(R.id.tvRole);

        if(mainActivity.get().getRole().equals("publisher")){
            textView.setText("SERVER");
            role = "Server";
        } else {
            appClient = new AppClient(keyPair, sslContext);
            mainActivity.get().getConnectionHandler().setAppClient(appClient);
            textView.setText("CLIENT");
            role = "Client";
            //client = new Client(keyPair,sslContext);   //if user is client, new thread for each server conn
            Thread thread = new Thread(appClient);
            thread.start();
        }


        this.appServer  = mainActivity.get().getConnectionHandler().getAppServer();

        Intent intent = getIntent();
        contact = (Contact) intent.getSerializableExtra("contact");
        setupUI();
        /* KOMMENTERT UT 06.04
        mainActivity.get().getConnectionHandler().registerConnectionListener(this); */


    }

    private void setupUI(){
       /* editChatText = findViewById(R.id.eTChatMsg);
        messageList = new ArrayList<>();
        mMessageRecycler = findViewById(R.id.recyclerChat);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this)); */       //ENDRET til Øystein sin adapter

        editChatText = findViewById(R.id.eTChatMsg);
        messageList = new ArrayList<>();


        mMessageAdapter = new MessageAdapter(this, R.layout.other_message, messageList, keyPair);
        ListView listView = findViewById(R.id.lvMessages);
        listView.setAdapter(mMessageAdapter);


        Button sendChatMsgbtn = findViewById(R.id.btnSendChatMsg);
        sendChatMsgbtn.setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                EditText messageText = findViewById(R.id.eTChatMsg);
                String messageToSend = messageText.getText().toString();
                sendMessage(messageToSend);
                messageText.getText().clear();
                if(appClient != null){
                   //appClient.sendMessage(messageToSend);
                    //sendMessage(messageToSend);
                }
                else{
                    //sendMessage(messageToSend);
                    /*//clientHandelerWeakReference.sendMessage(messageToSend);
                    try {
                        WeakReference<ClientHandeler> clientHandeler = clientHandelerWeakReference;
                        String [] msg = new String[0];
                        msg [0] = messageToSend;
                        clientHandelerWeakReference.getClass().getMethod("sendMessage", msg);
                        Log.i(LOG, "Send MEssage Client Handler");
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }*/
                }
            }
            Log.i(LOG, "Send btn pressed");
        });
    }




    public static void setChat(Message message){
        //MessageListItem chatMsg = new MessageListItem(message, ipv6);    //TODO: GET USERNAME FROM CHATLISTITEM
        //messageList.add(chatMsg);
        mMessageAdapter.add(message);
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
        Log.d(LOG, "Sending message: " + msg);

        final Message message;
        message = new Message(
                contact.getCertificate().getPublicKey(),
                keyPair.getPublic(),
                msg,
                keyPair.getPrivate());
        mMessageAdapter.add(message);  //endret Nå

        //mainActivity.get().getConnectionHandler().sendMessage(message);
        if(role.equals("Client")){
            appClient.sendMessage(message);
        } else {
            appServer.sendMessage(message);
        }

    }

    /*
    KOMMENTERT UT 06.04
    @Override
    public void onConnect() {

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onPacket(Message message) {
        Log.d(LOG, "onPacket in ChatAct");

    }

    @Override
    public void onServerPacket(AbstractPacket packet) {

    }
*/

}
