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
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.aware.AttachCallback;
import android.net.wifi.aware.DiscoverySession;
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
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testaware.AppServer;
import com.example.testaware.ConnectionHandler;
import com.example.testaware.IdentityHandler;
import com.example.testaware.TestChatActivity;
import com.example.testaware.listeners.ConnectionListener;
import com.example.testaware.listeners.SSLContextedObserver;
import com.example.testaware.models.AbstractPacket;
import com.example.testaware.models.Contact;
import com.example.testaware.listitems.ChatListItem;
import com.example.testaware.Constants;
import com.example.testaware.R;
import com.example.testaware.adapters.ChatsListAdapter;
import com.example.testaware.models.Message;


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

import lombok.Getter;

import static java.lang.System.currentTimeMillis;


public class MainActivity extends AppCompatActivity implements ConnectionListener {


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

    private PeerHandle peerHandle;
    private byte[] myMac;


    private final int MAC_ADDRESS_MESSAGE = 11;
    private ConnectivityManager connectivityManager;
    private NetworkSpecifier networkSpecifier;


    private final int                 MESSAGE                        = 7;
    private NetworkCapabilities networkCapabilities;
   //0 private Network network;

    private WifiAwareNetworkInfo peerAwareInfo;

    private static Inet6Address peerIpv6 ;
    private Inet6Address myIP;

    private String macAddress;

    private HashMap<String, PeerHandle> hashMapPeerHandleAndMac = new HashMap<>();
    private HashMap<Integer, PeerHandle> hashMapPort = new HashMap<>();

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

    long start = 0;
    long discovered = 0;
    long available = 0;
    long diffStartDiscovered;
    long diffStartAvailable;

    @Getter
    private AppServer appServer;

    private boolean wifiConnectionRequested = false;
    private List<String> connectedMac = new ArrayList<>();

    private HashMap<String, Network> hashMapOfNetworks = new HashMap<>();
    private List<PeerHandle> requestedConnectionList = new ArrayList<>();

    private PublishConfig publishConfig;
    private SubscribeConfig subscribeConfig;

    private int portToServer;
    private boolean isPortToServerSet = false;

    private HashMap<String, Integer> hashMapMacWithPort = new HashMap<String, Integer>();
    private int localPortServer;


    private List<PeerHandle> listOfPeersPortIsSentTo = new ArrayList<>();
    private HashMap<PeerHandle, Integer> hashMapPortAndServer = new HashMap<>();

