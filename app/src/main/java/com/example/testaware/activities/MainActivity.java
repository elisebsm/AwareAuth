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
import com.example.testaware.IdentityHandler;
import com.example.testaware.listeners.BooleanChangedListener;
import com.example.testaware.listeners.BooleanObserver;
import com.example.testaware.listeners.SSLContextedObserver;
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


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Inet6Address;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import java.util.Map;

import lombok.Getter;

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


    private WifiAwareNetworkInfo peerAwareInfo;

    private static Inet6Address peerIpv6 ;
    private Inet6Address myIP;



    private static final int MY_PERMISSION_COARSE_LOCATION_REQUEST_CODE = 88;
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
    private String receivedPubKey;
    private String receivedString;
    private String signedKeyReceived;
    private HashMap<String, String> peerSignedKeyAndAuthKeyReceived = new HashMap<>();
    private HashMap<String, Boolean> dontAuhtenticate = new HashMap<>();

    private HashMap<PeerHandle, Boolean> peerHandlesToUse = new HashMap<>();
    @Getter
    private String certSelfSigned="false";

    @Getter
    private String role = "publisher";

    private String LOG = "Log-Main";

    @Getter
    private String peerAuthenticated ="false";

    @Getter
    private PeerAuthServer peerAuthServer;

    @Getter
    private SSLContextedObserver sslContextedObserver;

    @Getter
    private AppServer appServer;

    private int localPortServer;


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


    PeerHandle peer;
    byte [] msg;

    private BooleanObserver booleanObserver;

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


        if (!canAccessLocationFine()) {
            requestPermissions(LOCATION_PERMS, LOCATION_REQUEST_FINE);
        }

        if (!canAccessLocationCoarse()) {
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

        sslContextedObserver = new SSLContextedObserver();
        sslContextedObserver.setListener(sslContext -> {
            connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            attachToSession();
        });

        sslContextedObserver.setSslContext(IdentityHandler.getSSLContext());

        certSelfSigned = certSelfSigned(IdentityHandler.getCertificate());

        setTextView();

        addPeersToChatList();
        ChatActivity.updateActivityMain(this);
        AppServer.updateActivity(this);
        PeerAuthServer.updateActivity(this);

        booleanObserver  = new BooleanObserver();
    }


    private void setUpNewConnection(){
        closeSession();
        attachToSession();
        addPeersToChatList();
        ChatActivity.updateActivityMain(this);
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

            ChatsListAdapter chatListAdapter = new ChatsListAdapter(this, userList);
            ListView listViewChats = findViewById(R.id.listViewChats);
            listViewChats.setAdapter(chatListAdapter);

            listViewChats.setOnItemClickListener((parent, view, position, id) -> {
                String peerIpv6 = chatListAdapter.getChats().get(position).getPeerIpv6();
                MainActivity.this.openChat(position, peerIpv6);
                Log.d(LOG, "peer ipv6 from chatview"+ peerIpv6);
            });
        }
    }


    public void startPublishAndSubscribe(){
        subscribe();
        publish();
    }


    private void attachToSession(){

        wifiAwareManager.attach(new AttachCallback() {
            @Override
            public void onAttached(WifiAwareSession session) {
                super.onAttached(session);
                wifiAwareSession = session;
            }
            @Override
            public void onAttachFailed() {
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
                    String messageIn = new String(message);

                    if (messageIn.contains("goToChat")) {
                        Toast.makeText(context, "A Peer Client wants to talk to you", Toast.LENGTH_SHORT).show();
                        connectionInitiatedMac.add(hashMapPeerHandleKeyAndMac.get(peerHandle));
                    } else if (message.length == 6) {

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
                            }
                        }


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
                        } else if (messageIn.contains("pubKey")) {
                            receivedPubKey = messageIn.replace("pubKey", "");
                            dontAuhtenticate.put(receivedPubKey, false);
                            startPeerAuthServer(receivedPubKey, peerHandle, usePublishSession);
                        } else if (messageIn.contains("reqAuth")) {    //request authentication
                            receivedPubKeyToBeSigned = messageIn.replace("reqAuth", "");
                            setDialogBox(receivedPubKeyToBeSigned, peerHandle, usePublishSession);
                        } else if (messageIn.contains("PACompletedK")) {
                            authenticatorKey = messageIn.replace("PACompletedK","");
                        }
                        else if (messageIn.contains("PACompletedS")) {
                            IamPeerAuth = true;
                            signedKeyReceived = messageIn.replace("PACompletedS", "");
                            tvRole.setText( "Peer Authenticated: "+ IamPeerAuth);
                            PeerSigner.setSignedKeySelf(signedKeyReceived, authenticatorKey);
                            Toast.makeText(context, "Authenticated! PA server started", Toast.LENGTH_LONG).show();
                        } else if (messageIn.contains("sigKeyList")) {
                            setBroadcastPeerAuthInfo(messageIn);
                        } else if (messageIn.contains("authUser")) {
                            setBroadcastPeerAuthInfo(messageIn);
                        }

                    }
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
                }

                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void onServiceDiscovered(PeerHandle peerHandle_, byte[] serviceSpecificInfo, List<byte[]> matchFilter) {
                    super.onServiceDiscovered(peerHandle_, serviceSpecificInfo, matchFilter);
                    serviceSpecificInfoText = "noConnection".getBytes();

                    if(!publishDiscoveredPeers.contains(peerHandle_)){
                        subscribeDiscoveredPeers.add(peerHandle_);
                    }

                    if(!discoveredDevices.contains(peerHandle_)){
                        discoveredDevices.add(peerHandle_);
                        addPeersToChatList();
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
                    if(message.length == 2) {
                        portToUse = byteToPortInt(message);
                    } else if (messageIn.contains("goToChat")) {
                        Toast.makeText(context, "A Peer wants to talk to you", Toast.LENGTH_SHORT).show();
                        connectionInitiatedMac.add(hashMapPeerHandleKeyAndMac.get(peerHandle));
                    }else if (message.length == 6){
                        String macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", message[0], message[1], message[2], message[3], message[4], message[5]);

                        if(!hashMapPeerHandleKeyAndMac.containsKey(peerHandle)){
                            hashMapPeerHandleKeyAndMac.put(peerHandle, macAddress);

                            peerAndMacByte.put(peerHandle, message);
                        }


                    } else if (messageIn.contains("startConnection") ) {
                        long peerMacDeci = getMacInDecimal(peerAndMacByte.get(peerHandle));

                        if(myMacDeci < peerMacDeci){
                            peerHandlesToUse.put(peerHandle, false);
                            requestWiFiConnection(peerHandle, "subscriber");
                        }

                    } else if(messageIn.contains("ServerPort:")) {
                        Integer portToServer = Integer.parseInt(messageIn.split(":")[1]);
                        String mac = hashMapPeerHandleKeyAndMac.get(peerHandle);
                        if(!hashMapMacWithPort.containsKey(mac)){
                            hashMapMacWithPort.put(mac, portToServer);
                        }

                    }
                    else if (message.length > 16) {

                        Log.d(LOG, "Message IN:" + messageIn);
                        boolean usePublishSession= false;
                        if( peerHandlesToUse.get(peerHandle)!= null){
                            usePublishSession = peerHandlesToUse.get(peerHandle);
                        }
                        if(messageIn.contains("signedString")) { //verify user has private key
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
                        }
                        else if(messageIn.contains("pubKey")){
                            receivedPubKey= messageIn.replace("pubKey", "");
                            dontAuhtenticate.put(receivedPubKey, false);
                            startPeerAuthServer(receivedPubKey, peerHandle, usePublishSession);
                        }
                        else if(messageIn.contains("reqAuth")){
                            receivedPubKeyToBeSigned= messageIn.replace("reqAuth", "");
                            setDialogBox(receivedPubKeyToBeSigned, peerHandle, usePublishSession);
                        } else if(messageIn.contains("PACompletedK")){
                            authenticatorKey = messageIn.replace("PACompletedK","");

                        } else if(messageIn.contains("PACompletedS")){
                            IamPeerAuth= true;
                            signedKeyReceived= messageIn.replace("PACompletedS", "");
                            tvRole.setText("Peer Authenticated: "+ IamPeerAuth);
                            PeerSigner.setSignedKeySelf(signedKeyReceived, authenticatorKey);
                            Toast.makeText(context, "Authentication Credentials Received!", Toast.LENGTH_LONG).show();
                        }
                        else if (messageIn.contains("PAServerUP")) {
                            Toast.makeText(context, "Starting PA server", Toast.LENGTH_LONG).show();
                        }
                        else if(messageIn.contains("sigKeyList")){
                            setBroadcastPeerAuthInfo(messageIn);
                        }
                        else if (messageIn.contains("authUser")){
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

        if(role.equals("subscriber")){
            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(subscribeDiscoverySession, peerHandle)
                    .setPskPassphrase("password123")
                    .build();

        } else {
            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(publishDiscoverySession, peerHandle)
                    .setPskPassphrase("password123")
                    .setTransportProtocol(6)
                    .build();
        }

        if (networkSpecifier == null) {
            return;
        }

        NetworkRequest myNetworkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
                .setNetworkSpecifier(networkSpecifier)
                .build();

        connectivityManager.requestNetwork(myNetworkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);

                if(!connectedDevices.contains(peerHandle)){
                    connectedDevices.add(peerHandle);
                    discoveredDevices.remove(peerHandle);
                }

                new Handler(Looper.getMainLooper()).post(()-> {
                    addPeersToChatList();
                });
                hashMapOfNetworks.put(hashMapPeerHandleKeyAndMac.get(peerHandle), network);
                Toast.makeText(context, "On Available!", Toast.LENGTH_SHORT).show();

                if(certSelfSigned.equals("true")) {
                    requestPeerAuthConn();
                    boolean usePublisherSession= peerHandlesToUse.get(peerHandle);
                    sendPeerAuthMsg(IamPeerAuth,peerHandle, usePublisherSession);
                }

                if(appServer == null){
                    appServer = new AppServer(sslContextedObserver.getSslContext());
                }
            }


            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Toast.makeText(context, "onUnavailable", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);
            }

            @Override
            public void onCapabilitiesChanged(Network network_, NetworkCapabilities networkCapabilities) {
                peerAwareInfo = (WifiAwareNetworkInfo) networkCapabilities.getTransportInfo();
                setPeerIpv6(peerAwareInfo.getPeerIpv6Addr());
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                String mac = getMacAddressFromNetwork(network);
                listConnectionInitiatedMacs.remove(mac);
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
                    new Handler(Looper.getMainLooper()).post(()-> {
                        setUpNewConnection();
                    });
                }
            }
        });
    }


    private void setPeerIpv6(Inet6Address ipv6){
        peerIpv6 = ipv6;
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

        if(peerAuthenticated.equals("false")){
            if(peerHandlesToUse.containsKey(peerHandles.get(0))){
                broadcastSignedKey(peerHandles.get(0));
                broadcastAuthenticatorKey(peerHandles.get(0));
            }
            else{
                broadcastSignedKey(peerHandles.get(1));
                broadcastAuthenticatorKey(peerHandles.get(1));
            }
        }
        Intent intentChat = new Intent(this, ChatActivity.class);
        intentChat.putExtra("position", position);

        int port = getPortFromMac(peerMac);

        peerHandlesStartChat = peerHandles;
        if (!connectionInitiatedMac.contains(peerMac)){
            byte [] message = "goToChat".getBytes();

            publishDiscoverySession.sendMessage(peerHandlesStartChat.get(0), MESSAGEGOTOCHAT, message);
        }

        if(connectionInitiatedMac.contains(peerMac)){
            intentChat.putExtra("Role", "Server");
            intentChat.putExtra("port", localPortServer);
            startActivity(intentChat);
        }

        booleanObserver.setListener(messageSentStatus -> {
             if (sendMessageOpenChatFailed) {
                Toast.makeText(context, "Message to open chat did not send. You are server", Toast.LENGTH_LONG).show();

                intentChat.putExtra("Role", "Server");
                intentChat.putExtra("port", localPortServer);
                startActivity(intentChat);

            } else if(!sendMessageOpenChatFailed){
                 intentChat.putExtra("Role", "Client");
                 intentChat.putExtra("port", port);
                 startActivity(intentChat);
             }
        });
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


    private void setMacAddress(byte[] mac) {
        myMac = mac;
        String macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
        EditText editText = findViewById(R.id.eTYourMac);
        editText.setText(macAddress);
    }


    public int byteToPortInt(byte[] bytes){
        return ((bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF));
    }


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
        return certSelfSigned;
    }

    private void setTextView(){
        if(certSelfSigned.equals("true")){
            tvRole.setText("Peer Authenticated: "+ IamPeerAuth);
        }else{
            tvRole.setText("Authenticated: true");
        }
    }


    private void setBroadcastPeerAuthInfo(String encodedPeerAuthInfo){
        if(!certSelfSigned.equals("true") ) {
            PeerSigner.setTmpPeerAuthInfo(encodedPeerAuthInfo);
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
                        publishDiscoverySession.sendMessage(peerHandle, MESSAGE, msgSignedKey);
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
                        subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE, msgSignedKey);
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
                    }
            }else{
                    if (subscribeDiscoverySession != null && peerHandle != null) {
                        for (int i = 0; i < signedKeyList.size(); i++) {
                            byte[] msgSignedKey = ("sigKeyList" + signedKeyList.get(i)).getBytes();
                            subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE, msgSignedKey);
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
                    }
                }
            }else{
                 if (subscribeDiscoverySession != null && peerHandle != null) {
                    for (int i = 0; i < authenticatorList.size(); i++) {
                        String pubKeyEncoded = Base64.getEncoder().encodeToString(authenticatorList.get(i).getEncoded());
                        byte[] msgAuthenticator = ("authUser" + pubKeyEncoded).getBytes();
                        subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE, msgAuthenticator);
                    }
                }
            }
        }
    }


    private void sendPeerAuthMsg(boolean IamAuth, PeerHandle peerHandle, boolean usePublishDiscSession){
        if(certSelfSigned.equals("true")) {
            byte[] msgSignedtosend = ("signedString" + signedStringToSend).getBytes();
            byte[] msgRandomStringtoSend = ("randomString" + randomStringToSend).getBytes();

            String publicKey;
            if (!IamAuth) {
                publicKey = "reqAuth" + encodedPubKeyToSend;

            } else {
                broadcastSignedKeySelf(peerHandle);
                publicKey = "pubKey" + encodedPubKeyToSend;
            }
            byte[] pubKeyToSend = publicKey.getBytes();
            if(usePublishDiscSession){
                if (publishDiscoverySession != null && peerHandle != null) {
                    publishDiscoverySession.sendMessage(peerHandle, SIGNED_STRING, msgSignedtosend);
                    publishDiscoverySession.sendMessage(peerHandle, MESSAGE, msgRandomStringtoSend);
                    publishDiscoverySession.sendMessage(peerHandle, PUBLIC_KEY, pubKeyToSend);
                }

            }else{
                 if(subscribeDiscoverySession != null && peerHandle != null) {
                    subscribeDiscoverySession.sendMessage(peerHandle, SIGNED_STRING, msgSignedtosend);
                    subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE, msgRandomStringtoSend);
                    subscribeDiscoverySession.sendMessage(peerHandle, PUBLIC_KEY, pubKeyToSend);
                }
            }

        }
        else {
            Toast.makeText(this, "Already authenticated", Toast.LENGTH_LONG).show();
        }
    }


    private void requestPeerAuthConn(){
        encodedPubKeyToSend = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        randomStringToSend = Decoder.generateRandomString(7);
        signedStringToSend = PeerSigner.signString(randomStringToSend, keyPair);
    }

    private void setDialogBox(String keyReceived, PeerHandle peerHandle, boolean usePublishDiscSession){
        String macAddress= "";
        if(hashMapPeerHandleKeyAndMac.containsKey(peerHandle)){
            macAddress = hashMapPeerHandleKeyAndMac.get(peerHandle);
        }


        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    authenticatePeer(keyReceived, peerHandle, usePublishDiscSession);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    startPeerAuthServer(keyReceived, peerHandle, usePublishDiscSession);
                    break;
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
                }
            } else {
                    if(subscribeDiscoverySession != null && peerHandle != null) {
                        subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE,authenticatorKey);
                        subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE,signedKey);
                }
            }
            peerAuthServer = new PeerAuthServer(sslContextedObserver.getSslContext(),peerPubKey);  //TODO : testing, move up?
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

            if (userIsAuthenticated) {
                peerAuthenticated = "true";
                byte[] PAServerUP= ("PAServerUPppppppppp").getBytes();
                peerAuthServer = new PeerAuthServer(sslContextedObserver.getSslContext(), clientPubKey);
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
            }
        }
    }
}
