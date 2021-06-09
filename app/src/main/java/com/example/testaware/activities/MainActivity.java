package com.example.testaware.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testaware.AppServer;
import com.example.testaware.ConnectionHandler;
import com.example.testaware.IdentityHandler;
import com.example.testaware.listeners.OnSSLContextChangedListener;
import com.example.testaware.listeners.BooleanChangedListener;
import com.example.testaware.listeners.BooleanObserver;
import com.example.testaware.activities.TestChatActivity;
import com.example.testaware.listeners.SSLContextedObserver;
import com.example.testaware.models.Contact;
import com.example.testaware.listitems.ChatListItem;
import com.example.testaware.Constants;
import com.example.testaware.R;
import com.example.testaware.adapters.ChatsListAdapter;
import com.example.testaware.offlineAuth.Decoder;
import com.example.testaware.offlineAuth.InitPeerAuthConn;
import com.example.testaware.offlineAuth.PeerAuthServer;
import com.example.testaware.offlineAuth.PeerSigner;
import com.example.testaware.offlineAuth.VerifyCredentials;
import com.example.testaware.offlineAuth.VerifyUser;
import com.example.testaware.models.Message;


import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.io.Serializable;
import java.net.Inet6Address;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.security.auth.x500.X500Principal;

import java.util.Map;
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
    private TextView tvRole;

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
    private final int PUBLIC_KEY   = 10;
    private final int  SIGNED_STRING  = 44;
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

    private String receivedPubKeyToBeSigned;
    private boolean IamPeerAuth;

    private KeyPair keyPair;
    //peer auth variables
    private String signedStringToSend;
    private String encodedPubKeyToSend;
    private String randomStringToSend;
    private String authenticatorKey;

    private String receivedSignedString;
    private String receivedPubKey;              //TODO : not variables for all, change for multiple connections
    private String receivedString;
    private String signedKeyReceived;
    private HashMap<String, String> peerSignedKeyAndAuthKeyReceived = new HashMap<>();
    private HashMap<String, Boolean> dontAuhtenticate = new HashMap<>();

    private HashMap<PeerHandle, Boolean> peerHandlesToUse = new HashMap<>();
    @Getter
    private String certSelfSigned="false";

    @Getter
    //private String role;
    //boolean isPublisher = false;
    //private String role = "subscriber";
    //boolean isPublisher = false;
    private String role = "publisher";
    boolean isPublisher = true;

    private String LOG = "Log-Main";

    @Getter
    private String peerAuthenticated ="false";

    @Getter
    private PeerAuthServer peerAuthServer;


    @Getter
    private ArrayList<Contact> contacts;

   // @Getter
   // private ConnectionHandler connectionHandler;

   // @Getter
   // private ConnectionHandler peerAuthConnectionHandler;

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

    private long requestPeerAuthStartTime;
    private long requestPeerAuthStopTime ;
    private long requestPeerAuthConnStopTime;

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

        setupPermissions();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        tvRole = findViewById(R.id.tvRole);


        if (canAccessLocationFine()) {
            //(LOG,"Can access fine location");
        }
        else {
            requestPermissions(LOCATION_PERMS, LOCATION_REQUEST_FINE);
        }

        if (canAccessLocationCoarse()) {
            //(LOG,"Can access coarse location");
        }
        else {
            requestPermissions(LOCATION_PERMS_COARSE, LOCATION_REQUEST_COARSE);
        }

        if(PeerSigner.getSignedKeySelf().isEmpty()){
            IamPeerAuth = false;
        }
        else{
            IamPeerAuth = true;
        }


        this.keyPair = IdentityHandler.getKeyPair();
        wifiAwareManager = (WifiAwareManager) getSystemService(Context.WIFI_AWARE_SERVICE);
        context = this;

      //  attachToSession();

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

        certSelfSigned = certSelfSigned(IdentityHandler.getCertificate());
        diffSSLContext = sslContextChanged[0] - sslContextStart;



        setTextView();

        addPeersToChatList();
        TestChatActivity.updateActivityMain(this);
        AppServer.updateActivity(this);
        PeerAuthServer.updateActivity(this);


        booleanObserver  = new BooleanObserver();
      //  getMacAddress();


    }
  /*  private void getMacAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Log.i(LOG, "network"+ networkInterface.getName());
                if (networkInterface.getHardwareAddress() != null) {
                    byte[] bytes = networkInterface.getHardwareAddress();
                    StringBuilder builder = new StringBuilder();
                    for (byte b : bytes) {
                        builder.append(String.format("%02X:", b));
                    }

                    if (builder.length() > 0) {
                        builder.deleteCharAt(builder.length() - 1);
                    }
                    Log.i(LOG, "network" + builder.toString());
                }

/*
                if (TextUtils.equals(networkInterface.getName(), interfaceName)) {
                    byte[] bytes = networkInterface.getHardwareAddress();
                    StringBuilder builder = new StringBuilder();
                    for (byte b : bytes) {
                        builder.append(String.format("%02X:", b));
                    }

                    if (builder.length() > 0) {
                        builder.deleteCharAt(builder.length() - 1);
                    }

                    return builder.toString();


                }


            }

        } catch (SocketException e) {

        }
    }
    */


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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
          // And if we're on SDK M or later...
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Ask again, nicely, for the permissions.
            String[] permissionsWeNeed = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE };
            requestPermissions(permissionsWeNeed, MY_PERMISSION_EXTERNAL_REQUEST_CODE);
          }
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


        for (String mac : hashMapPeerHandleKeyAndMac.values()) {
            if (!macAddresses.contains(mac)) {
                String status = "Not discovered";
                String cert = "Not discovered";

                if (counter == 1) {
                    cert = "Certificate: Valid";
                } else if (counter == 2) {
                    cert = "Certificate: Not valid";
                }
                List<PeerHandle> peerHandles = getPeerHandlesFromMacAddress(mac);
                if (peerHandles.size() > 1) {
                    if ((discoveredDevices.contains(peerHandles.get(0))) || (discoveredDevices.contains(peerHandles.get(1)))) {
                        status = "Discovered";
                    } else if ((connectedDevices.contains(peerHandles.get(0))) || (connectedDevices.contains(peerHandles.get(1)))) {
                        status = "Data path established";
                    }
                } else {
                    if (discoveredDevices.contains(peerHandles.get(0))) {
                        status = "Discovered";
                    } else if (connectedDevices.contains(peerHandles.get(0))) {
                        status = "Data path established";
                    }
                }


                ChatListItem chatListDevices = new ChatListItem("MAC:", mac, status, cert);
                userList.add(chatListDevices);
                macAddresses.add(mac);

                counter += 1;

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
                Log.d(LOG, "peer ipv6 from chatview"+ peerIpv6);

                /*Button reqPeerAuthConnBtn = view.findViewById(R.id.btnReqPeerAuthConn);

                reqPeerAuthConnBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //(LOG,"onclick");


                });
                */
            });


        }

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


    private void publish() {


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
                    //("LOG-Test-Debugging", "publish onServiceDiscovered");
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
                    if (!publishDiscoveredPeers.contains(peerHandle)) {
                        publishDiscoveredPeers.add(peerHandle);
                        publishDiscoverySession.sendMessage(peerHandle, MESSAGE, myMac);  //TODO : check peerhandle_ vs peerhandle
                    }

                    if (!discoveredDevices.contains(peerHandle)) {
                        discoveredDevices.add(peerHandle);
                        addPeersToChatList();
                    }
                    publishDiscoverySession.sendMessage(peerHandle, MESSAGE, myMac);
                    /*if (!listOfPeersPortIsSentTo.contains(peerHandle)) {
                        if (appServer != null) {
                            sendPort(peerHandle);
                        }
                    }*/
                    String messageIn = new String(message);
               /* if (message.length == 2) {
                    portToUse = byteToPortInt(message);
                    Log.d(LOG, "subscribe, will use port number " + portToUse);
                } else*/
                    if (messageIn.contains("goToChat")) {
                        Toast.makeText(context, "A Peer Client wants to talk to you", Toast.LENGTH_SHORT).show();
                        connectionInitiatedMac.add(hashMapPeerHandleKeyAndMac.get(peerHandle));
                    } else if (message.length == 6) {
                       // setOtherMacAddress(message);
                        String macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", message[0], message[1], message[2], message[3], message[4], message[5]);
                        if (!hashMapPeerHandleKeyAndMac.containsKey(peerHandle)) {
                            hashMapPeerHandleKeyAndMac.put(peerHandle, macAddress);
                            if (!listConnectionInitiatedMacs.contains(macAddress)) {

                                long peerMacDeci = getMacInDecimal(message);

                                if (myMacDeci >= peerMacDeci) {
                                    peerHandlesToUse.put(peerHandle, true);  //this session with peer user is publisher
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


                    } else if (message.length == 16) {
                        //setOtherIPAddress(message);
                        //Log.d(LOG, "setOtherIPAddress " + message);
                    } else if (message.length > 16) {
                        boolean usePublishSession = true;
                        if( peerHandlesToUse.get(peerHandle)!= null){
                            usePublishSession = peerHandlesToUse.get(peerHandle);
                        }

                        if (messageIn.contains("signedString")) {
                            receivedSignedString = messageIn.replace("signedString", "");
                            Log.d(LOG, "Signed string : " + receivedSignedString);
                        } else if (messageIn.contains("randomString")) {
                            receivedString = messageIn.replace("randomString", "");
                            Log.d(LOG, "random String: " + receivedString);

                        } else if (messageIn.contains("AuthKey")) {
                            authenticatorKey = messageIn.replace("AuthKey", "");
                            Log.d(LOG, "authenticator key " + authenticatorKey);
                        } else if (messageIn.contains("SelfSigKey")) {
                            String sigKey = messageIn.replace("SelfSigKey", "");
                            peerSignedKeyAndAuthKeyReceived.put(authenticatorKey,sigKey);
                            //(LOG, "self singed key"+ sigKey+ "and auth key received"+ authenticatorKey);

                        } else if (messageIn.contains("pubKey")) {
                            requestPeerAuthStartTime = currentTimeMillis();
                            receivedPubKey = messageIn.replace("pubKey", "");
                            dontAuhtenticate.put(receivedPubKey, false);
                            startPeerAuthServer(receivedPubKey, peerHandle, usePublishSession);
                        } else if (messageIn.contains("reqAuth")) {    //request authentication
                            //(LOG, "Req auth received");
                            receivedPubKeyToBeSigned = messageIn.replace("reqAuth", "");
                           // authenticatePeer(receivedPubKeyToBeSigned, peerHandle, usePublishSession);
                            setDialogBox(receivedPubKeyToBeSigned, peerHandle, usePublishSession);
                        } else if (messageIn.contains("PACompletedK")) {
                            authenticatorKey = messageIn.replace("PACompletedK","");
                            //(LOG, "PACompletedK");
                        }
                        else if (messageIn.contains("PACompletedS")) {
                            //(LOG, "PACompletedS");
                            IamPeerAuth = true;
                            signedKeyReceived = messageIn.replace("PACompletedS", "");
                            tvRole.setText( "Peer Authenticated: "+ IamPeerAuth);
                            PeerSigner.setSignedKeySelf(signedKeyReceived, authenticatorKey);
                           // requestPeerAuthStopTime = currentTimeMillis();
                           // writeTestingTimeVerifyPeer();
                           // PeerSigner.deleteFile("signedKeySelf.txt");

                           // connectionHandler = new ConnectionHandler(getApplicationContext(), sslContextedObserver.getSslContext(), keyPair, null, isPublisher, peerAuthenticated, peerAuthServer);
                            Toast.makeText(context, "Authenticated! PA server started", Toast.LENGTH_LONG).show();
                        } else if (messageIn.contains("PAServerUP")) {
                            requestPeerAuthConnStopTime = currentTimeMillis();
                            Log.i(LOG,"PAServerUP");
                            writeTestingTimeStartPAServer();
                            //Toast.makeText(context, "Starting PA server", Toast.LENGTH_LONG).show();
                        }
                        else if (messageIn.contains("sigKeyList")) {
                            //(LOG, "sigKeyList received");
                            setBroadcastPeerAuthInfo(messageIn);
                        } else if (messageIn.contains("authUser")) {
                            //(LOG, "trusted authenticator key received");
                            setBroadcastPeerAuthInfo(messageIn);
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
                    ////("LOG-Test-Debugging", "subscribe started");
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

                       // setOtherMacAddress(message);
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
                            peerHandlesToUse.put(peerHandle, false);
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
                    else if (message.length > 16) {

                        Log.d(LOG, "Message IN:" + messageIn);
                        boolean usePublishSession= false;
                        if( peerHandlesToUse.get(peerHandle)!= null){
                            usePublishSession = peerHandlesToUse.get(peerHandle);
                        }
                        if(messageIn.contains("signedString")) { //cverify user has private key
                            receivedSignedString = messageIn.replace("signedString","");
                        }
                        else if(messageIn.contains("randomString")) {
                            receivedString = messageIn.replace("randomString","");
                        }
                        else if (messageIn.contains("AuthKey")) {
                            authenticatorKey = messageIn.replace("AuthKey", "");

                        } else if (messageIn.contains("SelfSigKey")) {
                            String sigKey = messageIn.replace("SelfSigKey", "");
                            peerSignedKeyAndAuthKeyReceived.put(authenticatorKey,sigKey);
                            //(LOG, "self singed key"+ sigKey+ "and auth key received"+ authenticatorKey);

                        }
                        else if(messageIn.contains("pubKey")){
                            receivedPubKey= messageIn.replace("pubKey", "");
                            dontAuhtenticate.put(receivedPubKey, false);
                            startPeerAuthServer(receivedPubKey, peerHandle, usePublishSession);
                        }
                        else if(messageIn.contains("reqAuth")){
                            //(LOG, "Req auth received subscriber" );
                            receivedPubKeyToBeSigned= messageIn.replace("reqAuth", "");
                            setDialogBox(receivedPubKeyToBeSigned, peerHandle, usePublishSession);
                           // authenticatePeer(receivedPubKeyToBeSigned, peerHandle, usePublishSession);
                        } else if(messageIn.contains("PACompletedK")){
                            authenticatorKey = messageIn.replace("PACompletedK","");
                        //    //(LOG, "PACompletedKK subscriber");

                        } else if(messageIn.contains("PACompletedS")){
                        //    //(LOG, "PACompletedS subscriber");
                            IamPeerAuth= true;
                            signedKeyReceived= messageIn.replace("PACompletedS", "");
                            tvRole.setText("Peer Authenticated: "+ IamPeerAuth);
                            PeerSigner.setSignedKeySelf(signedKeyReceived, authenticatorKey);
                            //requestPeerAuthStopTime = currentTimeMillis();
                          //  writeTestingTimeVerifyPeer();
                            Toast.makeText(context, "Authentication Credentials Received!", Toast.LENGTH_LONG).show();
                        //    connectionHandler = new ConnectionHandler(getApplicationContext(), sslContextedObserver.getSslContext(), keyPair, null, isPublisher, peerAuthenticated, peerAuthServer);
                        }
                        else if (messageIn.contains("PAServerUP")) {
                            Log.i(LOG,"testing-----------");
                            requestPeerAuthConnStopTime = currentTimeMillis();
                            Log.i(LOG,"PA server is running");
                            writeTestingTimeStartPAServer();
                            Toast.makeText(context, "Starting PA server", Toast.LENGTH_LONG).show();
                        }
                        else if(messageIn.contains("sigKeyList")){
                         //   //(LOG,"sigKeyList received");
                            setBroadcastPeerAuthInfo(messageIn);
                        }
                        else if (messageIn.contains("authUser")){
                      //      //(LOG,"trusted authenticator key received");
                            setBroadcastPeerAuthInfo(messageIn);
                        }

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
                    .setPskPassphrase("password123")
                    .build();

            //Log.d(LOG, "This devices is subscriber");
        } else {
           // listConnectionWifiRequestedMacs.add(hashMapPeerHandleKeyAndMac.get(peerHandle));
            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(publishDiscoverySession, peerHandle)
                    .setPskPassphrase("password123")
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

                if(certSelfSigned=="true") {
                    requestPeerAuthConn();
                    boolean usePublisherSession= peerHandlesToUse.get(peerHandle);
                  //  //(LOG,"Peer auth conn called in onAvaliable and peerisPublisher is "+ usePublisherSession);
                //    //(LOG,"IAMPEERAUT IS :"+ IamPeerAuth);
                    sendPeerAuthMsg(IamPeerAuth,peerHandle, usePublisherSession);
                }


                /*startApp;
                startAttached;
                discovered;*/

                if(appServer == null){
                    appServer = new AppServer(sslContextedObserver.getSslContext(), network);

                }

                // connectionHandler = new ConnectionHandler(getApplicationContext(), sslContextedObserver.getSslContext(), keyPair, appServer, isPublisher);

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
                    peerHandlesToUse.remove(peerToRemove);
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
        List<PeerHandle> peerHandles = getPeerHandlesFromMacAddress(peerMac);

        if(peerAuthenticated=="false"){
            if(peerHandlesToUse.containsKey(peerHandles.get(0))){
                broadcastSignedKey(peerHandles.get(0));
                broadcastAuthenticatorKey(peerHandles.get(0));
            }
            else{
                broadcastSignedKey(peerHandles.get(1));
                broadcastAuthenticatorKey(peerHandles.get(1));
            }

        }
        //hashMapMacWithPort.put(peerMac, portToServer);
        Intent intentChat = new Intent(this, TestChatActivity.class);  //TODO: change back to chat activity just testing
        intentChat.putExtra("position", position);
        intentChat.putExtra("counter", countervalue);

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

       // EditText editText = findViewById(R.id.eTOtherMac);
       // editText.setText(macAddress);
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

//---------PEER AUTH START ---------------------
    public String certSelfSigned(X509Certificate cert){
        if(cert != null) {
            X500Principal subject = cert.getSubjectX500Principal();
            X500Principal issuer = cert.getIssuerX500Principal();
            if (subject.equals(issuer)) {
                certSelfSigned = "true";
            }

        }

        else{
           // //(LOG,"Certificate is null");
        }
        return certSelfSigned;
    }

    private void setTextView(){
        if(certSelfSigned=="true"){

            tvRole.setText("Peer Authenticated: "+ IamPeerAuth);
        }else{
            tvRole.setText("Authenticated: true" );
        }
    }


    private void setBroadcastPeerAuthInfo(String encodedPeerAuthInfo){
        if(!certSelfSigned.equals("true") ) {
            PeerSigner.setTmpPeerAuthInfo(encodedPeerAuthInfo);
         //   //(LOG, "Set broadcasted peer info" );

        }
    }

    private void broadcastSignedKeySelf(PeerHandle peerHandle){
        HashMap<String,String> signedKeyListSelf =  PeerSigner.getSignedKeySelf();
        boolean usePublishDiscSession = peerHandlesToUse.get(peerHandle);
        if(signedKeyListSelf != null) {
            if(usePublishDiscSession){
                if (publishDiscoverySession != null && peerHandle != null) {
                    Iterator it =signedKeyListSelf.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        byte[] msgAutKey = ("AuthKey" + pair.getKey()).getBytes();
                        byte[] msgSignedKey = ("SelfSigKey" + pair.getValue().toString()).getBytes();
                        publishDiscoverySession.sendMessage(peerHandle, MESSAGE, msgAutKey);
                       // //(LOG, "Sent signed key self in broadcastSignedKeySelf" + msgSignedKey);
                        publishDiscoverySession.sendMessage(peerHandle, MESSAGE, msgSignedKey);
                       // //(LOG, "Sent authenticator key from broadcastSignedKeySelf " + msgAutKey);

                       // //(LOG, "AuthKey is " + msgAutKey);
                        it.remove(); // avoids a ConcurrentModificationException
                    }
                }
            }else{
                if (subscribeDiscoverySession != null && peerHandle != null) {
                    Iterator it =signedKeyListSelf.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        byte[] msgAutKey = ("AuthKey" + pair.getKey()).getBytes();
                        byte[] msgSignedKey = ("SelfSigKey" + pair.getValue().toString()).getBytes();
                        subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE, msgAutKey);
                      //  //(LOG, "Sent signed key self in broadcastSignedKeySelf" + msgSignedKey);
                        subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE, msgSignedKey);
                      //  //(LOG, "Sent authenticator key from broadcastSignedKeySelf " + msgAutKey);
                        it.remove(); // avoids a ConcurrentModificationException

                    }
                }
            }
        }
    }

    private void broadcastSignedKey(PeerHandle peerHandle){
        ArrayList<String> signedKeyList = PeerSigner.getSavedSignedKeysFromFile();
        boolean usePublishDiscSession = peerHandlesToUse.get(peerHandle);
        if(signedKeyList != null) {
            if(usePublishDiscSession){
                if (publishDiscoverySession != null && peerHandle != null) {
                    for (int i = 0; i < signedKeyList.size(); i++) {
                        byte[] msgSignedKey = ("sigKeyList"+ signedKeyList.get(i)).getBytes();
                        publishDiscoverySession.sendMessage(peerHandle, MESSAGE, msgSignedKey);
                      //  //(LOG, "Sent signed key" + msgSignedKey);
                    }
            }else{
                    if (subscribeDiscoverySession != null && peerHandle != null) {
                        for (int i = 0; i < signedKeyList.size(); i++) {
                            byte[] msgSignedKey = ("sigKeyList" + signedKeyList.get(i)).getBytes();
                            subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE, msgSignedKey);
                          //  //(LOG, "Sent signed key" + msgSignedKey);

                        }
                    }
                }
            }
        }
    }

    private void broadcastAuthenticatorKey(PeerHandle peerHandle) {
        boolean usePublishDiscSession = peerHandlesToUse.get(peerHandle);
        ArrayList<PublicKey> authenticatorList =  VerifyUser.getValidatedAuthenticator();
        if(authenticatorList != null) {
            if(usePublishDiscSession){
                if (publishDiscoverySession != null && peerHandle != null) {
                    for (int i = 0; i < authenticatorList.size(); i++) {
                        String pubKeyEncoded = Base64.getEncoder().encodeToString(authenticatorList.get(i).getEncoded());
                        byte[] msgAuthenticator = ("authUser" + pubKeyEncoded).getBytes();
                        publishDiscoverySession.sendMessage(peerHandle, MESSAGE, msgAuthenticator);
                       // //(LOG, "Sent Authenticator list item" + authenticatorList.get(i));
                    }
                }
            }else{
                 if (subscribeDiscoverySession != null && peerHandle != null) {
                    for (int i = 0; i < authenticatorList.size(); i++) {
                        String pubKeyEncoded = Base64.getEncoder().encodeToString(authenticatorList.get(i).getEncoded());
                        byte[] msgAuthenticator = ("authUser" + pubKeyEncoded).getBytes();
                        subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE, msgAuthenticator);
                     //   //(LOG, "Sent Authenticator list item" + authenticatorList.get(i));
                    }
                }

            }


        }
    }


    private void sendPeerAuthMsg(boolean IamAuth, PeerHandle peerHandle, boolean usePublishDiscSession){
       // //(LOG,"This device is using pulishdiscovery session---------"+ usePublishDiscSession);
        if(certSelfSigned.equals("true")) {
            byte[] msgSignedtosend = ("signedString" + signedStringToSend).getBytes();
            byte[] msgRandomStringtoSend = ("randomString" + randomStringToSend).getBytes();

            String publicKey;
            if (!IamAuth) {
                publicKey = "reqAuth" + encodedPubKeyToSend;
              //  //(LOG,"requesting authentication");
             //   Toast.makeText(this, "Requesting PA", Toast.LENGTH_LONG).show();

            } else {
                //(LOG,"sending peer auth conn request");  //TODO: change. just testing
                broadcastSignedKeySelf(peerHandle);
                publicKey = "pubKey" + encodedPubKeyToSend;
             //   Toast.makeText(this, "Requesting Pa Conn", Toast.LENGTH_LONG).show();
            }
            byte[] pubKeyToSend = publicKey.getBytes();
            if(usePublishDiscSession){
                if (publishDiscoverySession != null && peerHandle != null) {
                    publishDiscoverySession.sendMessage(peerHandle, SIGNED_STRING, msgSignedtosend);
                 //   //(LOG,"msgSingedString to send"+ msgRandomStringtoSend);
                    publishDiscoverySession.sendMessage(peerHandle, MESSAGE, msgRandomStringtoSend);
                 //   //(LOG,"random string to send"+ msgRandomStringtoSend);
                    publishDiscoverySession.sendMessage(peerHandle, PUBLIC_KEY, pubKeyToSend);
                 //   //(LOG,"publickey to send"+pubKeyToSend);
                }

            }else{
                 if(subscribeDiscoverySession != null && peerHandle != null) {
                    subscribeDiscoverySession.sendMessage(peerHandle, SIGNED_STRING, msgSignedtosend);
                  //   //(LOG,"msgSingedString to send subscriber"+ msgRandomStringtoSend);
                    subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE, msgRandomStringtoSend);
                 //    //(LOG,"random string to send subscriber"+ msgRandomStringtoSend);
                    subscribeDiscoverySession.sendMessage(peerHandle, PUBLIC_KEY, pubKeyToSend);
                //     //(LOG,"publickey to send subscriber"+pubKeyToSend);
                }
            }

        }
        else {
            Toast.makeText(this, "Already authenticated", Toast.LENGTH_LONG).show();
        }
    }


    private void requestPeerAuthConn(){
        requestPeerAuthStartTime = currentTimeMillis();
        encodedPubKeyToSend = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        randomStringToSend = Decoder.generateRandomString(7);
        signedStringToSend = PeerSigner.signString(randomStringToSend, keyPair);
    }

    private void setDialogBox(String keyReceived, PeerHandle peerHandle, boolean usePublishDiscSession){
        String macAddress= "";
        if(hashMapPeerHandleKeyAndMac.containsKey(peerHandle)){
            macAddress = hashMapPeerHandleKeyAndMac.get(peerHandle);
        }
     //   //(LOG,"setDialogboc");


        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        authenticatePeer(keyReceived, peerHandle, usePublishDiscSession);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        ////(LOG,"Dont want to peer authenticate user");
                        startPeerAuthServer(keyReceived, peerHandle, usePublishDiscSession);
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Authenticate user:"+macAddress+"?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    private void authenticatePeer(String encodedKey, PeerHandle peerHandle, boolean usePublishDiscSession){
        PublicKey peerPubKey = Decoder.getPubKeyGenerated(encodedKey);
        if(VerifyCredentials.verifyString(receivedString,receivedSignedString,peerPubKey))  {
            String signedKeyByPeer= PeerSigner.signPeerKey(peerPubKey,IdentityHandler.getKeyPair());
            PeerSigner.saveSignedKeyToFile(signedKeyByPeer);
            VerifyUser.setAuthenticatedUser(encodedKey);
            peerAuthenticated="true";
            Toast.makeText(this, "User authenticated", Toast.LENGTH_LONG).show();
            encodedPubKeyToSend = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            byte[] signedKey= ("PACompletedS"+signedKeyByPeer).getBytes();
            byte[] authenticatorKey= ("PACompletedK"+ encodedPubKeyToSend).getBytes();
            if(usePublishDiscSession){
                if (publishDiscoverySession != null && peerHandle != null) {
                    publishDiscoverySession.sendMessage(peerHandle, MESSAGE, authenticatorKey);
                    publishDiscoverySession.sendMessage(peerHandle, MESSAGE, signedKey);
                 //   //(LOG,"pub session sent message"+ signedKey +" auth key" + authenticatorKey);
                }
            } else {
                    if(subscribeDiscoverySession != null && peerHandle != null) {
                        subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE,authenticatorKey);
                        subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE,signedKey);
                      //  //(LOG,"Subscribe session sent message"+ signedKey +" auth key" + authenticatorKey);
                }

            }
            Log.d(LOG, "Starting PA server");
            peerAuthServer = new PeerAuthServer(sslContextedObserver.getSslContext(),peerPubKey);  //TODO : testing, move up?



            // PeerSigner.deleteFile("signedKeys.txt");
           // PeerSigner.deleteFile("AuthenticatedUsers.txt");

        }
        else{
            ////(LOG,"Credentials not correct");
        }

    }


    private void startPeerAuthServer(String key, PeerHandle peerHandle,  boolean usePublishDiscSession) {
        PublicKey clientPubKey = Decoder.getPubKeyGenerated(key);
        boolean userIsAuthenticated=false;

        if(peerSignedKeyAndAuthKeyReceived != null) {

            Iterator it = peerSignedKeyAndAuthKeyReceived.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                it.remove(); // avoids a ConcurrentModificationException
                userIsAuthenticated = InitPeerAuthConn.checkPeerAuthCredentials(receivedString, receivedSignedString, clientPubKey, key, pair.getKey().toString(), pair.getValue().toString());

            }

           // //(LOG,"Credentials correct is" +userIsAuthenticated);

            if (userIsAuthenticated) {
                peerAuthenticated = "true";
                byte[] PAServerUP= ("PAServerUPppppppppp").getBytes();
               // Log.i(LOG, "peer auth server started. Sending mgs to ");
              //  Toast.makeText(this, "Starting peerAuthConn", Toast.LENGTH_SHORT).show();
                peerAuthServer = new PeerAuthServer(sslContextedObserver.getSslContext(), clientPubKey);
               // requestPeerAuthConnStopTime = currentTimeMillis();
                if(usePublishDiscSession){
                    if (publishDiscoverySession != null && peerHandle != null) {
                        Log.i(LOG, "Peer auth server started. Sending reply to "+ peerHandle);

                        publishDiscoverySession.sendMessage(peerHandle, MESSAGE,PAServerUP);

                    }
                } else {
                    if(subscribeDiscoverySession != null && peerHandle != null) {
                        Log.i(LOG, "Peer auth server started. Sending reply to "+ peerHandle);
                        subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE, PAServerUP);

                    }
                }
              //  serverWriteTestingTimeStartPAServer();

             //   PeerSigner.deleteFile("AuthenticatedUsers.txt");

              //   connectionHandler = new ConnectionHandler(getApplicationContext(), sslContextedObserver.getSslContext(), keyPair, null, isPublisher, peerAuthenticated, peerAuthServer);

          //  } else {
             //   //(LOG,"User not authenticated.");
             //   if (dontAuhtenticate != null && dontAuhtenticate.get(key) != null && !dontAuhtenticate.get(key)) {  //user not yet been asked if he/she wants to authenticate user
             //       dontAuhtenticate.remove(key);
             //       setDialogBox(key, peerHandle);


            //    }
            }

        }
        else {
          //  //(LOG,"Conn not up yet. IP is"+peerIpv6+" and peersignedkeyandkey is" +peerSignedKeyAndAuthKeyReceived);
        }
    }


    private void writeTestingTimeVerifyPeer(){
        //Log.d("TESTING-LOG-TIME-TLS-HANDSHAKE-COMPLETED-SERVER",  String.valueOf(handshakeCompletedServer));
        BufferedWriter writer = null;
        long requestPeerAuthDiffTime = requestPeerAuthStopTime-requestPeerAuthStartTime;
       // long totalTime = startPeerAuthServerAfterVerifyTime- requestPeerAuthStartTime;
        int count=0;
        try {
            String startTime = String.valueOf(requestPeerAuthStartTime);
            String stopTime = String.valueOf(requestPeerAuthStopTime);
            String diffTime= String.valueOf(requestPeerAuthDiffTime);
           // String startServerTime= String.valueOf(startPeerAuthServerAfterVerifyTime);
           // String startServerTimeTotal = String.valueOf(totalTime);
            writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/verifyPeerLoggingTime.txt", true));
            writer.append("Counter:" + count);
            writer.append("\n");
            writer.append(startTime + "----"+ stopTime+ "diff time"+ diffTime );
            writer.append("\n");
            writer.close();
            Log.i(LOG, "write to file"+ count);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeTestingTimeStartPAServer(){
        //Log.d("TESTING-LOG-TIME-TLS-HANDSHAKE-COMPLETED-SERVER",  String.valueOf(handshakeCompletedServer));
        BufferedWriter writer = null;
        long requestPeerAuthConnDiffTime = requestPeerAuthConnStopTime-requestPeerAuthStartTime;
        // long totalTime = startPeerAuthServerAfterVerifyTime- requestPeerAuthStartTime;

        try {
            String startTime = String.valueOf(requestPeerAuthStartTime);
            String stopTime = String.valueOf(requestPeerAuthConnStopTime);
            String diffTime= String.valueOf(requestPeerAuthConnDiffTime);
            // String startServerTime= String.valueOf(startPeerAuthServerAfterVerifyTime);
            // String startServerTimeTotal = String.valueOf(totalTime);
            writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/startPAServerLoggingTime.txt", true));
            writer.append(startTime + "----"+ stopTime+ "diffTime"+ diffTime);
            writer.append("\n");
            writer.close();
            Log.i(LOG, "write to file");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    private void serverWriteTestingTimeStartPAServer(){
//        //Log.d("TESTING-LOG-TIME-TLS-HANDSHAKE-COMPLETED-SERVER",  String.valueOf(handshakeCompletedServer));
//        BufferedWriter writer = null;
//        long requestPeerAuthConnDiffTime = requestPeerAuthConnStopTime-requestPeerAuthStartTime;
//        // long totalTime = startPeerAuthServerAfterVerifyTime- requestPeerAuthStartTime;
//
//        try {
//            String startTime = String.valueOf(requestPeerAuthStartTime);
//            String stopTime = String.valueOf(requestPeerAuthConnStopTime);
//            String diffTime= String.valueOf(requestPeerAuthConnDiffTime);
//            // String startServerTime= String.valueOf(startPeerAuthServerAfterVerifyTime);
//            // String startServerTimeTotal = String.valueOf(totalTime);
//            writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/ServerStartPAServerLoggingTime.txt", true));
//            writer.append(startTime + "----"+ stopTime+ "diffTime"+ diffTime);
//            writer.append("\n");
//            writer.close();
//            Log.i(LOG, "write to file");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }




}
