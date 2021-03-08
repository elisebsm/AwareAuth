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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.testaware.activities.MainActivity;
import com.example.testaware.listitems.ChatListItem;
import com.example.testaware.Constants;
import com.example.testaware.models.Contact;
import com.example.testaware.R;
import com.example.testaware.ConnectionHandler;
import com.example.testaware.adapters.ChatsListAdapter;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;


public class WifiAwareConnectionHandler {

    String LOG = "WifiAware Connection Handler";
    private WifiAwareManager wifiAwareManager;
    private WifiAwareSession wifiAwareSession;

    private PeerHandle peerHandle;

    private PublishDiscoverySession   publishDiscoverySession;
    private SubscribeDiscoverySession subscribeDiscoverySession;
    private List<PeerHandle> peerHandleList = new ArrayList<PeerHandle>();

    private final int MAC_ADDRESS_MESSAGE = 11;

    private byte[] myMac;

    private String macAddress;


    private List<String> otherMacList = new ArrayList<String>(); //TODO hva gjør denne??


    private byte[]                    otherIP;
    private byte[]                    msgtosend;
    private boolean isPublisher;

    WifiAwareConnectionHandler(Context context){
        initializeWiFiAware();
    }
    //TODO: send short message to establish who will work as publisher and who subscriber

    private void initializeWiFiAware() {
        wifiAwareManager = MainActivity.getWifiAwareManager();

        //TODO: check if a cluster exists, if not the first user will be publisher?
        wifiAwareManager.attach(new AttachCallback() {
            @Override
            public void onAttached(WifiAwareSession session) {
                super.onAttached(session);
                Log.i(LOG, "ON Attached!");
                wifiAwareSession = session;
                //TODO: close session ?
            }
            //TODO: make onattachfailed
        }, new IdentityChangedListener() {
            @Override
            public void onIdentityChanged(byte[] mac) {
                super.onIdentityChanged(mac);
                //setMacAddress(mac);
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
                Log.i(LOG, "publish started");
                super.onPublishStarted(session);
                publishDiscoverySession = session;
            }

            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onMessageReceived(PeerHandle peerHandle_, byte[] message) {
                super.onMessageReceived(peerHandle, message);
                if(message.length == 2) {
                    // portToUse = byteToPortInt(message);
                    // Log.d(LOG, "will use port number "+ Constants.SERVER_PORT);
                } else if (message.length == 6){
                    setOtherMacAddress(message);
//                    Toast.makeText(MainActivity.this, "mac received", Toast.LENGTH_LONG).show();
                } else if (message.length == 16) {
                    setOtherIPAddress(message);
                    //                   Toast.makeText(MainActivity.this, "ip received", Toast.LENGTH_LONG).show();
                } else if (message.length > 16) {
                    setMessage(message);
//                    Toast.makeText(MainActivity.this, "message received", Toast.LENGTH_LONG).show();
                }
                peerHandle = peerHandle_;
                if (!otherMacList.contains(macAddress)){
                    otherMacList.add(macAddress);
                    peerHandleList.add(peerHandle);
                    int numOfSubscribers = peerHandleList.size();   //number of subscribers
                    //Toast.makeText(WifiAwareConnectionHandler.this, "Subscribers: "+ numOfSubscribers, Toast.LENGTH_LONG).show();
                }
            }
        }, null);
    }


    private void subscribe(){
        SubscribeConfig config = new SubscribeConfig.Builder()
                .setServiceName(Constants.SERVICE_NAME)
                .build();
        //DiscoverySessionCallback is specified when events occur, for example when a publisher is discovered
        //can also use this method to communicate with publisher
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
                //Toast.makeText(MainActivity.this, "received", Toast.LENGTH_LONG).show();
                if(message.length == 2) {
                    //portToUse = byteToPortInt(message);
                    //Log.d(LOG, "subscribe, will use port number "+ portToUse);
                } else if (message.length == 6){
                    setOtherMacAddress(message);
                    //Toast.makeText(MainActivity.this, "mac received", Toast.LENGTH_LONG).show();
                } else if (message.length == 16) {
                    setOtherIPAddress(message);
                    //Toast.makeText(MainActivity.this, "ip received", Toast.LENGTH_LONG).show();
                } else if (message.length > 16) {
                    setMessage(message);
                    //Toast.makeText(MainActivity.this, "message received", Toast.LENGTH_LONG).show();
                }
            }
        }, null);
    }

    //TODO permission requests
    //TODO: check if there exissts a cluster with service name, if so subscribe


    /**
     * Helper to set the status field.
     */
    private void setMessage(byte[] msg) {
        String outmsg = new String(msg).replace("messageToBeSent: ","");
        //EditText editText = (EditText) findViewById(R.id.eTMsg);
        //editText.setText(outmsg);
    }

    private void setMacAddress(byte[] mac) {
        myMac = mac;
        String macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
        //EditText editText = findViewById(R.id.eTYourMac);
        //editText.setText(macAddress);
    }

    private void setOtherMacAddress(byte[] mac) {
        //otherMac = mac;
        macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);

        //EditText editText = findViewById(R.id.eTOtherMac);
        //editText.setText(macAddress);
    }


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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestWiFiConnection() throws IOException {

        WifiAwareNetworkSpecifier networkSpecifier;
        if (isPublisher){
            networkSpecifier = new WifiAwareNetworkSpecifier.Builder(publishDiscoverySession, peerHandle)
                    .setPskPassphrase("somePassword")
                    .setPort(Constants.SERVER_PORT)
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

            }

            @Override
            public void onCapabilitiesChanged(Network network_, NetworkCapabilities networkCapabilities_) {
                Toast.makeText(context, "onCapabilitiesChanged", Toast.LENGTH_LONG).show();
                Log.d(LOG, "onCapabilitiesChanged");
                networkCapabilities = networkCapabilities_;
                network = network_;
                peerAwareInfo = (WifiAwareNetworkInfo) networkCapabilities.getTransportInfo();
                peerIpv6 = peerAwareInfo.getPeerIpv6Addr();
                String hostname = peerIpv6.getHostName();
                //peerPort = peerAwareInfo.getPort();
                //port is set in startServer, so no point in setting it before startServer is run

            }

            @Override
            public void onLost(Network network_) {
                Toast.makeText(context, "onLost", Toast.LENGTH_LONG).show();
                Log.d(LOG, "onLost");
            }
        });
    }
}
