package com.example.testaware.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.aware.AttachCallback;
import android.net.wifi.aware.DiscoverySessionCallback;
import android.net.wifi.aware.IdentityChangedListener;
import android.net.wifi.aware.PeerHandle;
import android.net.wifi.aware.PublishConfig;
import android.net.wifi.aware.PublishDiscoverySession;
import android.net.wifi.aware.SubscribeConfig;
import android.net.wifi.aware.SubscribeDiscoverySession;
import android.net.wifi.aware.WifiAwareManager;
import android.net.wifi.aware.WifiAwareNetworkInfo;
import android.net.wifi.aware.WifiAwareNetworkSpecifier;
import android.net.wifi.aware.WifiAwareSession;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.testaware.AppServer;
import com.example.testaware.IdentityHandler;
import com.example.testaware.offlineAuth.PeerSigner;
import com.example.testaware.listitems.ChatListItem;
import com.example.testaware.Constants;
import com.example.testaware.R;
import com.example.testaware.adapters.ChatsListAdapter;

import java.net.Inet6Address;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
//import lombok.Getter;


public class MainActivity extends AppCompatActivity {


    private WifiAwareManager wifiAwareManager;
    private WifiAwareSession wifiAwareSession;
    private Context context;
    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final String[] LOCATION_PERMS_COARSE={
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int INITIAL_REQUEST=1337;
    private static final int LOCATION_REQUEST_COARSE=INITIAL_REQUEST+2;
    private static final int LOCATION_REQUEST_FINE=INITIAL_REQUEST+3;


    private PublishDiscoverySession   publishDiscoverySession;
    private SubscribeDiscoverySession subscribeDiscoverySession;

    private byte[]                    portOnSystem;
    private int                       portToUse;

    private byte[]                    myIP;
    private byte[]                    otherIP;
    private byte[]                    msgtosend;

    private byte[]                    otherMac;
    private PeerHandle peerHandle;
    private byte[] myMac;


    private final int MAC_ADDRESS_MESSAGE = 11;
    private BroadcastReceiver         broadcastReceiver;
    private ConnectivityManager       connectivityManager;
    private NetworkSpecifier          networkSpecifier;


    private final int                 IP_ADDRESS_MESSAGE             = 33;

    private String ipAddr; //other IP

    private ServerSocket              serverSocket;
    private final int                 MESSAGE                        = 7;
    private NetworkCapabilities networkCapabilities;
    private Network network;

    private WifiAwareNetworkInfo peerAwareInfo;

    private static Inet6Address peerIpv6 ;
    private int peerPort;


    private List<PeerHandle> peerHandleList = new ArrayList<>();
    private List<String> otherMacList = new ArrayList<>();
    private String macAddress;

    private static final int MY_PERMISSION_COARSE_LOCATION_REQUEST_CODE = 88;
    private static final int MY_PERMISSION_FINE_LOCATION_CODE = 99;
    private static final int MY_PERMISSION_NETWORK_STATE_CODE = 77;

    private static final int          MY_PERMISSION_EXTERNAL_REQUEST_CODE = 99;

    private static boolean isPublisher = false;
    private boolean hasEstablishedPublisherAndSubscriber = false;
    private static SSLContext sslContext;
    private KeyPair keyPair;
    private String signedKey;


    //private WifiAwareManager wifiAwareManager;
    private void initAwareManager(){
        wifiAwareManager = (WifiAwareManager) getSystemService(Context.WIFI_AWARE_SERVICE);
    }
  /*  public static WifiAwareManager getWifiAwareManager(){
        return wifiAwareManager;
    }
*/
    public static boolean getRole(){
        return isPublisher;
    }


    private String LOG = "LOG-Test-Aware";

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wifiAwareManager = null;
        wifiAwareSession = null;
        connectivityManager = null;
        networkSpecifier = null;
        publishDiscoverySession = null;
        subscribeDiscoverySession = null;
        peerHandle = null;

        setupPermissions();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiAwareManager = (WifiAwareManager) getSystemService(Context.WIFI_AWARE_SERVICE);

        context = this;

        addPeersToChatList();

        Button btn = findViewById(R.id.btnPublish);
        btn.setOnClickListener(v -> establishWhoIsPublisherAndSubscriber());


        Button send = findViewById(R.id.btnSend);
        send.setOnClickListener(view -> {
            requestWiFiConnection();
            /*String msg= "messageToBeSent: ";

            EditText editText = findViewById(R.id.eTMsg);
            msg += editText.getText().toString();
            byte[] msgtosend = msg.getBytes();
            if (publishDiscoverySession != null && peerHandle != null) {
                publishDiscoverySession.sendMessage(peerHandle, MESSAGE, msgtosend);
            } else if(subscribeDiscoverySession != null && peerHandle != null) {
                subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE, msgtosend);
            }*/
        });


        if (canAccessLocationFine()) {
            Log.i(LOG,"Can access fine location");
        }
        else {
            requestPermissions(LOCATION_PERMS, LOCATION_REQUEST_FINE);
        }

        if (canAccessLocationCoarse()) {
            Log.i(LOG,"Can access coarse location");
        }
        else {
            requestPermissions(LOCATION_PERMS_COARSE, LOCATION_REQUEST_COARSE);
        }

        sslContext = IdentityHandler.getSSLContext(this.context);
        this.keyPair = IdentityHandler.getKeyPair();
        attachToSession();

        Button conPub = findViewById(R.id.btnConnectPub);
        conPub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.requestWiFiConnection();
                /*Thread startConn = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        MainActivity.this.requestWiFiConnection();
                    }
                });
                startConn.start();*/
                Button startServerButton = findViewById(R.id.btnStartServer);
                startServerButton.setVisibility(View.VISIBLE);
                startServerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppServer appServer = new AppServer(sslContext, Constants.SERVER_PORT);
                        Log.d(LOG, "SERVER: " + peerIpv6);
                    }
                });
            }
        });

        Button subPub = findViewById(R.id.btnConnectSub);
        subPub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.requestWiFiConnection();
                /*Thread startConn = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        MainActivity.this.requestWiFiConnection();
                    }
                });
                startConn.start();*/
            }
        });

        //String signedKey = PeerSigner.peerSign();  //TODO: call this method somewhere else where suitable, called from main just for testing

    }



    /**
     * App Permissions for Coarse Location
     **/
    private void setupPermissions() {
        // If we don't have the record network permission...
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // And if we're on SDK M or later...
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Ask again, nicely, for the permissions.
                String[] permissionsWeNeed = new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION };
                requestPermissions(permissionsWeNeed, MY_PERMISSION_COARSE_LOCATION_REQUEST_CODE);
            }
        }

        //-------------------------------------------------------------------------------------------- +++++
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // And if we're on SDK M or later...
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Ask again, nicely, for the permissions.
                String[] permissionsWeNeed = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE };
                requestPermissions(permissionsWeNeed, MY_PERMISSION_EXTERNAL_REQUEST_CODE);
            }
        }
        //-------------------------------------------------------------------------------------------- -----
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_COARSE_LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;

                } else {
                    Toast.makeText(this, "Permission for location not granted. NAN can't run.", Toast.LENGTH_LONG).show();
                    finish();
                    // The permission was denied, so we can show a message why we can't run the app
                    // and then close the app.
                }
            }

            //-------------------------------------------------------------------------------------------- +++++
            case MY_PERMISSION_EXTERNAL_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;

                } else {
                    Toast.makeText(this, "no sd card access", Toast.LENGTH_LONG).show();
                }
            }
            //-------------------------------------------------------------------------------------------- -----
            // Other permissions could go down here

        }
    }


    public static SSLContext getSslContext(){
        return sslContext;
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void addPeersToChatList(){
        ArrayList<ChatListItem> userList = new ArrayList<>();
        for (String macAddr : otherMacList){
            ChatListItem chatElise = new ChatListItem("MAC:", macAddr);
            userList.add(chatElise);
        }
        ChatsListAdapter chatListAdapter = new ChatsListAdapter(this, userList);
        ListView listViewChats = findViewById(R.id.listViewChats);
        listViewChats.setAdapter(chatListAdapter);
        listViewChats.setOnItemClickListener((parent, view, position, id) -> {
            MainActivity.this.openChat(position);
            //MainActivity.this.requestWiFiConnection();
        });
    }


    private void establishWhoIsPublisherAndSubscriber(){
        if(!hasEstablishedPublisherAndSubscriber){
            if (publishDiscoverySession != null && subscribeDiscoverySession != null){
                isPublisher = true;
                findViewById(R.id.btnConnectPub).setVisibility(View.VISIBLE);
                String msg = "subscriber.establishingRole";
                byte[] msgtosend = msg.getBytes();
                if (publishDiscoverySession != null && peerHandle != null) {
                    publishDiscoverySession.sendMessage(peerHandle, MESSAGE, msgtosend);
                } else if(subscribeDiscoverySession != null && peerHandle != null) {
                    subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE, msgtosend);
                }
            } else if(publishDiscoverySession != null){
                isPublisher = true;
            }
            hasEstablishedPublisherAndSubscriber = true;
            subscribeDiscoverySession = null;
        }
    }


    public static boolean isPublisher(){
        return isPublisher;
    }


    public void startPublishAndSubscribe(){
        publish();
        /*Thread publishThread = new Thread(() -> {
            try {
                Thread.sleep(2000 );
                if(notSubscribed){
                    publish();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        publishThread.start();*/
        subscribe();
    }


    private void attachToSession(){
        //TODO: check if a cluster exists, if not the first user will be publisher?
        wifiAwareManager.attach(new AttachCallback() {
            @Override
            public void onAttached(WifiAwareSession session) {
                super.onAttached(session);
                Log.i(LOG, "ON Attached!");
                wifiAwareSession = session;
                //TODO: close session
            }
            @Override
            public void onAttachFailed() {
            }

        }, new IdentityChangedListener() {
            @Override
            public void onIdentityChanged(byte[] mac) {
                super.onIdentityChanged(mac);
                setMacAddress(mac);
                startPublishAndSubscribe();
                //closeSession(); ENDRET
            }
        }, null);
    }

    private final byte[]              serviceInfo            = "android".getBytes();
    /*private void publish () {
        PublishConfig config = new PublishConfig.Builder()
                .setServiceName(Constants.SERVICE_NAME)
                .build();
        //TODO: set service specific info ?

        wifiAwareSession.publish(config, new DiscoverySessionCallback() {
            @Override
            public void onPublishStarted(PublishDiscoverySession session) {
                Log.i(LOG, "publish started");
                super.onPublishStarted(session);
                publishDiscoverySession = session;
                if (publishDiscoverySession != null && peerHandle != null) {
                    publishDiscoverySession.sendMessage(peerHandle, MAC_ADDRESS_MESSAGE, myMac);
                    Log.d("nanPUBLISH", "onPublishStarted sending mac");
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onMessageReceived(PeerHandle peerHandle_, byte[] message) {
                super.onMessageReceived(peerHandle, message);

                Log.i(LOG, "onMessageReceived");
                if(message.length == 2) {
                    portToUse = byteToPortInt(message);
                    Log.d(LOG, "subscribe, will use port number "+ portToUse);
                } else if (message.length == 6){
                    setOtherMacAddress(message);
                    Log.d(LOG, "setOtherMacAddress "+ message);
                } else if (message.length == 16) {
                    setOtherIPAddress(message);
                    Log.d(LOG, "setOtherIPAddress "+ message);
                } else if (message.length > 16) {
                    String[] messageIn = new String(message).split(".");
                    if(messageIn[0].equals("subscriber") && messageIn[1].equals("establishingRole")){
                        isPublisher = false;
                        hasEstablishedPublisherAndSubscriber = true;
                    }
                }
                peerHandle = peerHandle_;
                if (!otherMacList.contains(macAddress)){
                    otherMacList.add(macAddress);
                    peerHandleList.add(peerHandle);
                    int numOfSubscribers = peerHandleList.size();
                    Log.d(LOG, "numOfSubscribers" + numOfSubscribers);
                    if(otherMacList.size()>1){
                        for (int i = 0; i <otherMacList.size(); i ++){
                            Log.d(LOG, "otherMacList " + i + " macAddress: " + otherMacList.get(i));
                            //byte [] messageOtherMac =  otherMacList.get(i).getBytes();
                            //publishDiscoverySession.sendMessage(peerHandle, MESSAGE, messageOtherMac);
                        }
                    }

                    if(peerHandleList.size()>1){
                        for (int i = 0; i <peerHandleList.size(); i ++){
                            Log.d(LOG, "peerHandleList " + i + " peerhandle: " + peerHandleList.get(i));
                            //byte [] messageOtherMac =  peerHandleList.get(i).getBytes();
                            //publishDiscoverySession.sendMessage(peerHandle, MESSAGE, messageOtherMac);
                        }
                    }
                    Log.d(LOG, "numOfSubscribers" + numOfSubscribers);
                    addPeersToChatList();
                }
            }
        }, null);
    }*/

    private void publish () {
        PublishConfig config = new PublishConfig.Builder()
                .setServiceName(Constants.SERVICE_NAME)
                .build();
        //TODO: set service specific info ?

        wifiAwareSession.publish(config, new DiscoverySessionCallback() {
            @Override
            public void onPublishStarted(PublishDiscoverySession session) {
                Log.i(LOG, "publish started");
                super.onPublishStarted(session);
                publishDiscoverySession = session;
            }

            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onMessageReceived(PeerHandle peerHandle_, byte[] message) {
                super.onMessageReceived(peerHandle, message);

                Log.i(LOG, "onMessageReceived");
                if(message.length == 2) {
                    portToUse = byteToPortInt(message);
                    Log.d(LOG, "subscribe, will use port number "+ portToUse);
                } else if (message.length == 6){
                    setOtherMacAddress(message);
                    Log.d(LOG, "setOtherMacAddress "+ message);
                } else if (message.length == 16) {
                    setOtherIPAddress(message);
                    Log.d(LOG, "setOtherIPAddress "+ message);
                } else if (message.length > 16) {
                    String[] messageIn = new String(message).split(".");
                    if(messageIn[0].equals("subscriber") && messageIn[1].equals("establishingRole")){
                        isPublisher = false;
                        hasEstablishedPublisherAndSubscriber = true;
                    }
                }
                peerHandle = peerHandle_;
                if (!otherMacList.contains(macAddress)){
                    otherMacList.add(macAddress);
                    peerHandleList.add(peerHandle);
                    int numOfSubscribers = peerHandleList.size();
                    Log.d(LOG, "numOfSubscribers" + numOfSubscribers);
                    if(otherMacList.size()>1){
                        for (int i = 0; i <otherMacList.size(); i ++){
                            Log.d(LOG, "otherMacList " + i + " macAddress: " + otherMacList.get(i));
                            //byte [] messageOtherMac =  otherMacList.get(i).getBytes();
                            //publishDiscoverySession.sendMessage(peerHandle, MESSAGE, messageOtherMac);
                        }
                    }

                    if(peerHandleList.size()>1){
                        for (int i = 0; i <peerHandleList.size(); i ++){
                            Log.d(LOG, "peerHandleList " + i + " peerhandle: " + peerHandleList.get(i));
                            //byte [] messageOtherMac =  peerHandleList.get(i).getBytes();
                            //publishDiscoverySession.sendMessage(peerHandle, MESSAGE, messageOtherMac);
                        }
                    }
                    Log.d(LOG, "numOfSubscribers" + numOfSubscribers);
                    addPeersToChatList();
                }
            }
        }, null);
    }

    /*private void subscribe(){
        SubscribeConfig config = new SubscribeConfig.Builder()
                .setServiceName(Constants.SERVICE_NAME)
                .build();
        wifiAwareSession.subscribe(config, new DiscoverySessionCallback() {
            @Override
            public void onSubscribeStarted(SubscribeDiscoverySession session) {
                super.onSubscribeStarted(session);
                subscribeDiscoverySession = session;
                Log.i(LOG, "subscribe started");

                if (subscribeDiscoverySession != null && peerHandle != null) {
                    subscribeDiscoverySession.sendMessage(peerHandle, MAC_ADDRESS_MESSAGE, myMac);
                    Log.d(LOG, " subscribe, onServiceStarted send mac");
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onServiceDiscovered(PeerHandle peerHandle_, byte[] serviceSpecificInfo, List<byte[]> matchFilter) {
                super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter);
                peerHandle=peerHandle_;
                if (subscribeDiscoverySession != null && peerHandle != null) {
                    subscribeDiscoverySession.sendMessage(peerHandle, MAC_ADDRESS_MESSAGE, myMac);
                }
                // establishWhoIsPublisherAndSubscriber();
            }
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onMessageReceived(PeerHandle peerHandle, byte[] message) {
                super.onMessageReceived(peerHandle, message);
                Log.d(LOG, "subscribe, received message");
                Toast.makeText(MainActivity.this, "received", Toast.LENGTH_LONG).show();
                if(message.length == 2) {
                    portToUse = byteToPortInt(message);
                    Log.d(LOG, "subscribe, will use port number "+ portToUse);
                } else if (message.length == 6){
                    setOtherMacAddress(message);
                    Log.d(LOG, "setOtherMacAddress "+ message);
                } else if (message.length == 16) {
                    setOtherIPAddress(message);
                    Log.d(LOG, "setOtherIPAddress "+ message);
                } else if (message.length > 16) {
                    String messageIn = new String(message);
                    Log.d(LOG, "Message IN:" + messageIn);
                    if(messageIn.contains("subscriber") && messageIn.contains("establishingRole")) {
                        Log.d(LOG, "YES" + messageIn);
                        isPublisher = false;
                        findViewById(R.id.btnConnectSub).setVisibility(View.VISIBLE);
                        hasEstablishedPublisherAndSubscriber = true;
                        publishDiscoverySession = null;
                        //requestWiFiConnection();
                    }
                    //setMessage(message);
                    //Toast.makeText(MainActivity.this, "message received", Toast.LENGTH_LONG).show();
                }
            }
        }, null);
    }*/

    private void subscribe(){
        SubscribeConfig config = new SubscribeConfig.Builder()
                .setServiceName(Constants.SERVICE_NAME)
                .build();
        wifiAwareSession.subscribe(config, new DiscoverySessionCallback() {
            @Override
            public void onSubscribeStarted(SubscribeDiscoverySession session) {
                super.onSubscribeStarted(session);
                subscribeDiscoverySession = session;
                Log.i(LOG, "subscribe started");

                if (subscribeDiscoverySession != null && peerHandle != null) {
                    subscribeDiscoverySession.sendMessage(peerHandle, MAC_ADDRESS_MESSAGE, myMac);
                    Log.d(LOG, " subscribe, onServiceStarted send mac");
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onServiceDiscovered(PeerHandle peerHandle_, byte[] serviceSpecificInfo, List<byte[]> matchFilter) {
                super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter);
                peerHandle=peerHandle_;
                if (subscribeDiscoverySession != null && peerHandle != null) {
                    subscribeDiscoverySession.sendMessage(peerHandle, MAC_ADDRESS_MESSAGE, myMac);
                }
                // establishWhoIsPublisherAndSubscriber();
            }
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onMessageReceived(PeerHandle peerHandle, byte[] message) {
                super.onMessageReceived(peerHandle, message);
                Log.d(LOG, "subscribe, received message");
                Toast.makeText(MainActivity.this, "received", Toast.LENGTH_LONG).show();
                if(message.length == 2) {
                    portToUse = byteToPortInt(message);
                    Log.d(LOG, "subscribe, will use port number "+ portToUse);
                } else if (message.length == 6){
                    setOtherMacAddress(message);
                    Log.d(LOG, "setOtherMacAddress "+ message);
                } else if (message.length == 16) {
                    setOtherIPAddress(message);
                    Log.d(LOG, "setOtherIPAddress "+ message);
                } else if (message.length > 16) {
                    String messageIn = new String(message);
                    Log.d(LOG, "Message IN:" + messageIn);
                    if(messageIn.contains("subscriber") && messageIn.contains("establishingRole")) {
                        Log.d(LOG, "YES" + messageIn);
                        isPublisher = false;
                        findViewById(R.id.btnConnectSub).setVisibility(View.VISIBLE);
                        hasEstablishedPublisherAndSubscriber = true;
                        publishDiscoverySession = null;
                        //requestWiFiConnection();
                    }
                    //setMessage(message);
                    //Toast.makeText(MainActivity.this, "message received", Toast.LENGTH_LONG).show();
                }
            }
        }, null);
    }



    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG, "MainActivity stopped");
    }


    private void closeSession() {

        if (publishDiscoverySession != null) {
            publishDiscoverySession.close();
            publishDiscoverySession = null;
        }

        if (subscribeDiscoverySession != null) {
            subscribeDiscoverySession.close();
            subscribeDiscoverySession = null;
        }

        if (wifiAwareSession != null) {
            wifiAwareSession.close();
            wifiAwareSession = null;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG, "App onDestroy");
        //TODO: remove peer from list
    }





    /**
     * Helper to set the status field.
     */
    private void setMessage(byte[] msg) {
        String outmsg = new String(msg).replace("messageToBeSent: ","");
        EditText editText = findViewById(R.id.eTMsg);
        editText.setText(outmsg);
    }


    private void setMacAddress(byte[] mac) {
        myMac = mac;
        String macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
        EditText editText = findViewById(R.id.eTYourMac);
        editText.setText(macAddress);
    }


    private void setOtherMacAddress(byte[] mac) {
        otherMac = mac;
        macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);

        EditText editText = findViewById(R.id.eTOtherMac);
        editText.setText(macAddress);
    }


    private void setOtherIPAddress(byte[] ip) {
        otherIP = ip;
    }


    public int byteToPortInt(byte[] bytes){
        return ((bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF));
    }



    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestWiFiConnection() {
        if(!hasEstablishedPublisherAndSubscriber){
            Log.d(LOG, "hasestablished not");
            //establishWhoIsPublisherAndSubscriber();
        }
        if (isPublisher){
            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(publishDiscoverySession, peerHandle)
                    //.setPort(Constants.SERVER_PORT)
                    .build();
            Log.d(LOG, "This devices is publisher");

        } else{
            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(subscribeDiscoverySession, peerHandle)
                    .build();

            Log.d(LOG, "This devices is subscriber");
        }
        //connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (networkSpecifier == null) {
            Log.d(LOG, "No NetworkSpecifier Created ");
            return;
        }
        NetworkRequest myNetworkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
                .setNetworkSpecifier(networkSpecifier)
                .build();
        Toast.makeText(context, "networkrequest build", Toast.LENGTH_LONG).show();

        connectivityManager.requestNetwork(myNetworkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network_) {
                super.onAvailable(network);
                //Toast.makeText(context, "onAvaliable", Toast.LENGTH_LONG).show();
                Log.d(LOG, "onAvaliable + Network:" + network_.toString());
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.d(LOG, "entering onUnavailable ");
            }

            @Override
            public void onCapabilitiesChanged(Network network_, NetworkCapabilities networkCapabilities_) {
                //Toast.makeText(context, "onCapabilitiesChanged", Toast.LENGTH_LONG).show();
                Log.d(LOG, "onCapabilitiesChanged");
                networkCapabilities = networkCapabilities_;
                network = network_;
                peerAwareInfo = (WifiAwareNetworkInfo) networkCapabilities.getTransportInfo();
                setPeerIpv6(peerAwareInfo.getPeerIpv6Addr());
                Log.d(LOG, "Peeripv6: " + peerAwareInfo.getPeerIpv6Addr() );
            }

            @Override
            public void onLosing(Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
                Log.d(LOG, "losing Network");
            }

            @Override
            public void onLost(Network network_) {
                super.onLost(network);
                //Toast.makeText(context, "onLost", Toast.LENGTH_LONG).show();
                Log.d(LOG, "onLost");
            }
        });
    }

    private void setPeerIpv6(Inet6Address ipv6){
        this.peerIpv6 = ipv6;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void openChat(int position){

        //keyPair.toString();
        //sslContext.toString();
        Intent intentChat = new Intent(this, ChatActivity.class);
        intentChat.putExtra("position", position);
        //intentChat.putExtra("keypair", keyPair.toString());
        //intentChat.putExtra("sslContext", sslContext.toString());
        //intentChat.putExtra("IPV6_Address", peerIpv6.toString());
        startActivity(intentChat);
    }


   public static Inet6Address getPeerIpv6() {
        return peerIpv6;
    }
/*
    public static List<PeerHandle> getPeerHandleList(){
        return peerHandleList;
    }

    public static List<String> getMacAddressesList(){
        return otherMacList;
    }
*/

/*    public static PublishDiscoverySession getPublishDiscoverySession(){
        return publishDiscoverySession;
    }

    public static SubscribeDiscoverySession getSubscribeDiscoverySession(){
        return subscribeDiscoverySession;
    }*/

    private boolean canAccessLocationFine() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }


    private boolean canAccessLocationCoarse() {
        return(hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
    }


    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
    }



}

