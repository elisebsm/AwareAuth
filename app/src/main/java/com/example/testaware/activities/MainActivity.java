package com.example.testaware.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.aware.AttachCallback;
import android.net.wifi.aware.Characteristics;
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
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testaware.AppServer;
import com.example.testaware.ClientHandler;
import com.example.testaware.ConnectionHandler;
import com.example.testaware.IdentityHandler;
import com.example.testaware.listeners.OnSSLContextChangedListener;
import com.example.testaware.listeners.SSLContextedObserver;
import com.example.testaware.models.AbstractPacket;
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
import java.io.Serializable;
import java.net.Inet6Address;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.net.ssl.SSLContext;
import lombok.Getter;

public class MainActivity extends AppCompatActivity  {


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

    private byte[] portOnSystem;
    private int  portToUse;

    private byte[]                    otherMac;
    private byte[] otherIP;

    private PeerHandle peerHandle;
    private byte[] myMac;


    private final int MAC_ADDRESS_MESSAGE = 11;
    private ConnectivityManager connectivityManager;
    private NetworkSpecifier networkSpecifier;


    private final int                 IP_ADDRESS_MESSAGE             = 33;

    private String ipAddr; //other IP

    private final int                 MESSAGE                        = 7;
    private final int                 PUBLIC_KEY                        = 10;
    private final int                 SIGNED_STRING                        = 44;
    private NetworkCapabilities networkCapabilities;
    private Network network;

    private WifiAwareNetworkInfo peerAwareInfo;

    //@Getter
    private static Inet6Address peerIpv6 ;
    private int peerPort;


    private List<PeerHandle> peerHandleList = new ArrayList<>();
    private List<String> otherMacList = new ArrayList<>();
    private String macAddress;

    private static final int MY_PERMISSION_COARSE_LOCATION_REQUEST_CODE = 88;
    private static final int MY_PERMISSION_FINE_LOCATION_CODE = 99;
    private static final int MY_PERMISSION_NETWORK_STATE_CODE = 77;

    private static final int MY_PERMISSION_EXTERNAL_REQUEST_CODE = 99;

    private KeyPair keyPair;
    private String signedStringToSend;
    private String encPubKeyToSend;
    private String randomStringToSend;

    private String receivedSignedString;
    private String receivedPubKey;              //TODO : not variables for all, change for multiple connections
    private String receivedString;





    @Getter
   // private String role = "subscriber";
   // boolean isPublisher = false;
    private String role = "publisher";
    boolean isPublisher = true;

    private String LOG = "LOG-Test-Aware";

    @Getter
    private String peerAuthenticated ="false";

    @Getter
    private PeerAuthServer peerAuthServer;


    @Getter
    private ArrayList<Contact> contacts;

    @Getter
    private ConnectionHandler connectionHandler;

    @Getter
    private SSLContextedObserver sslContextedObserver;


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
        TextView tvRole = findViewById(R.id.tvRole);
        tvRole.setText(role);


        this.keyPair = IdentityHandler.getKeyPair();
        wifiAwareManager = (WifiAwareManager) getSystemService(Context.WIFI_AWARE_SERVICE);
        Characteristics networkList = wifiAwareManager.getCharacteristics();

        context = this;

        sslContextedObserver = new SSLContextedObserver();

        sslContextedObserver.setListener(new OnSSLContextChangedListener(){
            @Override
            public void onSSLContextChanged(SSLContext sslContext){
                connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                //connectionHandler = new ConnectionHandler(getApplicationContext(), sslContext, keyPair);
                //connectionHandler.registerCener(MainActivity.this);
                attachToSession();


            }
        });
        sslContextedObserver.setSslContext(IdentityHandler.getSSLContext(this.context));



        addPeersToChatList();
        TestChatActivity.updateActivityMain(this);
        AppServer.updateActivity(this);
        PeerAuthServer.updateActivity(this);


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

