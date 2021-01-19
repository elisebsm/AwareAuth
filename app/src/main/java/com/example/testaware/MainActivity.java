package com.example.testaware;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.testaware.adapters.ChatsListAdapter;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private boolean supportsAware;
    private String LOG = "LOG-Test-Aware";
    private SubscribeDiscoverySession mainSession;
    private WifiAwareManager wifiAwareManager;
    private WifiAwareSession wifiAwareSession;
    private String serviceName = "Elise";
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
    private Inet6Address              ipv6;

    private ServerSocket              serverSocket;
    private final int                 MESSAGE                        = 7;
    private NetworkCapabilities networkCapabilities;
    private Network network;

    private WifiAwareNetworkInfo peerAwareInfo;

    private Inet6Address peerIpv6 ;
    private int peerPort;

    private EditText editTextLongMessage;

    private List<PeerHandle> peerHandleList = new ArrayList<PeerHandle>();
    private List<String> otherMacList = new ArrayList<String>();
    private String macAddress;


    // Hentet fra:https://github.com/anagramrice/NAN/blob/master/app/src/main/java/net/mobilewebprint/nan/MainActivity.java
    private static final int MY_PERMISSION_COARSE_LOCATION_REQUEST_CODE = 88;
    private static final int MY_PERMISSION_FINE_LOCATION_CODE = 99;
    private static final int MY_PERMISSION_NETWORK_STATE_CODE = 77;
    //-------------------------------------------------------------------------------------------------------------------------

    private boolean isPublisher = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        /*wifiAwareManager = null;
        wifiAwareSession = null;
        connectivityManager = null;
        networkSpecifier = null;
        publishDiscoverySession = null;
        subscribeDiscoverySession = null;
        peerHandle = null;*/

        //TODO: make array of contacts/Chats, check that current user is not added to the list
        ArrayList<Chat> testArrayOfChats = new ArrayList<Chat>();
        ChatsListAdapter chatListAdapter = new ChatsListAdapter(this, testArrayOfChats);
        Chat chatElise = new Chat("Elise");
        Chat chatKirsten = new Chat("Kirsten");

        testArrayOfChats.add(chatElise);
        testArrayOfChats.add(chatKirsten);

        //ArrayAdapter<String> chatListAdapter = new ArrayAdapter<String>(this, R.layout.chat_list_elements, testArray);

        ListView listViewChats = findViewById(R.id.listViewChats);
        listViewChats.setAdapter(chatListAdapter);
        listViewChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openChat();
            }
        });

        //Button calling the subscribe function
        Button subscribeBtn = findViewById(R.id.btnSubscribe);
        subscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribe();
                Log.i(LOG, "Subscribe button pressed");
                findViewById(R.id.btnConnectSub).setVisibility(View.VISIBLE);
            }
        });

        //Button calling the publish function
        Button publishBtn = findViewById(R.id.btnPublish);
        publishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish();
                Log.i(LOG, "Publish button pressed");
                isPublisher = true;
                findViewById(R.id.btnConnectPub).setVisibility(View.VISIBLE);
            }
        });

        Button connectBtn = findViewById(R.id.btnConnectPub);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        requestWiFiConnection();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Button connectBtnSub = findViewById(R.id.btnConnectSub);
        connectBtnSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        requestWiFiConnection();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //------------------------------------------------------------------------------------------------------
        Button send = findViewById(R.id.btnSend);        /* +++++ */
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg= "messageToBeSent: ";
                EditText editText = (EditText)findViewById(R.id.eTMsg);
                msg += editText.getText().toString();
                msgtosend = msg.getBytes();
                if (publishDiscoverySession != null && peerHandle != null) {
                    publishDiscoverySession.sendMessage(peerHandle, MESSAGE, msgtosend);
                } else if(subscribeDiscoverySession != null && peerHandle != null) {
                    subscribeDiscoverySession.sendMessage(peerHandle, MESSAGE, msgtosend);
                }
            }
        });                                                                                   /* ----- */
        //------------------------------------------------------------------------------------------------------

        Button buttonSendLongMessage = findViewById(R.id.btnSendLongMessage);
        buttonSendLongMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    clientSendMessage(peerIpv6, portToUse);
                }
            }
        });


        // Hentet fra:https://github.com/anagramrice/NAN/blob/master/app/src/main/java/net/mobilewebprint/nan/MainActivity.java
        setupPermissions();
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


        supportsAware = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE);
        if(supportsAware){
            Log.i(LOG, "The device supports Wi-Fi Aware");
        } else {
            Log.i(LOG, "The device DOES NOT supports Wi-Fi Aware");
        }


        wifiAwareManager = (WifiAwareManager) getSystemService(Context.WIFI_AWARE_SERVICE);

        // Messages for whether or not device has WiFi Aware
        final Toast awareSupported = Toast.makeText(this, "Wi-Fi Aware Supported", Toast.LENGTH_LONG);
        final Toast awareUnsupported = Toast.makeText(this, "Wi-Fi Aware Unsupported. Do you have  Wi-Fi or Location OFF? ", Toast.LENGTH_LONG);
        IntentFilter filter =
                new IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED);
        BroadcastReceiver myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // discard current sessions
                if (wifiAwareManager.isAvailable()) {
                    awareSupported.show();
                } else {
                    awareUnsupported.show();
                }
            }
        };
        this.registerReceiver(myReceiver, filter);

        attachToSession();
    }

    private void attachToSession(){
        //Obtain a session, by calling attach(). Joins or forms a WifI aware cluster.
        wifiAwareManager.attach(new AttachCallback() {
            @Override
            public void onAttached(WifiAwareSession session) {
                super.onAttached(session);
                Log.i(LOG, "ON Attached!");
                wifiAwareSession = session;
                //TODO: close session
            }
            //TODO: make onattachfailed
        }, new IdentityChangedListener() {
            @Override
            public void onIdentityChanged(byte[] mac) {
                super.onIdentityChanged(mac);
                setMacAddress(mac);
            }
        }, null);

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
     * App Permissions for Coarse Location
     **/
    private void setupPermissions(){
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // And if we're on SDK M or later...
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Ask again, nicely, for the permissions.
                String[] permissionsWeNeed = new String[]{ Manifest.permission.ACCESS_FINE_LOCATION };
                requestPermissions(permissionsWeNeed, MY_PERMISSION_FINE_LOCATION_CODE);
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
                    Log.i(LOG, "Permission for location granted.");
                    return;

                } else {
                    Log.i(LOG, "Permission for location not granted. NAN can't run.");
                    finish();
                    // The permission was denied, so we can show a message why we can't run the app
                    // and then close the app.
                }
            }

            //-------------------------------------------------------------------------------------------- +++++
            case MY_PERMISSION_FINE_LOCATION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(LOG, "Fine location permission granted");
                    return;

                } else {
                    Log.i(LOG, "Fine location permission not granted");
                }
            }
            //-------------------------------------------------------------------------------------------- -----

            case MY_PERMISSION_NETWORK_STATE_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(LOG, "Network state permission granted");
                    return;

                } else {
                    Log.i(LOG, "Network state permission not granted");
                }
            }
            // Other permissions could go down here
        }
    }


    //testing by having subscriber subcribing to published service "receive_message_service" and sending message to
    //the publisher
    //make a service discoverable by callin publish()
    private void publish () {
        //specify name of service and optional match filters
        PublishConfig config = new PublishConfig.Builder()
                .setServiceName(serviceName)
                .build();
                //TODO: set service specific info ?

        //specify actions when events occur, such when the subscriber receives a message
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
                if(message.length == 2) {
                    portToUse = byteToPortInt(message);
                    Log.d(LOG, "will use port number "+ portToUse);
                } else if (message.length == 6){
                    setOtherMacAddress(message);
                    //Toast.makeText(MainActivity.this, "mac received", Toast.LENGTH_LONG).show();
                } else if (message.length == 16) {
                    setOtherIPAddress(message);
                    Toast.makeText(MainActivity.this, "ip received", Toast.LENGTH_LONG).show();
                } else if (message.length > 16) {
                    setMessage(message);
                    Toast.makeText(MainActivity.this, "message received", Toast.LENGTH_LONG).show();
                }
                peerHandle = peerHandle_;
                if (!otherMacList.contains(macAddress)){
                    otherMacList.add(macAddress);
                    Log.d(LOG, "mac list"+ otherMacList);

                    peerHandleList.add(peerHandle);
                    int numOfSubscribers = peerHandleList.size();   //number of subscribers
                    Toast.makeText(MainActivity.this, "Subscribers: "+ numOfSubscribers, Toast.LENGTH_LONG).show();

                }




            }
        }, null);
    }


    //subsribe method is used to subscribe to a service
    private void subscribe(){
        //specify name of the service to subscribe to
        SubscribeConfig config = new SubscribeConfig.Builder()
                .setServiceName(serviceName)
                .build();
        //DiscoverySessionCallback is specified when events occur, for example when a publisher is discovered
        //can also use this method to communicate with publisher
        wifiAwareSession.subscribe(config, new DiscoverySessionCallback() {
            //Need discoverySession in order to send message
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
            //called when matching publishers come into wifi range
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onServiceDiscovered(PeerHandle peerHandle_, byte[] serviceSpecificInfo, List<byte[]> matchFilter) {
                super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter);
                //String message = "Hei!";
                peerHandle=peerHandle_;
                if (subscribeDiscoverySession != null && peerHandle != null) {
                    subscribeDiscoverySession.sendMessage(peerHandle, MAC_ADDRESS_MESSAGE, myMac);
                }
            }
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
                    Toast.makeText(MainActivity.this, "mac received", Toast.LENGTH_LONG).show();
                } else if (message.length == 16) {
                    setOtherIPAddress(message);
                    Toast.makeText(MainActivity.this, "ip received", Toast.LENGTH_LONG).show();
                } else if (message.length > 16) {
                    setMessage(message);
                    Toast.makeText(MainActivity.this, "message received", Toast.LENGTH_LONG).show();
                }
            }
        }, null);

    }

    /**
     * Helper to set the status field.
     */
    private void setMessage(byte[] msg) {
        String outmsg = new String(msg).replace("messageToBeSent: ","");
        EditText editText = (EditText) findViewById(R.id.eTMsg);
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

    //-------------------------------------------------------------------------------------------- +++++
    private void setOtherIPAddress(byte[] ip) {
        otherIP = ip;
        try {
            String ipAddr = Inet6Address.getByAddress(otherIP).toString();
            //EditText editText = (EditText) findViewById(R.id.IPv6text);
            //editText.setText(ipAddr);
        } catch (UnknownHostException e) {
            Log.d(LOG, "socket exception " + e.toString());
        }
    }


    public int byteToPortInt(byte[] bytes){
        return ((bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF));
    }

    public byte[] portToBytes(int port){
        byte[] data = new byte [2];
        data[0] = (byte) (port & 0xFF);
        data[1] = (byte) ((port >> 8) & 0xFF);
        return data;
    }


    private int port;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestWiFiConnection() throws IOException {

        if (isPublisher){
            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(publishDiscoverySession, peerHandle)
                    .setPskPassphrase("somePassword")
                    //.setPort(port)
                    //.setTransportProtocol(6)
                    .build();

        } else{
            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(subscribeDiscoverySession, peerHandle)
                    .setPskPassphrase("somePassword")
                    .build();
        }
        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest myNetworkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
                .setNetworkSpecifier(networkSpecifier)
                .build();
        Toast.makeText(context, "networkrequest build", Toast.LENGTH_LONG).show();

        connectivityManager.requestNetwork(myNetworkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network_) {
                Toast.makeText(context, "onAvaliable", Toast.LENGTH_LONG).show();
                Log.d(LOG, "onAvaliable");
            }

            @Override
            public void onCapabilitiesChanged(Network network_, NetworkCapabilities networkCapabilities_) {
                Toast.makeText(context, "onCapabilitiesChanged", Toast.LENGTH_LONG).show();
                Log.d(LOG, "onCapabilitiesChanged");
                networkCapabilities = networkCapabilities_;
                network = network_;
                peerAwareInfo = (WifiAwareNetworkInfo) networkCapabilities.getTransportInfo();
                peerIpv6 = peerAwareInfo.getPeerIpv6Addr();
                peerPort = peerAwareInfo.getPort();   //port is set in startServer, so no point in setting it before startServer is run
                startServer();
            }

            @Override
            public void onLost(Network network_) {
                Toast.makeText(context, "onLost", Toast.LENGTH_LONG).show();
                Log.d(LOG, "onLost");
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void openSocket(){
        Log.d(LOG, "openSocket");
        //-------------------------------------------------------------------------------------------- +++++
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            // And if we're on SDK M or later...
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Ask again, nicely, for the permissions.
                String[] permissionsWeNeed = new String[]{ Manifest.permission.ACCESS_NETWORK_STATE };
                requestPermissions(permissionsWeNeed, MY_PERMISSION_NETWORK_STATE_CODE);
            }
        }
        //-------------------------------------------------------------------------------------------- -----
        WifiAwareNetworkInfo peerAwareInfo = (WifiAwareNetworkInfo) networkCapabilities.getTransportInfo();
        Inet6Address peerIpv6 = peerAwareInfo.getPeerIpv6Addr();
        //int peerPort = peerAwareInfo.getPort();
        try {
            Socket socket = network.getSocketFactory().createSocket(peerIpv6, portToUse);
            socket.getPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //only receives messages
    public void startServer(){
        Runnable serverTask = new Runnable(){  //new thread for each client server conn
            @Override
            public void run() {
                //initialize socket and input stream
                ServerSocket server;
                Socket socket = null;
                DataInputStream inputStream = null;
                //start server and wait for conn
                try {
                    server = new ServerSocket(0);
                    int port = server.getLocalPort();
                    Log.d(LOG, String.valueOf(port));

                    //TODOO: set port correctly
                    while (true) {
                        portOnSystem = portToBytes(port);   //get port set by server, and send it to the client (publisher or subscriber)
                        if (publishDiscoverySession != null && peerHandle != null) { //client can either publisher or subscriber
                            publishDiscoverySession.sendMessage(peerHandle, MAC_ADDRESS_MESSAGE, portOnSystem);
                        } else if (subscribeDiscoverySession != null && peerHandle != null)  {
                            subscribeDiscoverySession.sendMessage(peerHandle, MAC_ADDRESS_MESSAGE, portOnSystem);
                        }
                        Log.d(LOG, "Server started, Waiting for client");
                        socket = server.accept();
                        Log.d(LOG, "Client accepted");
                        inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                        strMessageFromClient = (String) inputStream.readUTF();
                        Log.d(LOG, "Reading message from client");
                        editTextLongMessage= (EditText) findViewById(R.id.textViewSendLongMessage);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editTextLongMessage.setText(strMessageFromClient);
                            }
                        });

                        
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            /*


            */
                /*try {
                    socket.close();
                    in.close();
                    Log.d(LOG, "Closing conn");
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        };

        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }

    private String strMessageFromClient;

    //only sends messages
    private void clientSendMessage(final Inet6Address ipv6Address, final int port){
        Runnable serverTask = new Runnable(){  //new thread for each client server conn
            @Override
            public void run() {
                //initialize socket and input stream
                Socket socket = null;
                //DataInputStream in = null;   //dont need it yet
                DataOutputStream outputStream= null;
                try {

                        socket = new Socket(ipv6Address, port);   //just testing with port 0
                        String msg= "";
                        editTextLongMessage = (EditText)findViewById(R.id.textViewSendLongMessage);
                        msg += editTextLongMessage.getText().toString();
                        outputStream= new DataOutputStream(socket.getOutputStream());
                        outputStream.writeUTF(msg);
                        outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            /*
                while(true){
                    byte[] sysPort= portToBytes(server.getLocalPort());
                    if (publishDiscoverySession != null && peerHandle != null) {
                        publishDiscoverySession.sendMessage(peerHandle, MAC_ADDRESS_MESSAGE, sysPort);
                    } else if (subscribeDiscoverySession != null && peerHandle != null)  {
                        subscribeDiscoverySession.sendMessage(peerHandle, MAC_ADDRESS_MESSAGE, sysPort);
                    }

            */
                /*try {
                    socket.close();
                    //in.close();
                    out.close();
                    Log.d(LOG, "Closing conn");
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }


    private void openChat(){
        Intent intentChat = new Intent(this, ChatActivity.class);
        startActivity(intentChat);

    }
}