/*

    */
/**
     * App Permissions for Coarse Location
     **//*

    private void setupPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String[] permissionsWeNeed = new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION };
                requestPermissions(permissionsWeNeed, MY_PERMISSION_COARSE_LOCATION_REQUEST_CODE);
                Log.d(LOG, " setupPermissions 1, ACCESS_COARSE_LOCATION");
            }
            Log.d(LOG, " setupPermissions 2, ACCESS_COARSE_LOCATION");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String[] permissionsWeNeed = new String[]{ Manifest.permission.ACCESS_FINE_LOCATION };
                requestPermissions(permissionsWeNeed, MY_PERMISSION_FINE_LOCATION_CODE);
                Log.d(LOG, " setupPermissions 1, ACCESS_FINE_LOCATION");
            }
            Log.d(LOG, " setupPermissions 2, ACCESS_FINE_LOCATION");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_COARSE_LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(LOG, "Permission for location granted.");
                    return;

                } else {
                    Log.i(LOG, "Permission for location not granted. NAN can't run.");
                    finish(); //WHY: finish?
                }
            }

            case MY_PERMISSION_FINE_LOCATION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(LOG, "Fine location permission granted");
                    return;
                } else {
                    Log.i(LOG, "Fine location permission not granted");
                }
            }

*/
/*            case MY_PERMISSION_NETWORK_STATE_CODE: { //WHY: må vi ha denne?
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(LOG, "Network state permission granted");
                    return;

                } else {
                    Log.i(LOG, "Network state permission not granted");
                }
            }*//*

        }
    }*/