    int data_init ;
    int data_resp ;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG, "the app is starting");
        if(start == 0){
            start = currentTimeMillis();
            Log.d("TESTING-LOG-TIME-START", String.valueOf(start));
        }
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

        sslContextedObserver = new SSLContextedObserver();
        sslContextedObserver.setListener(sslContext -> {
            connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            attachToSession();
        });
        sslContextedObserver.setSslContext(IdentityHandler.getSSLContext(this.context));


        addPeersToChatList();
        TestChatActivity.updateActivityMain(this);
        AppServer.updateActivity(this);
    }

    public void setRolePublisher(View view){
        role = "publisher";
        TextView tvRole = findViewById(R.id.tvRole);
        tvRole.setText(role);
        isPublisher = true;
        attachToSession();
    }

    public void setRoleSubscriber(View view){
        role = "subscriber";
        TextView tvRole = findViewById(R.id.tvRole);
        tvRole.setText(role);
        attachToSession();
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
        wifiConnectionRequested = false;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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
        ArrayList<ChatListItem> userList = new ArrayList<>();
        for (String macAddr : hashMapPeerHandleAndMac.keySet()){
            ChatListItem chatListDevices = new ChatListItem("MAC:", macAddr);
            userList.add(chatListDevices);
        }
        ChatsListAdapter chatListAdapter = new ChatsListAdapter(this, userList);
        ListView listViewChats = findViewById(R.id.listViewChats);
        listViewChats.setAdapter(chatListAdapter);
        listViewChats.setOnItemClickListener((parent, view, position, id) -> {
            String peerIpv6 = chatListAdapter.getChats().get(position).getPeerIpv6();
            MainActivity.this.openChat(position, peerIpv6);
        });
    }


    public void startPublishAndSubscribe(String role){
        if(role.equals("subscriber")){
            subscribe();
        } else {
            publish();
        }
        //subscribe();
        //publish();
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
                Log.d(LOG,"onAttachFailed");
            }
        }, new IdentityChangedListener() {
            @Override
            public void onIdentityChanged(byte[] mac) {
                Log.d(LOG, "onIdentitychanged");
                super.onIdentityChanged(mac);
                setMacAddress(mac);
                startPublishAndSubscribe(role);
            }
        }, null);
    }



    private void publish () {
        publishConfig = new PublishConfig.Builder()
                .setServiceName(Constants.SERVICE_NAME)
          //      .setPublishType(PublishConfig.PUBLISH_TYPE_UNSOLICITED)
                .build();

        if(wifiAwareSession!=null){

            wifiAwareSession.publish(publishConfig, new DiscoverySessionCallback() {
            @Override
            public void onSessionConfigFailed(){
                Log.i("LOG-Test-Debugging", "onSessionConfigFailed");
            }

            @Override
            public void onServiceDiscovered(PeerHandle peerHandle_, byte[] serviceSpecificInfo, List<byte[]> matchFilter) {
                super.onServiceDiscovered(peerHandle_, serviceSpecificInfo, matchFilter);
                Log.i("LOG-Test-Debugging", "publish onServiceDiscovered");
            }

            @Override
            public void onPublishStarted(PublishDiscoverySession session) {
                super.onPublishStarted(session);
                publishDiscoverySession = session;
                Log.d("LOG", "on publish started");
            }

            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onMessageReceived(PeerHandle peerHandle_, byte[] message) {
                super.onMessageReceived(peerHandle_, message);
                publishDiscoverySession.sendMessage(peerHandle_, MESSAGE, myMac);       //Så lenge den mottar meldinger, det skjer hele tiden hvis det er noen andre i nærheten
                Log.d(LOG, "message received publisher: " + message);
                if (!listOfPeersPortIsSentTo.contains(peerHandle_)){
                    if(appServer!=null){
                        sendPort(peerHandle_);
                    }
                }

                /*if (message.length == 2) {
                    portToUse = byteToPortInt(message);
                    Log.d(LOG, "subscribe, will use port number " + portToUse);
                } else*/ if (message.length == 6) {
                    setOtherMacAddress(message);
                    if (!hashMapPeerHandleAndMac.containsKey(macAddress)) {
                        hashMapPeerHandleAndMac.put(macAddress, peerHandle_);
                        addPeersToChatList();
                    }
                    for (String macAddr : hashMapPeerHandleAndMac.keySet()) {
                        if (!connectedMac.contains(macAddr)) {
                            requestWiFiConnection(peerHandle_, role);
                            connectedMac.add(macAddr);
                            byte[] msgtosend = "startConnection".getBytes();
                            publishDiscoverySession.sendMessage(peerHandle_, MESSAGE, msgtosend);   //OK
                        }
                    }

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
                    Log.d(LOG, "setOtherIPAddress " + message);
                } else {
                    Log.d(LOG, "MEssage length = " + message.length);
                }
                peerHandle = peerHandle_;
            }

            }, null);

        }
    }


    private void subscribe(){
        subscribeConfig = new SubscribeConfig.Builder()
                .setServiceName(Constants.SERVICE_NAME)
         //       .setSubscribeType(SubscribeConfig.SUBSCRIBE_TYPE_PASSIVE)
                .build();
        if(wifiAwareSession!= null){
            wifiAwareSession.subscribe(subscribeConfig, new DiscoverySessionCallback() {
                @Override
                public void onMessageSendFailed (int messageId) {
                    Log.i("LOG-Test-Debugging", "onMessageSendFailed");
                }
                @Override
                public void onSessionConfigFailed(){
                    Log.i("LOG-Test-Debugging", "onSessionConfigFailed");
                }
                @Override
                public void onSubscribeStarted(@NonNull SubscribeDiscoverySession session) {
                    super.onSubscribeStarted(session);
                    subscribeDiscoverySession = session;
                    Log.i("LOG-Test-Debugging", "subscribe started");

                    /*if (subscribeDiscoverySession != null && peerHandle != null) {
                        subscribeDiscoverySession.sendMessage(peerHandle, MAC_ADDRESS_MESSAGE, myMac);  //Hele tiden
                        Log.d(LOG, " subscribe, onServiceStarted send mac");
                    }*/
                }

                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void onServiceDiscovered(PeerHandle peerHandle_, byte[] serviceSpecificInfo, List<byte[]> matchFilter) {
                    super.onServiceDiscovered(peerHandle_, serviceSpecificInfo, matchFilter);

                    Log.i("LOG-Test-Debugging", "subscribe onServiceDiscovered");

                    if(discovered == 0){
                        discovered = currentTimeMillis();
                        Log.d("TESTING-LOG-TIME-DISCOVERY", String.valueOf(discovered));
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

                    Log.d(LOG, "Message Received" + messageIn);
                    if(message.length == 2) {
                        portToUse = byteToPortInt(message);
                        Log.d(LOG, "subscribe, will use port number "+ portToUse);
                    } else if (message.length == 6){
                        setOtherMacAddress(message);

                        if (!hashMapPeerHandleAndMac.containsKey(macAddress)){
                            hashMapPeerHandleAndMac.put(macAddress, peerHandle);
                            addPeersToChatList();
                        }
                    } else if (messageIn.contains("startConnection") && !wifiConnectionRequested) {
                        Log.d(LOG, "Message IN:" + messageIn);
                        requestWiFiConnection(peerHandle, "subscriber");
                    } else if(messageIn.contains("ServerPort:") && !isPortToServerSet) {
                        Log.d(LOG, "Message IN:" + messageIn);
                        portToServer = Integer.parseInt(messageIn.split(":")[1]);
                        if(!hashMapPortAndServer.containsValue(peerHandle)){
                            hashMapPortAndServer.put(peerHandle, portToServer);
                        }
                    }
                    else if (message.length == 16) {
                        //setOtherIPAddress(message);
                        Log.d(LOG, "setOtherIPAddress "+ message);
                    } else if (message.length > 16) {
                        Log.d(LOG, "Message IN:" + messageIn);

                        /*boolean hasRequestedConnection = hasRequestedConnection(peerHandle);
                        if(messageIn.contains("startConnection")  && !hasRequestedConnection) {
                            requestWiFiConnection(peerHandle, "subscriber");
                            requestedConnectionList.add(peerHandle);
                        }*/
                    }
                }
            }, null);
        }

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
        Log.d(LOG, "App onDestroy");
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


    private String getMacAddressFromPeerHandle(PeerHandle peerHandle){
        for (String macAddr : hashMapPeerHandleAndMac.keySet())
            if (hashMapPeerHandleAndMac.get(macAddr).equals(peerHandle)) {
                return macAddr;
            }
        return null;
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
        Log.d(LOG, "requestWiFiConnection");
        wifiConnectionRequested = true;

        if(role.equals("subscriber")){
            //DiscoverySession session = subscribeDiscoverySession;


            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(subscribeDiscoverySession, peerHandle)
                    .setPskPassphrase("password")
                    // .setTransportProtocol(6)
                    //.setPmk(test)
                    .build();

            Log.d(LOG, "This devices is subscriber");
        } else {

            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(publishDiscoverySession, peerHandle)
                    .setPskPassphrase("password")
                    .setTransportProtocol(6)
                    .build();

            Log.d(LOG, "This devices is publisher");
        }
        int data_init = WifiAwareManager.WIFI_AWARE_DATA_PATH_ROLE_INITIATOR;

        Log.d(LOG,  "requestWiFiConnection  init:" + data_init);
        int data_resp = WifiAwareManager.WIFI_AWARE_DATA_PATH_ROLE_RESPONDER;

        Log.d(LOG, "requestWiFiConnection  responder: " + data_resp);

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
            public void onAvailable(Network network) {
                super.onAvailable(network);
                hashMapOfNetworks.put(getMacAddressFromPeerHandle(peerHandle), network);
                Log.d(LOG, "onAvaliable + Network:" + network.toString());
                Toast.makeText(context, "On Available!", Toast.LENGTH_SHORT).show();
                //connectionHandler.setAppServer(new AppServer(sslContextedObserver.getSslContext(), Constants.SERVER_PORT));
                Log.d(LOG, "On available for " + role);

                if( available == 0){
                    available = currentTimeMillis();
                    Log.d(LOG, String.valueOf(available));
                    diffStartAvailable = available - start;
                    diffStartDiscovered = discovered - start;
                    Log.d("TESTING-LOG-TIME-DIFF", + diffStartDiscovered + ":" + diffStartAvailable);
                }


                if(appServer == null){
                    appServer = new AppServer(sslContextedObserver.getSslContext(), network);
                }



                connectionHandler = new ConnectionHandler(getApplicationContext(), sslContextedObserver.getSslContext(), keyPair, appServer, isPublisher);
            }


            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.d(LOG, "entering onUnavailable ");
                Toast.makeText(context, "onUnavailable", Toast.LENGTH_SHORT).show();
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
                Log.d(LOG, "onCapabilitiesChanged");
                //networkCapabilities = networkCapabilities;
                //network = network_;
                peerAwareInfo = (WifiAwareNetworkInfo) networkCapabilities.getTransportInfo();
                setPeerIpv6(peerAwareInfo.getPeerIpv6Addr());
                Log.d(LOG, "Peeripv6: " + peerAwareInfo.getPeerIpv6Addr() );



                // WifiAwareNetworkInfo wifiInfo = (WifiAwareNetworkInfo) networkCapabilities_.getTransportInfo();
                //String ssid = wifiInfo.get;
            }


            @Override
            public void onLost(Network network) {
                super.onLost(network);
                String mac = getMacAddressFromNetwork(network);
                Log.d("LOG-Test-Debugging", "onLOST: " + mac + network.toString());
                hashMapPeerHandleAndMac.remove(mac);
                hashMapOfNetworks.remove(mac);
                new Handler(Looper.getMainLooper()).post(()-> {
                    addPeersToChatList();
                });
                //addPeersToChatList();
                if(hashMapPeerHandleAndMac.isEmpty()){
                    Log.d(LOG, "HashMap empty? yes");
                    appServer.stop();
                    appServer = null;
                    new Handler(Looper.getMainLooper()).post(()-> {
                        setUpNewConnection();
                    });
                } /*else {
                    for (Network networkHash : hashMapOfNetworks.values()){
                        Log.d("LOG-Test-Debugging", networkHash.toString() );
                    }
                    for (String string : hashMapPeerHandleAndMac.keySet()){
                        Log.d("LOG-Test-Debugging", string );
                    }
                }*/
            }
        });
    }


    private void setPeerIpv6(Inet6Address ipv6){
        this.peerIpv6 = ipv6;
    }

    private void sendPort(PeerHandle peerHandle){
        listOfPeersPortIsSentTo.add(peerHandle);
        String portStr = "ServerPort:"+ localPortServer;
        byte [] message = portStr.getBytes();
        publishDiscoverySession.sendMessage(peerHandle, MESSAGE, message);
        Log.d(LOG, portStr );
    }

    public void setServerPort(Network network){
        if(role.equals("publisher")){
            localPortServer = appServer.getLocalPort();
            hashMapMacWithPort.put(getMacAddressFromNetwork(network), localPortServer);
            PeerHandle peer = hashMapPeerHandleAndMac.get(getMacAddressFromNetwork(network));
            sendPort(peer);
        }
    }
    private HashMap<String, Integer> hashMapPortMac = new HashMap<String, Integer>();

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void openChat(int position, String peerIpv6){
        hashMapMacWithPort.put(peerIpv6, portToServer);
        byte [] message = "goToChat".getBytes();
        publishDiscoverySession.sendMessage(hashMapPeerHandleAndMac.get(peerIpv6), MESSAGE, message); //TODO
        Intent intentChat = new Intent(this, TestChatActivity.class);  //TODO: change back to chat activity just testing
        intentChat.putExtra("position", position);
        Contact contact = new Contact(IdentityHandler.getCertificate());
        intentChat.putExtra("contact", contact);
        intentChat.putExtra("port", portToServer);
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
        macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);

        EditText editText = findViewById(R.id.eTOtherMac);
        editText.setText(macAddress);
    }


    private void setOtherIPAddress(byte[] ip) {
        //otherIP = ip;
    }



    public int byteToPortInt(byte[] bytes){
        return ((bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF));
    }


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

    public KeyPair getKeyPair(){
        return keyPair;
    }

}
