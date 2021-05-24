package com.example.testaware.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
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
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testaware.AppServer;
import com.example.testaware.ConnectionHandler;
import com.example.testaware.IdentityHandler;
import com.example.testaware.TestChatActivity;
import com.example.testaware.listeners.BooleanChangedListener;
import com.example.testaware.listeners.BooleanObserver;

import com.example.testaware.listeners.SSLContextedObserver;
import com.example.testaware.models.Contact;
import com.example.testaware.listitems.ChatListItem;
import com.example.testaware.Constants;
import com.example.testaware.R;
import com.example.testaware.adapters.ChatsListAdapter;


import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import lombok.Getter;

import static java.lang.System.currentTimeMillis;


public class MainActivity extends AppCompatActivity  {


    private WifiAwareManager wifiAwareManager;
    private WifiAwareSession wifiAwareSession;
    @Getter
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


    private int  portToUse;

    //private PeerHandle peerHandle;
    private byte[] myMac;
    private long myMacDeci;

    private final int MAC_ADDRESS_MESSAGE = 11;
    private ConnectivityManager connectivityManager;
    private NetworkSpecifier networkSpecifier;


    private final int MESSAGE = 7;
    private final int MESSAGEPORT = 9;

    private final int MESSAGEGOTOCHAT = 5;
    private final int MESSAGESTARTCON = 11;
    private NetworkCapabilities networkCapabilities;
   //0 private Network network;

    private WifiAwareNetworkInfo peerAwareInfo;

    private static Inet6Address peerIpv6 ;
    private Inet6Address myIP;

    //private String macAddress;



    private static final int MY_PERMISSION_COARSE_LOCATION_REQUEST_CODE = 88;
    private static final int MY_PERMISSION_FINE_LOCATION_CODE = 99;
    private static final int MY_PERMISSION_NETWORK_STATE_CODE = 77;

    private static final int MY_PERMISSION_EXTERNAL_REQUEST_CODE = 99;


    private KeyPair keyPair;

    @Getter
    //private String role;
    //boolean isPublisher = false;
    //private String role = "subscriber";
    //boolean isPublisher = false;
    private String role = "publisher";
    boolean isPublisher = true;

    private String LOG = "Log-Main";



    @Getter
    private ArrayList<Contact> contacts;

    @Getter
    private ConnectionHandler connectionHandler;


    @Getter
    private SSLContextedObserver sslContextedObserver;

    long startApp = 0;
    long discovered = 0;
    long available = 0;
    long onUnavailable = 0;

    long timeToDiscoverServiceAfterAttacheToClusterStarted;
    long timeToGetFullDataPathAfterAttacheToClusterStarted;
    long diffSSLContext;

    @Getter
    private AppServer appServer;

    private int localPortServer;

    //private boolean wifiConnectionRequested = false;

    private HashMap<PeerHandle, String> hashMapPeerHandleKeyAndMac;
    private HashMap<String, Network> hashMapOfNetworks;
    private HashMap<String, Integer> hashMapMacWithPort;
    private HashMap<PeerHandle, byte[]> peerAndMacByte;

    private List<PeerHandle> requestedConnectionList;
    private List<PeerHandle> listOfPeersPortIsSentTo; // TODO: change, have multiple peer handles
    private List<String> listConnectionInitiatedMacs;
    private List<String> connectionInitiatedMac;
    private List<String> listConnectionWifiRequestedMacs;
    public List<PeerHandle> publishDiscoveredPeers;
    public List<PeerHandle> subscribeDiscoveredPeers;
    private List<PeerHandle> discoveredDevices = new ArrayList<>();
    private List<PeerHandle> connectedDevices = new ArrayList<>();

   // private List<String> listMacMessageRequestSentFromPublisher = new ArrayList<>();

   // private int portToServer;
   // private boolean isPortToServerSet = false;

    //private HashMap<String, PeerHandle> hashMapPeerHandleAndMac = new HashMap<>();
    //private HashMap<Integer, PeerHandle> hashMapPort = new HashMap<>();
    //private HashMap<PeerHandle, Integer> hashMapPortAndPeerServer = new HashMap<>();
    //private HashMap<String, Integer> hashMapPortMac = new HashMap<>();

    int data_init ;
    int data_resp ;
    private long startAttached;
    private long timeToAttachToCluster;

    PeerHandle peer;
    byte [] msg;

    private BooleanObserver booleanObserver;


    @Getter
    private  int countervalue;
    private boolean counterValueChanged = false;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        if(startApp == 0){
            startApp = currentTimeMillis();
        }

        wifiAwareManager = null;
        wifiAwareSession = null;
        connectivityManager = null;
        networkSpecifier = null;
        publishDiscoverySession = null;
        subscribeDiscoverySession = null;

