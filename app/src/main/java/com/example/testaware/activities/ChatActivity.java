package com.example.testaware.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testaware.AppClient;
import com.example.testaware.AppServer;
import com.example.testaware.IdentityHandler;
import com.example.testaware.adapters.MessageAdapter;
import com.example.testaware.listeners.ConnectionListener;
import com.example.testaware.listeners.SSLContextedObserver;
import com.example.testaware.listitems.MessageListItem;
import com.example.testaware.R;
import com.example.testaware.User;
import com.example.testaware.adapters.MessageListAdapter;
import com.example.testaware.models.AbstractPacket;
import com.example.testaware.models.Contact;
import com.example.testaware.models.Message;
import com.example.testaware.models.MessagePacket;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Inet6Address;

import java.net.InetAddress;
import java.net.NetworkInterface;


import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;

import lombok.Getter;


@RequiresApi(api = Build.VERSION_CODES.Q)
public class ChatActivity extends AppCompatActivity implements ConnectionListener {
        private String LOG = "LOG-Test-Aware-Chat-Activity";
        private RecyclerView mMessageRecycler;
        public MessageAdapter mMessageAdapter;  //Endret
        //public  ArrayList<MessageListItem> messageList; //Endret
        public  ArrayList<Message> messageList;
        private Context context;
        private User user;
        private String myIpvAddr;

        private  Inet6Address peerIpv6;
        private EditText editChatText;

        private SSLContext sslContext;
        private KeyPair keyPair;

        @Getter
        private AppClient appClient;
        private AppServer appServer;

        private ConnectivityManager connectivityManager;

    private Contact contact;

    private static WeakReference<MainActivity> mainActivity;
    public static void updateActivity(MainActivity activity) {
        mainActivity = new WeakReference<>(activity);
    }

    String role;
    //String role = "Server";

    //TODO: change to get dynamic ports

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chat);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            this.context = this;


            myIpvAddr = getLocalIp();


            //user = new User("Server", myIpvAddr, true); //TODO: decide who will be server and client

            SSLContextedObserver sslContextedObserver = mainActivity.get().getSslContextedObserver();
            this.sslContext = sslContextedObserver.getSslContext();
            this.connectivityManager = sslContextedObserver.getConnectivityManager();

            this.keyPair = IdentityHandler.getKeyPair();
            peerIpv6 = MainActivity.getPeerIpv6();
            TextView textView = findViewById(R.id.tvRole);

            textView.setText("SERVER");
            role = "Server";

            //textView.setText("CLIENT");
            //role = "Client";

            this.appClient  = mainActivity.get().getConnectionHandler().getAppClient();
            this.appServer  = mainActivity.get().getConnectionHandler().getAppServer();

                    Intent intent = getIntent();
            contact = (Contact) intent.getSerializableExtra("contact" + "");
            setTitle(contact.getCommonName());

            setupUI();

        }

        private void setupUI(){
            editChatText = findViewById(R.id.eTChatMsg);
            messageList = new ArrayList<>();


            mMessageAdapter = new MessageAdapter(this, R.layout.other_message, messageList, keyPair);
            ListView listView = findViewById(R.id.lvMessages);


            //mMessageRecycler = findViewById(R.id.recyclerChat); //Endret
            listView.setAdapter(mMessageAdapter);     //Endret
            //mMessageRecycler.setAdapter(mMessageAdapter);
            //mMessageRecycler.setLayoutManager(new LinearLayoutManager(this)); //Endret, kommentert ut



            /*final Button sendChatMsgbtn = findViewById(R.id.btnSendChatMsg);
            if (MainActivity.isPublisher()) {
                startServer();
            } else {
                initClient();
            }*/
            Button sendChatMsgbtn = findViewById(R.id.btnSendChatMsg);
            EditText messageText = findViewById(R.id.eTChatMsg);
            sendChatMsgbtn.setOnClickListener(v -> {
                    Log.d(LOG, messageText.getText().toString());
                    sendMessage(messageText.getText().toString());
                    messageText.setText("");
                    Log.i(LOG, "Send btn pressed");
            });

           // Intent intent = getIntent();
           // contact = (Contact) intent.getSerializableExtra("EXTRA_CONTACT");
           // setTitle(contact.getCommonName());
        }


    private void sendMessage(String msg) {
        Log.d(LOG, "Sending message: " + msg);

        final Message message;
        message = new Message(
                contact.getCertificate().getPublicKey(),
                keyPair.getPublic(),
                msg,
                keyPair.getPrivate());
        mMessageAdapter.add(message);
        mainActivity.get().getConnectionHandler().sendMessage(message);
        if(role.equals("Client")){
            appClient.sendMessage(message);
        } else {
            appServer.sendMessage(message);
        }

        /*final Message message;
        try {
            message = new Message(
                    contact.getCertificate().getPublicKey(),
                    keyPair.getPublic(),
                    msg,
                    keyPair.getPrivate()
            );
            messageList.add(message);
            mMessageAdapter.add(message);
            mainActivity.get().getConnectionManager().sendMessage(message);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchPaddingException | IOException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
*/
        //MessageListItem messageListItem = new MessageListItem(msg, "ipv6_other_user" );    Endret, kommentert ut
        //messageList.add(messageListItem);                                                      Endret, kommentert ut
        //mMessageAdapter.notifyDataSetChanged();        Endret, kommentert ut
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

    @Override
    public void onConnect() {

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onPacket(Contact contact, AbstractPacket packet) {
        if(packet instanceof MessagePacket) {
            MessagePacket messagePacket = (MessagePacket) packet;
            final Message message = messagePacket.getMessage();

            if(message.getFrom().equals(contact.getCertificate().getPublicKey())) {
                runOnUiThread(() -> {
                    mMessageAdapter.add(message);
                });
            }
        }
    }

    @Override
    public void onServerPacket(AbstractPacket packet) {

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


/*    private X509Certificate getPeerCertificate() {
        try {
            Certificate[] certs = socket2.getSession().getPeerCertificates();
            if(certs.length > 0 && certs[0] instanceof X509Certificate) {
                return (X509Certificate) certs[0];

            }
        } catch (SSLPeerUnverifiedException | NullPointerException ignored) {

        }


        return null;
    }*/
 /*   private X509Certificate chechPeerCertificate(){
            //TODO: chech if peerCert signed by CA, if not send to peerSigner if trusted. Prompt user yes/no. If yes, send to PeerSigner
        return cert;
    }

                          */
}