        Button reqPeerAuthConnBtn = findViewById(R.id.btnReqPeerAuthConn);
        reqPeerAuthConnBtn.setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPeerAuthConn();

                byte[] msgSignedtosend = ("signedString"+signedStringToSend).getBytes();
                byte[] msgRandomStringtoSend = ("randomString"+randomStringToSend).getBytes();
                String publicKey = "pubKey"+ encPubKeyToSend;
                byte[] pubKeyToSend =publicKey.getBytes();
                Log.i(LOG,"Bytes length key"+  pubKeyToSend.length);

                if(role == "publisher"){
                    if (publishDiscoverySession != null && peerHandle != null) {
                        publishDiscoverySession.sendMessage(peerHandle, SIGNED_STRING, msgSignedtosend);
                        publishDiscoverySession.sendMessage(peerHandle, MESSAGE, msgRandomStringtoSend);
                        publishDiscoverySession.sendMessage(peerHandle, PUBLIC_KEY, pubKeyToSend);
                        //connectionHandler = new ConnectionHandler(getApplicationContext(), sslContextedObserver.getSslContext(), keyPair, null, isPublisher, peerAuthenticated, peerAuthServer );
                    }
                }
                else{
                    if (subscribeDiscoverySession != null && peerHandle != null) {
                        subscribeDiscoverySession.sendMessage(peerHandle, SIGNED_STRING, msgSignedtosend);
                        subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE, msgRandomStringtoSend);
                        subscribeDiscoverySession.sendMessage(peerHandle, PUBLIC_KEY,pubKeyToSend);
                        connectionHandler = new ConnectionHandler(getApplicationContext(), sslContextedObserver.getSslContext(), keyPair, null, isPublisher, peerAuthenticated, peerAuthServer );
                    }
                }
            }
        });
    }
    //Performed by client
    private void requestPeerAuthConn(){
        encPubKeyToSend = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        randomStringToSend = "hellbff";  //TODO: genreate random for each time
        signedStringToSend = PeerSigner.signString(randomStringToSend, keyPair);

    }

    private void startPeerAuthServer(String key) {
        PublicKey clientPubKey = Decoder.getPubKeyGenerated(key);
        boolean userIsAuthenticated = InitPeerAuthConn.checkPeerAuthCredentials(receivedString, receivedSignedString, clientPubKey, peerIpv6.toString());
        if (userIsAuthenticated) {
            peerAuthenticated="true";
            if (role == "publisher") {
                peerAuthServer = new PeerAuthServer(sslContextedObserver.getSslContext(), Constants.SERVER_PORT, clientPubKey.toString());           //TODO: change to no auth server port ?
            }
            connectionHandler = new ConnectionHandler(getApplicationContext(), sslContextedObserver.getSslContext(), keyPair, null, isPublisher, peerAuthenticated, peerAuthServer);
        }
    }

    /**
     * App Permissions for Coarse Location
     **/
    private void setupPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String[] permissionsWeNeed = new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION };
                requestPermissions(permissionsWeNeed, MY_PERMISSION_COARSE_LOCATION_REQUEST_CODE);
            }
        }
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
                }
            }
        }
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
        });
    }


    public void startPublishAndSubscribe(String role){
        if(role.equals("subscriber")){
            subscribe();
        } else {
            publish();
        }
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
                startPublishAndSubscribe(role);
                //closeSession(); ENDRET  //TODO: close session
            }
        }, null);
    }


    private void publish () {
        PublishConfig config = new PublishConfig.Builder()
                .setServiceName(Constants.SERVICE_NAME)
                .build();

        wifiAwareSession.publish(config, new DiscoverySessionCallback() {
            @Override
            public void onPublishStarted(PublishDiscoverySession session) {
                super.onPublishStarted(session);
                publishDiscoverySession = session;
            }

            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onMessageReceived(PeerHandle peerHandle_, byte[] message) {
                super.onMessageReceived(peerHandle, message);
                publishDiscoverySession.sendMessage(peerHandle_, MESSAGE, myMac);
                String messageIn = new String(message);
                if(message.length == 2) {
                    portToUse = byteToPortInt(message);
                    Log.d(LOG, "subscribe, will use port number "+ portToUse);
                } else if (message.length == 6){
                    setOtherMacAddress(message);
                    Log.d(LOG, "setOtherMacAddress "+ message);

                    requestWiFiConnection(peerHandle_, role);

                    byte[] msgtosend = "startConnection".getBytes();
                    publishDiscoverySession.sendMessage(peerHandle_, MESSAGE, msgtosend);


                    if (!otherMacList.contains(macAddress)){
                        otherMacList.add(macAddress);
                        if (!peerHandleList.contains(peerHandle_)){
                            peerHandleList.add(peerHandle_);
                        }
                        int numOfSubscribers = peerHandleList.size();
                        Log.d(LOG, "numOfSubscribers" + numOfSubscribers);

                        if(otherMacList.size()>1){  //Kun for å logge info
                            for (int i = 0; i <otherMacList.size(); i ++){
                                Log.d(LOG, "otherMacList " + i + " macAddress: " + otherMacList.get(i));
                                //byte [] messageOtherMac =  otherMacList.get(i).getBytes();
                                //publishDiscoverySession.sendMessage(peerHandle, MESSAGE, messageOtherMac);
                            }
                        }

                        if(peerHandleList.size()>1){ //Kun for å logge info
                            for (int i = 0; i <peerHandleList.size(); i ++){
                                Log.d(LOG, "peerHandleList " + i + " peerhandle: " + peerHandleList.get(i));
                                //byte [] messageOtherMac =  peerHandleList.get(i).getBytes();
                                //publishDiscoverySession.sendMessage(peerHandle, MESSAGE, messageOtherMac);
                            }
                        }
                        Log.d(LOG, "numOfSubscribers" + numOfSubscribers);
                        addPeersToChatList();
                    }

                } else if (message.length == 16) {
                    setOtherIPAddress(message);
                    Log.d(LOG, "setOtherIPAddress "+ message);
                } else if (message.length > 16) {

                    if(messageIn.contains("signedString")) {
                        receivedSignedString = messageIn.replace("signedString","");
                        Log.d(LOG, "Signed string : " + receivedSignedString);
                    }
                    else if(messageIn.contains("randomString")) {
                        receivedString = messageIn.replace("randomString","");
                        Log.d(LOG, "random String: " + receivedString);

                    }
                    else if(messageIn.contains("pubKey")){
                        receivedPubKey= messageIn.replace("pubKey", "");
                        startPeerAuthServer(receivedPubKey);
                    }


   /* forsøk på å bestemme hvem som er pub/sub

                    requestWiFiConnection(peerHandle_, role);

                    byte[] msgtosend = "startConnection".getBytes();
                    publishDiscoverySession.sendMessage(peerHandle, MESSAGE, msgtosend);*/

                   /* String[] messageIn = new String(message).split(".");
                    if(messageIn[0].equals("subscriber") && messageIn[1].equals("establishingRole")){
                        isPublisher = false;
                        hasEstablishedPublisherAndSubscriber = true;
                    }*/
                }


                peerHandle = peerHandle_;
            }
        }, null);
    }


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

                if (!peerHandleList.contains(peerHandle_)){
                    peerHandleList.add(peerHandle_);
                }
            }
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onMessageReceived(PeerHandle peerHandle, byte[] message) {
                super.onMessageReceived(peerHandle, message);
                Log.d(LOG, "subscribe, received message");
                String messageIn = new String(message);
                if(message.length == 2) {
                    portToUse = byteToPortInt(message);
                    Log.d(LOG, "subscribe, will use port number "+ portToUse);
                } else if (message.length == 6){
                    setOtherMacAddress(message);
                    Log.d(LOG, "setOtherMacAddress "+ message);
                    if (!otherMacList.contains(macAddress)){
                        otherMacList.add(macAddress);
                        if (!peerHandleList.contains(peerHandle)){
                            peerHandleList.add(peerHandle);
                        }
                        int numOfSubscribers = peerHandleList.size();
                        Log.d(LOG, "numOfSubscribers" + numOfSubscribers);

   //Kun for å logge info
                        if(otherMacList.size()>1){
                            for (int i = 0; i <otherMacList.size(); i ++){
                                Log.d(LOG, "otherMacList " + i + " macAddress: " + otherMacList.get(i));
                                //byte [] messageOtherMac =  otherMacList.get(i).getBytes();
                                //publishDiscoverySession.sendMessage(peerHandle, MESSAGE, messageOtherMac);
                            }
                        }
//Kun for å logge info
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

                } else if (message.length == 16) {
                    setOtherIPAddress(message);
                    Log.d(LOG, "setOtherIPAddress "+ message);
                } else if (message.length > 16) {
                    Log.d(LOG, "Message IN:" + messageIn);

                    if(messageIn.contains("startConnection")) {
                        requestWiFiConnection(peerHandle, "subscriber");
                    }
                    else if(messageIn.contains("signedString")) { //cverify user has private key
                        receivedSignedString = messageIn.replace("signedString","");
                    }
                    else if(messageIn.contains("randomString")) {
                        receivedString = messageIn.replace("randomString","");
                    }
                    if(messageIn.contains("pubKey")){
                        receivedPubKey= messageIn.replace("pubKey", "");
                        startPeerAuthServer(receivedPubKey);
                    }

                } else if (messageIn.contains("startConnection")) {
                    Log.d(LOG, "Message IN:" + messageIn);
                        requestWiFiConnection(peerHandle, "subscriber");
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


@Getter
private AppServer appServer;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestWiFiConnection(PeerHandle peerHandle, String role) {
        Log.d(LOG, "requestWiFiConnection");
        /*if(!hasEstablishedPublisherAndSubscriber){
            Log.d(LOG, "hasestablished not");
            //establishWhoIsPublisherAndSubscriber();
        }*/
        if(role.equals("subscriber")){
            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(subscribeDiscoverySession, peerHandle)
                    .build();
            Log.d(LOG, "This devices is subscriber");
        } else {
            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(publishDiscoverySession, peerHandle)
                    .build();
            Log.d(LOG, "This devices is publisher");
        }

        if (networkSpecifier == null) {
            Log.d(LOG, "No NetworkSpecifier Created ");
            return;
        }
        NetworkRequest myNetworkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
                .setNetworkSpecifier(networkSpecifier)
                .build();

        connectivityManager.requestNetwork(myNetworkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network_) {
                super.onAvailable(network);
                Log.d(LOG, "onAvaliable + Network:" + network_.toString());
                Toast.makeText(context, "On Available!", Toast.LENGTH_LONG).show();
                if(role=="publisher"){
                //    appServer = new AppServer(sslContextedObserver.getSslContext(), Constants.SERVER_PORT);
                }
              
             //   connectionHandler = new ConnectionHandler(getApplicationContext(), sslContextedObserver.getSslContext(), keyPair, appServer, isPublisher, peerAuthenticated, null );

            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.d(LOG, "entering onUnavailable ");
                Toast.makeText(context, "onUnavailable", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCapabilitiesChanged(Network network_, NetworkCapabilities networkCapabilities_) {
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
                Log.d(LOG, "onLost");
            }
        });
    }

    private void setPeerIpv6(Inet6Address ipv6){
        this.peerIpv6 = ipv6;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void openChat(int position) {
        Intent intentChat = new Intent(this, TestChatActivity.class);  //TODO: change back to chat activity just testing
        intentChat.putExtra("position", position);
        Contact contact = new Contact(IdentityHandler.getCertificate());
        intentChat.putExtra("contact", (Serializable) contact);
        startActivity(intentChat);
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