        hashMapPeerHandleKeyAndMac = new HashMap<>();
        hashMapOfNetworks = new HashMap<>();
        hashMapMacWithPort = new HashMap<>();
        peerAndMacByte = new HashMap<>();

        requestedConnectionList = new ArrayList<>();
        listOfPeersPortIsSentTo = new ArrayList<>();
        listConnectionInitiatedMacs = new ArrayList<>();
        connectionInitiatedMac = new ArrayList<>();
        listConnectionWifiRequestedMacs = new ArrayList<>();
        publishDiscoveredPeers = new ArrayList<>();
        subscribeDiscoveredPeers = new ArrayList<>();

        booleanObserver  = new BooleanObserver();

        setupPermissions();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        TextView tvRole = findViewById(R.id.tvRole);
        tvRole.setText(role);


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

        this.keyPair = IdentityHandler.getKeyPair();
        wifiAwareManager = (WifiAwareManager) getSystemService(Context.WIFI_AWARE_SERVICE);
        context = this;

        //attachToSession();

       final long[] sslContextChanged = new long[1];

        sslContextedObserver = new SSLContextedObserver();
        sslContextedObserver.setListener(sslContext -> {
            sslContextChanged[0] = currentTimeMillis();
            connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            attachToSession();

            Log.d("ELISE", "attached started" );

        });
        long sslContextStart = currentTimeMillis();
        sslContextedObserver.setSslContext(IdentityHandler.getSSLContext(this.context));
        diffSSLContext = sslContextChanged[0] - sslContextStart;


