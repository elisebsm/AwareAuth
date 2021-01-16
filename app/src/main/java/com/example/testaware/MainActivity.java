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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;


public class MainActivity extends AppCompatActivity {

    private boolean supportsAware;
    private String LOG = "LOG-Test-Aware";
    private SubscribeDiscoverySession mainSession;
    private WifiAwareManager wifiAwareManager;
    private WifiAwareSession wifiAwareSession;
    private String serviceName = "Receive-Message-Service";
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

        wifiAwareManager = null;
        wifiAwareSession = null;
        connectivityManager = null;
        networkSpecifier = null;
        publishDiscoverySession = null;
        subscribeDiscoverySession = null;
        peerHandle = null;


        //Button calling the subscribe function
        Button subscribeBtn = findViewById(R.id.btnSubscribe);
        subscribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribe();
                Log.i(LOG, "Subscribe button pressed");
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


        // Messages for whether or not device has WiFi Aware
        //Toast hasAware = Toast.makeText(this, "WiFi Aware is available", Toast.LENGTH_LONG);
        //Toast noAware = Toast.makeText(this, "WiFi Aware is not available", Toast.LENGTH_LONG);
        supportsAware = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE);
        if(supportsAware){
            //hasAware.show();
            Log.i(LOG, "The device supports Wi-Fi Aware");

            //attachToSession();
        } else {
            //noAware.show();
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
                    //attachToSession();
                } else {
                    awareUnsupported.show();
                }
            }
        };
        this.registerReceiver(myReceiver, filter);

        /*//Obtain a session, by calling attach(). Joins or forms a WifI aware cluster.
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
        }, null);*/


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
                    Log.d("received", "will use port number "+ portToUse);
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
                peerHandle = peerHandle_;
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
                    Log.d("nanSUBSCRIBE", "onServiceStarted send mac");
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
                Log.d("nanSUBSCRIBE", "received message");
                Toast.makeText(MainActivity.this, "received", Toast.LENGTH_LONG).show();
                if(message.length == 2) {
                    portToUse = byteToPortInt(message);
                    Log.d("received", "will use port number "+ portToUse);
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
        String macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
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
            Log.d("myTag", "socket exception " + e.toString());
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
            Log.d(LOG, "publisher, making network specifier");
            //starting serverSocket on publisher device
            //ServerSocket serverSocket = new ServerSocket(0);
            //port = serverSocket.getLocalPort();

            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(publishDiscoverySession, peerHandle)
                    .setPskPassphrase("somePassword")
                    //.setPort(port)
                    .build();

        } else{
            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(subscribeDiscoverySession, peerHandle)
                    .setPskPassphrase("somePassword")
                    .build();
            Toast.makeText(context, "subscriber in request", Toast.LENGTH_LONG).show();
        }
        Toast.makeText(context, "subscriber in request", Toast.LENGTH_LONG).show();
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
                openSocket();
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
        int peerPort = peerAwareInfo.getPort();
        try {
            Socket socket = network.getSocketFactory().createSocket(peerIpv6, peerPort);
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
                ServerSocket server = null;
                Socket socket = null;
                DataInputStream in = null;
                //start server and wait for conn

                try {
                    server = new ServerSocket(0);   //TODOO: set port correctly
                    while (true) {
                        Log.d("serverThread", "Server started, Waiting for client");
                        socket = server.accept();
                        Log.d("serverThread", "Client accepted");
                        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                        String str = (String) in.readUTF();
                        Log.d("serverThread", "Reading message from client");
                        EditText editText = (EditText) findViewById(R.id.eTOtherMac);
                        editText.setText(str);
                    }
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
                try {
                    socket.close();
                    in.close();
                    Log.d("serverThread", "Closing conn");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }


    //only sends messages
    private void clientSendMessage(final Inet6Address ipv6Address){
        Runnable serverTask = new Runnable(){  //new thread for each client server conn
            @Override
            public void run() {
                //initialize socket and input stream
                Socket socket = null;
                //DataInputStream in = null;   //dont need it yet
                DataOutputStream out= null;
                try {

                    while (true) {
                        socket = new Socket(ipv6Address, 0);   //just testing with port 0
                        String msg= "messageToBeSent: ";
                        EditText editText = (EditText)findViewById(R.id.eTOtherMac);
                        msg += editText.getText().toString();
                        out= new DataOutputStream(socket.getOutputStream());
                        out.writeUTF(msg);
                        out.flush();
                    }
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
                try {
                    socket.close();
                    //in.close();
                    out.close();
                    Log.d("serverThread", "Closing conn");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }


    //private WifiAwareSession mAwaresession;
    /*public void onAttached(final WifiAwareSession session) {
        Log.d(LOG, "Attach operation completed and can now start discovery sessions");
        final byte[] messageSend = new byte[1];
        messageSend[0] = 104;
        mAwaresession = session;
        // Start Subscribe Session
        SubscribeConfig config = new SubscribeConfig.Builder()
                .setServiceName("Receive_Message_Service")
                .build();

        mAwaresession.subscribe(config, new DiscoverySessionCallback() {

            SubscribeDiscoverySession mainSession;

            @Override
            public void onSubscribeStarted(SubscribeDiscoverySession session) {
                Log.d("Method Called", "onSubscribeStarted");
                mainSession = session;
            }
            @Override
            public void onServiceDiscovered(PeerHandle peerHandle, byte[] serviceSpecificInfo,
                                            List<byte[]> matchFilter) {
                Log.d("Method Called", "onServiceDiscovered");
                mainSession.sendMessage(peerHandle, 1, messageSend);
            }
            @Override
            public void onMessageReceived(PeerHandle peerHandle, byte[] messageFromPub) {
                Log.d("Method Called", "onMessageReceived");
                Log.d("Messaged Received is", new String(messageFromPub));
                //messageReceived = messageFromPub;
            }}, null);
        };*/




   /* An application must use {@link #attach(AttachCallback, Handler)} to initialize a
 *     Aware cluster - before making any other Aware operation. Aware cluster membership is a
 *     device-wide operation - the API guarantees that the device is in a cluster or joins a
 *     Aware cluster (or starts one if none can be found). Information about attach success (or
 *     failure) are returned in callbacks of {@link AttachCallback}. Proceed with Aware
 *     discovery or connection setup only after receiving confirmation that Aware attach
 *     succeeded - {@link AttachCallback#onAttached(WifiAwareSession)}. When an
 *     application is finished using Aware it <b>must</b> use the
 *     {@link WifiAwareSession#close()} API to indicate to the Aware service that the device
 *     may detach from the Aware cluster. The device will actually disable Aware once the last
 *     application detaches.
 *
 *
 *  An application <b>must</b> call {@link WifiAwareSession#close()} when done with the
    * Wi-Fi Aware object.
    * <p>
    * Note: a Aware cluster is a shared resource - if the device is already attached to a cluster
    * then this function will simply indicate success immediately using the same {@code
    * attachCallback}.
    *
    * * @param handler The Handler on whose thread to execute the callbacks of the {@code
    * attachCallback} object. If a null is provided then the application's main thread will be
    *                used.
*/

   /*TODO: create toast which indicates if you have joined or formed a cluster depending on attach success/failure (AttachCallback)*/
/*
    public void attach(AttachCallback attachCallback, Handler handler) {
        attach(attachCallback, handler);
        attachCallback.toString()
    }*/

}