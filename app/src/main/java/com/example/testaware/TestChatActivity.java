package com.example.testaware;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkSpecifier;
import android.net.wifi.aware.PeerHandle;
import android.net.wifi.aware.WifiAwareNetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testaware.activities.ChatActivity;
import com.example.testaware.activities.MainActivity;
import com.example.testaware.adapters.MessageListAdapter;
import com.example.testaware.listitems.MessageListItem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

public class TestChatActivity extends AppCompatActivity {

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

    private ConnectivityManager connectivityManager;
    private NetworkSpecifier networkSpecifier;
    private NetworkCapabilities networkCapabilities;
    private PeerHandle peerHandle;
    private WifiAwareNetworkInfo peerAwareInfo;



    private SSLContext sslContext;
    private KeyPair keyPair;

    private AppClient appClient;
    private AppServer appServer;
    private Client client;
    private Server server;

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
       // textView.setText("CLIENT");
       // Log.d(LOG, "CLIENT: " + peerIpv6);
        //client = new Client(keyPair,sslContext);   //if user is client, new thread for each server conn
       // Thread thread = new Thread(client);
        //thread.start();



    }

    private void setupUI(){
        editChatText = findViewById(R.id.eTChatMsg);
        messageList = new ArrayList<>();
        mMessageRecycler = findViewById(R.id.recyclerChat);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));

        Button sendChatMsgbtn = findViewById(R.id.btnSendChatMsg);
        sendChatMsgbtn.setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                EditText messageText = findViewById(R.id.eTChatMsg);
                String messageToSend = messageText.getText().toString();
                if(client != null){

                   client.sendMessage(messageToSend);
                }
                else{
                    ClientHandeler.setOutputStream(messageToSend);
                }
                /*catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }*/
            }
            Log.i(LOG, "Send btn pressed");
            //}
        });
    }


    public static void setChat(String message, String ipv6){
        MessageListItem chatMsg = new MessageListItem(message, ipv6);    //TODO: GET USERNAME FROM CHATLISTITEM
        messageList.add(chatMsg);
        mMessageAdapter.notifyDataSetChanged();
    }


    public void notifyMessageAdapter(){
        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
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