        addPeersToChatList();
        TestChatActivity.updateActivityMain(this);
        AppServer.updateActivity(this);



        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);



    }

    private boolean hasRequestedConnection(PeerHandle peerHandle){ //TODO:
        for (PeerHandle peer : requestedConnectionList)
            if (peer.equals(peerHandle)) {
                return true;
            }
        return false;

    }

    private void setUpNewConnection(){

        /*wifiAwareManager = null;
        wifiAwareSession = null;
        networkSpecifier = null;
        publishDiscoverySession = null;
        subscribeDiscoverySession = null;
        peerHandle = null;
        wifiConnectionRequested = false;
        publishConfig = null;
        subscribeConfig = null;
        connectivityManager = null;*/


        //wifiAwareManager = null;
        //connectivityManager = null;
        //
        //wifiConnectionRequested = false;
        //wifiAwareManager = (WifiAwareManager) getSystemService(Context.WIFI_AWARE_SERVICE);
        //connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        closeSession();
        attachToSession();
        addPeersToChatList();
        TestChatActivity.updateActivityMain(this);
        AppServer.updateActivity(this);
    }


    private void setupPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissionsWeNeed = new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION };
            requestPermissions(permissionsWeNeed, MY_PERMISSION_COARSE_LOCATION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_COARSE_LOCATION_REQUEST_CODE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                Toast.makeText(this, "Permission for location not granted. NAN can't run.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void addPeersToChatList(){
        int counter = 1;
        ArrayList<ChatListItem> userList = new ArrayList<>();
        List<String> macAddresses = new ArrayList<>();
        /*for(PeerHandle peerHandle : hashMapPeerHandleKeyAndMac.keySet()){
            String mac = hashMapPeerHandleKeyAndMac.get(peerHandle);
            if(!macAddresses.contains(mac)){
                String status = "Not discovered";
                if(discoveredDevices.contains(getPeerHandleFromMacAddress(mac))){
                    status = "Discovered";
                } else if(connectedDevices.contains(getPeerHandleFromMacAddress(mac))){
                    status = "Data path established";
                }
                ChatListItem chatListDevices = new ChatListItem("MAC:", mac, status);
                userList.add(chatListDevices);
                macAddresses.add(mac);
            }
        }*/
        for (String mac : hashMapPeerHandleKeyAndMac.values()){
            if(!macAddresses.contains(mac)){
                String status = "Not discovered";
                String cert = "Not discovered";

                if(counter ==1){
                    cert = "Certificate: Valid";
                } else if(counter == 2){
                    cert = "Certificate: Not valid";
                }

                List<PeerHandle> peerHandles = getPeerHandlesFromMacAddress(mac);
                if(peerHandles.size()>1){
                    if((discoveredDevices.contains(peerHandles.get(0)))||(discoveredDevices.contains(peerHandles.get(1)))){
                        status = "Discovered";
                    } else if((connectedDevices.contains(peerHandles.get(0)))||(connectedDevices.contains(peerHandles.get(1)))){
                        status = "Data path established";
                    }
                } else {
                    if(discoveredDevices.contains(peerHandles.get(0))){
                        status = "Discovered";
                    } else if(connectedDevices.contains(peerHandles.get(0))){
                        status = "Data path established";
                    }
                }

                ChatListItem chatListDevices = new ChatListItem("MAC:", mac, status, cert);
                userList.add(chatListDevices);
                macAddresses.add(mac);
            }
            counter +=1;
        }
       /* for (String macAddr : hashMapPeerHandleAndMac.keySet()){
            ChatListItem chatListDevices = new ChatListItem("MAC:", macAddr);
            userList.add(chatListDevices);
        }*/
        ChatsListAdapter chatListAdapter = new ChatsListAdapter(this, userList);
        ListView listViewChats = findViewById(R.id.listViewChats);
        listViewChats.setAdapter(chatListAdapter);
        listViewChats.setOnItemClickListener((parent, view, position, id) -> {
            String peerIpv6 = chatListAdapter.getChats().get(position).getPeerIpv6();
            MainActivity.this.openChat(position, peerIpv6);
        });
    }


    public void startPublishAndSubscribe(){
        subscribe();
        publish();
    }


    private void attachToSession(){
        startAttached = currentTimeMillis();

        wifiAwareManager.attach(new AttachCallback() {
            @Override
            public void onAttached(WifiAwareSession session) {
                long onAttached = currentTimeMillis();
                timeToAttachToCluster = onAttached - startAttached;
                super.onAttached(session);
                wifiAwareSession = session;

            }
            @Override
            public void onAttachFailed() {
                if(!counterValueChanged){
                    long onAttachedFailed = currentTimeMillis();
                    long resultAttachedFailed = onAttachedFailed - startAttached;

                    String outputText = "AppStartedTime:" + startApp + ":Discovery:" + 0 + ":onUnavailable:" + 0 + ":SSLContext:" + diffSSLContext + ":onAttachFailed:"+ resultAttachedFailed;

                    countervalue = 0;
                    BufferedWriter writerCounter;

                    try {
                        writerCounter = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/counter"));

                        FileReader file = new FileReader("/data/data/com.example.testaware/counter");
                        Scanner sc=new Scanner(file);
                        while(sc.hasNextLine()){
                            String line = sc.nextLine();
                            countervalue = Integer.parseInt(line);
                        }
                        sc.close();;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/onAvailable", true));
                        writerCounter = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/counter"));
                        writer.append("Counter:" + countervalue);
                        writer.append("\n");
                        writer.append(outputText);
                        writer.append("\n");
                        writer.close();

                        String counterText = String.valueOf(countervalue+1);
                        writerCounter.write(counterText);
                        writerCounter.close();
                        counterValueChanged = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

            }
        }, new IdentityChangedListener() {
            @Override
            public void onIdentityChanged(byte[] mac) {
                super.onIdentityChanged(mac);
                setMacAddress(mac);
                myMacDeci = getMacInDecimal(mac);

                startPublishAndSubscribe();
            }
        }, null);
    }
    private boolean wait = true;
private int counterRetriedSendMessagePort = 0;
    private int counterRetriedSendMessageOpenChat = 0;
    private boolean sendMessageOpenChatFailed;

    byte [] serviceSpecificInfoText = "StartingConnection".getBytes();
    private void publish () {

        PublishConfig publishConfig = new PublishConfig.Builder()
                .setServiceName(Constants.SERVICE_NAME)
                .setPublishType(PublishConfig.PUBLISH_TYPE_UNSOLICITED)
                //.setServiceSpecificInfo(serviceSpecificInfoText)
                .build();
        if(wifiAwareSession!=null){
            wifiAwareSession.publish(publishConfig, new DiscoverySessionCallback() {
                @Override
                public void onMessageSendFailed (int messageId){
                    if(messageId==MESSAGEPORT && counterRetriedSendMessagePort <=5){
                        counterRetriedSendMessagePort++;
                        publishDiscoverySession.sendMessage(peer, MESSAGEPORT, msg);
                    } else if(messageId == MESSAGEGOTOCHAT && counterRetriedSendMessageOpenChat <=5 ){
                        counterRetriedSendMessageOpenChat += 1;
                        Log.d(LOG, "Sending message goToChat failed");
                        byte [] message = "goToChat".getBytes();
                        publishDiscoverySession.sendMessage(peerHandlesStartChat.get(1), MESSAGEGOTOCHAT, message);
                        //sendMessageOpenChatFailed = true;
                        //booleanObserver.setMessageSentStatus(sendMessageOpenChatFailed);


                    } else if(counterRetriedSendMessagePort >5){
                        localPortServer = 1025;
                    } else if(counterRetriedSendMessageOpenChat >1) {
                        sendMessageOpenChatFailed = true;
                        booleanObserver.setMessageSentStatus(sendMessageOpenChatFailed);
                    } else if(messageId==MESSAGESTARTCON){
                        try {
                            BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/startConFailed", true));
                            writer.append("Counter:" + countervalue);
                            writer.append("\n");
                            writer.append("messageStartConFailed");
                            writer.append("\n");
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void onMessageSendSucceeded (int messageId){
                    if(messageId==MESSAGEPORT){
                        peer = null;
                        msg = null;
                        Log.d(LOG, "Sending message port success");
                    } if(messageId == MESSAGEGOTOCHAT){
                        sendMessageOpenChatFailed = false;
                        booleanObserver.setMessageSentStatus(sendMessageOpenChatFailed);
                    }
                }

                @Override
                public void onServiceDiscovered(PeerHandle peerHandle_, byte[] serviceSpecificInfo, List<byte[]> matchFilter) {
                super.onServiceDiscovered(peerHandle_, serviceSpecificInfo, matchFilter);
                Log.i("LOG-Test-Debugging", "publish onServiceDiscovered");
               // publishDiscoveredPeers.add(peerHandle_);
            }

            @Override
            public void onPublishStarted(PublishDiscoverySession session) {
                super.onPublishStarted(session);
                publishDiscoverySession = session;
            }

            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onMessageReceived(PeerHandle peerHandle, byte[] message) {
                super.onMessageReceived(peerHandle, message);
                if(!publishDiscoveredPeers.contains(peerHandle)){
                    publishDiscoveredPeers.add(peerHandle);
                }

                if(!discoveredDevices.contains(peerHandle)){
                    discoveredDevices.add(peerHandle);
                    addPeersToChatList();
                }
                publishDiscoverySession.sendMessage(peerHandle, MESSAGE, myMac);
                if (!listOfPeersPortIsSentTo.contains(peerHandle)){
                    if(appServer!=null){
                        sendPort(peerHandle);
                    }
                }
                String messageIn = new String(message);
               /* if (message.length == 2) {
                    portToUse = byteToPortInt(message);
                    Log.d(LOG, "subscribe, will use port number " + portToUse);
                } else*/
                if (messageIn.contains("goToChat")) {
                    Toast.makeText(context, "A Peer Client wants to talk to you", Toast.LENGTH_SHORT).show();
                    connectionInitiatedMac.add(hashMapPeerHandleKeyAndMac.get(peerHandle));
                } else if (message.length == 6) {
                    setOtherMacAddress(message);
                    String macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", message[0], message[1], message[2], message[3], message[4], message[5]);
                    if(!hashMapPeerHandleKeyAndMac.containsKey(peerHandle)){
                        hashMapPeerHandleKeyAndMac.put(peerHandle, macAddress);
                        if(!listConnectionInitiatedMacs.contains(macAddress)){

                            long peerMacDeci = getMacInDecimal(message);

                            if(myMacDeci >= peerMacDeci){
                                requestWiFiConnection(peerHandle, role);
                                Log.d(LOG, "requesting Connection for Publisher");

                                listConnectionInitiatedMacs.add(macAddress);
                                byte[] msgtosend = "startConnection".getBytes();
                                publishDiscoverySession.sendMessage(peerHandle, MESSAGESTARTCON, msgtosend);
                            }

                           /* requestWiFiConnection(peerHandle, role);

                            //listMacMessageRequestSentFromPublisher.add(macAddress);
                            Log.d(LOG, "requesting Connection for Publisher");*/

                            /*listConnectionInitiatedMacs.add(macAddress);
                            byte[] msgtosend = "startConnection".getBytes();
                            publishDiscoverySession.sendMessage(peerHandle, MESSAGE, msgtosend);   //OK*/

                        }
                    }
                   /* if (!hashMapPeerHandleAndMac.containsKey(macAddress)) {
                        hashMapPeerHandleAndMac.put(macAddress, peerHandle_);
                        addPeersToChatList();
                    }*/
                   /*if()
                    for (String macAddr : hashMapPeerHandleAndMac.keySet()) {
                        if (!connectedMac.contains(macAddr)) {
                            requestWiFiConnection(peerHandle_, role);

                            connectedMac.add(macAddr);
                            byte[] msgtosend = "startConnection".getBytes();
                            publishDiscoverySession.sendMessage(peerHandle_, MESSAGE, msgtosend);   //OK
                        }
                    }*/

                    /*for (String macAddr : hashMapPeerHandleAndMac.keySet()) {
                        if (!connectedMac.contains(macAddr)) {
                            requestWiFiConnection(peerHandle_, role);
                            connectedMac.add(macAddr);
                            byte[] msgtosend = "startConnection".getBytes();
                            publishDiscoverySession.sendMessage(peerHandle_, MESSAGE, msgtosend);   //OK
                        }
                    }*/
                    /*if (!wifiConnectionRequested) { //TODO: hashmap wifiConnected and peerhandle
                        requestWiFiConnection(peerHandle_, role);
                        byte[] msgtosend = "startConnection".getBytes();
                        publishDiscoverySession.sendMessage(peerHandle_, MESSAGE, msgtosend);       //OK
                    }*/


                } else if (message.length == 16) {
                    //setOtherIPAddress(message);
                    //Log.d(LOG, "setOtherIPAddress " + message);
                }
            }

            @Override
            public void onSessionConfigFailed () {
                  /*  if(!firstTimeRestarted){

                        finish();
                        startActivity(getIntent());
                        firstTimeRestarted = true;
                    }*/
                /*finish();
                startActivity(getIntent());*/
                /*PublishConfig publishConfigUpdate = new PublishConfig.Builder()
                        .setServiceName(Constants.SERVICE_NAME)
                        .setPublishType(PublishConfig.PUBLISH_TYPE_UNSOLICITED)
                        //.setServiceSpecificInfo(serviceSpecificInfoText)
                        .build();

                publishDiscoverySession.updatePublish(publishConfigUpdate);*/

                }

            }, null);

        }
    }




    private void subscribe(){
        SubscribeConfig subscribeConfig = new SubscribeConfig.Builder()
                .setServiceName(Constants.SERVICE_NAME)
                .setSubscribeType(SubscribeConfig.SUBSCRIBE_TYPE_PASSIVE)
                .build();
        if(wifiAwareSession!= null){
            wifiAwareSession.subscribe(subscribeConfig, new DiscoverySessionCallback() {

                @Override
                public void onSubscribeStarted(@NonNull SubscribeDiscoverySession session) {
                    super.onSubscribeStarted(session);
                    subscribeDiscoverySession = session;
                    //Log.i("LOG-Test-Debugging", "subscribe started");
                }

                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void onServiceDiscovered(PeerHandle peerHandle_, byte[] serviceSpecificInfo, List<byte[]> matchFilter) {
                    super.onServiceDiscovered(peerHandle_, serviceSpecificInfo, matchFilter);
                    serviceSpecificInfoText = "noConnection".getBytes();
                    //Log.d(LOG, new String(serviceSpecificInfo));

                    if(!publishDiscoveredPeers.contains(peerHandle_)){
                        subscribeDiscoveredPeers.add(peerHandle_);
                    }

                    if(!discoveredDevices.contains(peerHandle_)){
                        discoveredDevices.add(peerHandle_);
                        addPeersToChatList();
                    }
                    if(discovered == 0){
                        discovered = currentTimeMillis();
                        //("TESTING-LOG-TIME-DISCOVERY", String.valueOf(discovered));
                    }

                    if (subscribeDiscoverySession != null && peerHandle_ != null) {
                        subscribeDiscoverySession.sendMessage(peerHandle_, MAC_ADDRESS_MESSAGE, myMac);      //Hele tiden
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void onMessageReceived(PeerHandle peerHandle, byte[] message) {
                    super.onMessageReceived(peerHandle, message);

                    String messageIn = new String(message);
                    //Log.d(LOG, "Message Received " + messageIn);
                    if(message.length == 2) {
                        portToUse = byteToPortInt(message);
                        //Log.d(LOG, "subscribe, will use port number "+ portToUse);
                    } else if (messageIn.contains("goToChat")) {
                        Toast.makeText(context, "A Peer wants to talk to you", Toast.LENGTH_SHORT).show();
                        connectionInitiatedMac.add(hashMapPeerHandleKeyAndMac.get(peerHandle));
                    }else if (message.length == 6){
                        String macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", message[0], message[1], message[2], message[3], message[4], message[5]);

                        setOtherMacAddress(message);
                        if(!hashMapPeerHandleKeyAndMac.containsKey(peerHandle)){
                            hashMapPeerHandleKeyAndMac.put(peerHandle, macAddress);

                            peerAndMacByte.put(peerHandle, message);
                        }


                    } else if (messageIn.contains("startConnection") ) {
                        //Log.d(LOG, "Message IN:" + messageIn);
                        String mac = hashMapPeerHandleKeyAndMac.get(peerHandle);
                        //String mac = getMacAddressFromPeerHandle(peerHandle);


                        long peerMacDeci = getMacInDecimal(peerAndMacByte.get(peerHandle));

                        if(myMacDeci < peerMacDeci){
                            requestWiFiConnection(peerHandle, "subscriber");
                            //Log.d(LOG, "requesting Connection for Subscriber:");
                        }

                        /*requestWiFiConnection(peerHandle, "subscriber");
                        Log.d(LOG, "requesting Connection for Subscriber:");*/

                       /* if (!listConnectionWifiRequestedMacs.contains(mac)) {
                            listConnectionWifiRequestedMacs.add(mac);
                            requestWiFiConnection(peerHandle, "subscriber");
                            Log.d(LOG, "requesting Connection for Subscriber:");
                        }*/

                    } else if(messageIn.contains("ServerPort:")) {
                        //Log.d(LOG, "onMessageReceived, server port:" + messageIn);
                        Integer portToServer = Integer.parseInt(messageIn.split(":")[1]);
                        String mac = hashMapPeerHandleKeyAndMac.get(peerHandle);
                        if(!hashMapMacWithPort.containsKey(mac)){
                            hashMapMacWithPort.put(mac, portToServer);
                        }
                       /* if(!hashMapPortAndPeerServer.containsValue(peerHandle)){
                            hashMapPortAndPeerServer.put(peerHandle, portToServer);
                        }*/
                    }
                    else if (message.length == 16) {
                        //setOtherIPAddress(message);
                        //Log.d(LOG, "setOtherIPAddress "+ message);
                    }
                }
            }, null);
        }

    }


    private long getMacInDecimal(byte [] mac){
        BigInteger one;
        one = new BigInteger(mac);
        String strResult = one.toString();
        long value = 0 ;
        int counter = strResult.length();
        for (int i = 0; i < strResult.length(); i++){
            int number = Integer.parseInt(String.valueOf(strResult.charAt(i)));
            value += number * (long) Math.pow(10, counter-1);
            counter -= 1;
        }

        return value;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG, "MainActivity stopped");
    }

    @Override
    protected void onResume() {
        super.onResume();
        addPeersToChatList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeSession();
        Log.d(LOG, "App onDestroy");
        Log.d("TESTING-LOG-TIME-TLS-onDestroy", "App destroyed");
        //TODO: remove peer from list
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


    private List<PeerHandle> getPeerHandlesFromMacAddress(String mac){
        List<PeerHandle> peerHandles = new ArrayList<>();
        for (PeerHandle peerHandle : hashMapPeerHandleKeyAndMac.keySet())
            if (hashMapPeerHandleKeyAndMac.get(peerHandle).equals(mac)) {
                peerHandles.add(peerHandle);
            }
        return peerHandles;
    }

    private String getMacAddressFromNetwork(Network network){
        for (String macAddr : hashMapOfNetworks.keySet())
            if (hashMapOfNetworks.get(macAddr).equals(network)) {
                return macAddr;
            }
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestWiFiConnection(PeerHandle peerHandle, String role) {
        //Log.d(LOG, "requestWiFiConnection");
        String peerRole = role;

        if(hashMapOfNetworks.keySet().equals(hashMapPeerHandleKeyAndMac.get(peerHandle))){
            //Log.d(LOG, "There already exists a network with this peer");
        }

        if(role.equals("subscriber")){
            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(subscribeDiscoverySession, peerHandle)
                    .setPskPassphrase("password")
                    .build();

            //Log.d(LOG, "This devices is subscriber");
        } else {
           // listConnectionWifiRequestedMacs.add(hashMapPeerHandleKeyAndMac.get(peerHandle));
            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(publishDiscoverySession, peerHandle)
                    .setPskPassphrase("password")
                    .setTransportProtocol(6)
                  //  .setPort(.getLocalPort())
                    .build();

            //Log.d(LOG, "This devices is publisher");
        }

        if (networkSpecifier == null) {
            //Log.d(LOG, "No NetworkSpecifier Created ");
            return;
        }

        NetworkRequest myNetworkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
                .setNetworkSpecifier(networkSpecifier)
                .build();

        connectivityManager.requestNetwork(myNetworkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                if( available == 0) {
                    available = currentTimeMillis();
                }
                super.onAvailable(network);

                //List<Network> listActiveNetworks = (List<Network>) connectivityManager.getActiveNetwork();

                if(!connectedDevices.contains(peerHandle)){
                    connectedDevices.add(peerHandle);
                    discoveredDevices.remove(peerHandle);
                }


                new Handler(Looper.getMainLooper()).post(()-> {
                    addPeersToChatList();
                });
                hashMapOfNetworks.put(hashMapPeerHandleKeyAndMac.get(peerHandle), network); // mac = 0 ? TODO is this fixed?
                Toast.makeText(context, "On Available!", Toast.LENGTH_SHORT).show();
                //Log.d(LOG, "On available for " + peerRole);




                long timeBeforeAttachingToCluster = startAttached - startApp;

                /*startApp;
                startAttached;
                discovered;*/


                timeToGetFullDataPathAfterAttacheToClusterStarted = available - startAttached;
                timeToDiscoverServiceAfterAttacheToClusterStarted = discovered - startAttached;


                String outputText = "AppStartedTime:" + startApp + ":TimeBeforeAttachingToCluster:" + timeBeforeAttachingToCluster + ":Discovery:" + timeToDiscoverServiceAfterAttacheToClusterStarted + ":Available:" + timeToGetFullDataPathAfterAttacheToClusterStarted + ":SSLContext:" + diffSSLContext + ":onAttached:"+ timeToAttachToCluster;

                countervalue = 0;
                BufferedWriter writerCounter;

                try {
                        //writerCounter = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/counter"));
                        //writerCounter.close();

                    FileReader file = new FileReader("/data/data/com.example.testaware/counter");
                    Scanner sc=new Scanner(file);
                    while(sc.hasNextLine()){
                        String line = sc.nextLine();
                        countervalue = Integer.parseInt(line);
                    }
                    sc.close();;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/onAvailable", true));
                    writerCounter = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/counter"));
                    writer.append("Counter:" + countervalue);
                    writer.append("\n");
                    writer.append(outputText);
                    writer.append("\n");
                    writer.close();

                    String counterText = String.valueOf(countervalue+1);
                    writerCounter.write(counterText);
                    writerCounter.close();
                    counterValueChanged = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }



                if(appServer == null){
                    appServer = new AppServer(sslContextedObserver.getSslContext(), network);

                }
                //connectionHandler = new ConnectionHandler(getApplicationContext(), sslContextedObserver.getSslContext(), keyPair, appServer, isPublisher);

            }


            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.d(LOG, "entering onUnavailable ");
                Toast.makeText(context, "onUnavailable", Toast.LENGTH_SHORT).show();
                if(!counterValueChanged){
                    if( onUnavailable == 0){
                        onUnavailable = currentTimeMillis();
                        long diffStartUnAvailable = onUnavailable - startApp;
                        timeToDiscoverServiceAfterAttacheToClusterStarted = discovered - startApp;


                        String outputText = "AppStartedTime:" + startApp + ":Discovery:"+ timeToDiscoverServiceAfterAttacheToClusterStarted + ":onUnavailable:" + diffStartUnAvailable + ":SSLContext:" + diffSSLContext + "Cluster:"+ timeToAttachToCluster;

                        countervalue = 0;
                        BufferedWriter writerCounter;

                        try {

                            writerCounter = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/counter"));
                            FileReader file = new FileReader("/data/data/com.example.testaware/counter");
                            Scanner sc=new Scanner(file);
                            while(sc.hasNextLine()){
                                String line = sc.nextLine();
                                countervalue = Integer.parseInt(line);
                            }
                            sc.close();;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        try {
                            BufferedWriter writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/onAvailable", true));
                            writerCounter = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/counter"));
                            writer.append("Counter:" + countervalue);
                            writer.append("\n");
                            writer.append(outputText);
                            writer.append("\n");
                            writer.close();

                            String counterText = String.valueOf(countervalue+1);
                            writerCounter.write(counterText);
                            writerCounter.close();
                            counterValueChanged = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                }
            }

            @Override
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);

                try {
                    NetworkInterface awareNi = NetworkInterface.getByName(linkProperties.getInterfaceName());
                    Enumeration<InetAddress> inetAddresses = awareNi.getInetAddresses();

                    while (inetAddresses.hasMoreElements()) {
                        InetAddress addr = inetAddresses.nextElement();
                        if (addr instanceof Inet6Address) {
                            Log.d("myTag", "netinterface ipv6 address: " + addr.toString());
                            if (addr.isLinkLocalAddress()) {
                                //ipv6 = Inet6Address.getByAddress("WifiAware",addr.getAddress(),awareNi);
                                myIP = Inet6Address.getByAddress("WifiAware",addr.getAddress(),awareNi);
                            }
                        }
                    }

                } catch (SocketException | UnknownHostException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCapabilitiesChanged(Network network_, NetworkCapabilities networkCapabilities) {
                Log.d(LOG, "onCapabilitiesChanged with Network " + network_);
                peerAwareInfo = (WifiAwareNetworkInfo) networkCapabilities.getTransportInfo();
                setPeerIpv6(peerAwareInfo.getPeerIpv6Addr());
                Log.d(LOG, "Peeripv6: " + peerAwareInfo.getPeerIpv6Addr() );

            }


            @Override
            public void onLost(Network network) {
                super.onLost(network);
                String mac = getMacAddressFromNetwork(network);
                Log.d("LOG-Test-Debugging", "onLOST: " + mac + network.toString());
                listConnectionInitiatedMacs.remove(mac);
                //listConnectionWifiRequestedMacs.remove(mac);
                connectionInitiatedMac.remove(mac);
                hashMapOfNetworks.remove(mac);
                hashMapMacWithPort.remove(mac);



                List<PeerHandle> peersToRemove = new ArrayList<>();
                for (PeerHandle peer : hashMapPeerHandleKeyAndMac.keySet()){
                    if (hashMapPeerHandleKeyAndMac.get(peer).equals(mac)){
                        peersToRemove.add(peer);
                    }
                }
                for (PeerHandle peerToRemove: peersToRemove){
                    hashMapPeerHandleKeyAndMac.remove(peerToRemove);
                    listOfPeersPortIsSentTo.remove(peerToRemove);
                    publishDiscoveredPeers.remove(peerToRemove);
                    subscribeDiscoveredPeers.remove(peerToRemove);
                    requestedConnectionList.remove(peerToRemove);
                    peerAndMacByte.remove(peerToRemove);
                }

                new Handler(Looper.getMainLooper()).post(()-> {
                    addPeersToChatList();
                });
                if(hashMapPeerHandleKeyAndMac.isEmpty()){
                    Log.d(LOG, "HashMap empty? yes");
 /*                   appServer.stop();
                    appServer = null;
   */
                    new Handler(Looper.getMainLooper()).post(()-> {
                        setUpNewConnection();
                    });
                }
            }
        });
    }

    private boolean firstTimeRestarted = false;

    private void setPeerIpv6(Inet6Address ipv6){
        peerIpv6 = ipv6;
    }

    private void sendPort(PeerHandle peerHandle){
        listOfPeersPortIsSentTo.add(peerHandle);
        String portStr = "ServerPort:"+ localPortServer;
        byte [] message = portStr.getBytes();
        peer = peerHandle;
        msg = message;
        publishDiscoverySession.sendMessage(peerHandle, MESSAGEPORT, message);
    }



    public void setServerPort(Network network, String role, int port){
        localPortServer = port;
        for (PeerHandle peerHandle: hashMapPeerHandleKeyAndMac.keySet()){
            if(!listOfPeersPortIsSentTo.contains(peerHandle)){
                sendPort(peerHandle);
            }
        }
        //if(role.equals("publisher")){

        //hashMapMacWithPort.put(getMacAddressFromNetwork(network), localPortServer);
        //PeerHandle peer = getPeerHandleFromMacAddress(getMacAddressFromNetwork(network));

        //PeerHandle peer = hashMapPeerHandleAndMac.get(getMacAddressFromNetwork(network));
       /* if(!role.equals("server")){

            hashMapMacWithPort.put(getMacAddressFromNetwork(network), localPortServer);
        }*/
        //sendPort(peer);
        //}
    }

    private Integer getPortFromMac(String mac){
        for (String macAddress: hashMapMacWithPort.keySet()){
            if(macAddress.equals(mac)){
                Integer integer = hashMapMacWithPort.get(macAddress);
                return integer;
            }
        }
        return 1025;
    }

private List<PeerHandle> peerHandlesStartChat;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void openChat(int position, String peerMac){
        //hashMapMacWithPort.put(peerMac, portToServer);
        Intent intentChat = new Intent(this, TestChatActivity.class);  //TODO: change back to chat activity just testing
        intentChat.putExtra("position", position);
        intentChat.putExtra("counter", countervalue);
        List<PeerHandle> peerHandles = getPeerHandlesFromMacAddress(peerMac);

        int port = getPortFromMac(peerMac);
        /*while(wait){
            Log.d(LOG, "Waiting");
        }*/

        peerHandlesStartChat = peerHandles;
        if (!connectionInitiatedMac.contains(peerMac)){
            byte [] message = "goToChat".getBytes();

            publishDiscoverySession.sendMessage(peerHandlesStartChat.get(0), MESSAGEGOTOCHAT, message);
            //subscribeDiscoverySession.sendMessage(peer, MESSAGEGOTOCHAT, message);

        }

        if(connectionInitiatedMac.contains(peerMac)){
            intentChat.putExtra("Role", "Server");
            intentChat.putExtra("port", localPortServer);
            Log.d(LOG, "PORT openChat as Server " + port);
            startActivity(intentChat);
        }


        booleanObserver.setListener(new BooleanChangedListener() {
            @Override
            public void onBooleanChanged(boolean messageSentStatus) {
                 if (sendMessageOpenChatFailed) {
                    Toast.makeText(context, "Message to open chat did not send. You are server", Toast.LENGTH_LONG).show();

                    intentChat.putExtra("Role", "Server");
                    intentChat.putExtra("port", localPortServer);
                    Log.d(LOG, "PORT openChat as Server " + port);
                    startActivity(intentChat);

                } else if(!sendMessageOpenChatFailed){
                     intentChat.putExtra("Role", "Client");
                     intentChat.putExtra("port", port);
                     Log.d(LOG, "PORT openChat Client " + port);
                     startActivity(intentChat);
                 }
            }
        });


       // Contact contact = new Contact(IdentityHandler.getCertificate());
       // intentChat.putExtra("contact", contact);

    }


    public static Inet6Address getPeerIpv6() {
        return peerIpv6;
    }


    private boolean canAccessLocationFine() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }


    private boolean canAccessLocationCoarse() {
        return(hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
    }


    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
    }




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
        //otherMac = mac;
        String macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);

        EditText editText = findViewById(R.id.eTOtherMac);
        editText.setText(macAddress);
    }


    private void setOtherIPAddress(byte[] ip) {
        //otherIP = ip;
    }



    public int byteToPortInt(byte[] bytes){
        return ((bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF));
    }

/*
    @Override
    public void onConnect() {

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onPacket(Message message) {

    }

    @Override
    public void onServerPacket(AbstractPacket packet) {

    }

 */

    public KeyPair getKeyPair(){
        return keyPair;
    }



}
