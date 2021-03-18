/*

package com.example.testaware;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
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
import android.net.wifi.aware.WifiAwareSession;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.testaware.activities.MainActivity;

import java.net.Inet6Address;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;



public class WiFiAwareConnectionManager {

    @Getter
    private WifiAwareManager wifiAwareManager;

     WiFiAwareConnectionManager(Context context) {
        wifiAwareManager.WI
    }
}
*/
/*
    private boolean supportsAware;
    private String LOG = "LOG-Test-Aware";
    private SubscribeDiscoverySession mainSession;
    // private WifiAwareManager wifiAwareManager;
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


    private PublishDiscoverySession publishDiscoverySession;
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
    private ConnectivityManager connectivityManager;
    private NetworkSpecifier networkSpecifier;


    private final int                 IP_ADDRESS_MESSAGE             = 33;

    private String ipAddr; //other IP

    private ServerSocket serverSocket;
    private final int                 MESSAGE                        = 7;
    private NetworkCapabilities networkCapabilities;
    private Network network;

    private WifiAwareNetworkInfo peerAwareInfo;

    private static Inet6Address peerIpv6 ;
    private static Inet6Address sendPeerIp;
    private int peerPort;

    private EditText editTextLongMessage;

    private List<PeerHandle> peerHandleList = new ArrayList<PeerHandle>();
    private List<String> otherMacList = new ArrayList<String>();
    private String macAddress;

    private static final int MY_PERMISSION_COARSE_LOCATION_REQUEST_CODE = 88;
    private static final int MY_PERMISSION_FINE_LOCATION_CODE = 99;
    private static final int MY_PERMISSION_NETWORK_STATE_CODE = 77;

    private static boolean isPublisher = false;
    private boolean hasEstablishedPublisherAndSubscriber = false;
    private static WifiAwareManager wifiAwareManager;

    public WiFiAwareConnectionManager() {

        wifiAwareManager = MainActivity.getAwareManager();
        attachToSession();

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute("start");


    }

    private void establishWhoIsPublisherAndSubscriber(){
        if(!hasEstablishedPublisherAndSubscriber){
            if (publishDiscoverySession != null && subscribeDiscoverySession != null){
                isPublisher = true;
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
        }
    }

    public static boolean isPublisher(){
        return isPublisher;
    }
    public void startPublishAndSubscribe(){
        publish();
        subscribe();

    }
    private void attachToSession(){
        //Obtain a session, by calling attach(). Joins or forms a WifI aware cluster.
        //TODO: check if a cluster exists, if not the first user will be publisher?
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
                startPublishAndSubscribe();
            }
        }, null);
    }


    private void publish () {
        //specify name of service and optional match filters
        PublishConfig config = new PublishConfig.Builder()
                .setServiceName(Constants.SERVICE_NAME)
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
                    // portToUse = byteToPortInt(message);
                } else if (message.length == 6){
                    setOtherMacAddress(message);
                } else if (message.length == 16) {
                    setOtherIPAddress(message);
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
                    Log.i(LOG, "Number of subscribers:" + numOfSubscribers);
                }
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
            //called when matching publishers come into wifi range
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onServiceDiscovered(PeerHandle peerHandle_, byte[] serviceSpecificInfo, List<byte[]> matchFilter) {
                super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter);
                peerHandle=peerHandle_;
                if (subscribeDiscoverySession != null && peerHandle != null) {
                    subscribeDiscoverySession.sendMessage(peerHandle, MAC_ADDRESS_MESSAGE, myMac);
                }
            }
            @Override
            public void onMessageReceived(PeerHandle peerHandle, byte[] message) {
                super.onMessageReceived(peerHandle, message);
                Log.d(LOG, "subscribe, received message");
                if(message.length == 2) {
                    portToUse = byteToPortInt(message);
                    Log.d(LOG, "subscribe, will use port number "+ portToUse);
                } else if (message.length == 6){
                    setOtherMacAddress(message);
                } else if (message.length == 16) {
                    setOtherIPAddress(message);
                } else if (message.length > 16) {
                    String[] messageIn = new String(message).split(".");
                    if(messageIn[0].equals("subscriber") && messageIn[1].equals("establishingRole")){
                        isPublisher = false;
                        hasEstablishedPublisherAndSubscriber = true;
                    }
                }
            }
        }, null);
    }


    *//*

*/
/**
     * Helper to set the status field.
     *//*
*/
/*

    private void setMessage(byte[] msg) {
        String outmsg = new String(msg).replace("messageToBeSent: ","");
    }

    private void setMacAddress(byte[] mac) {
        myMac = mac;
        String macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
    }

    private void setOtherMacAddress(byte[] mac) {
        //otherMac = mac;
        macAddress = String.format("%02x:%02x:%02x:%02x:%02x:%02x", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
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

}


class AsyncTaskRunner extends AsyncTask<String, String, String> {

    private String resp;

    @Override
    protected String doInBackground(String... params) {
        return resp;
    }


    @Override
    protected void onPostExecute(String result) {
        Log.d("RESULTAT", result);
    }


    @Override
    protected void onProgressUpdate(String... text) {
        Log.d("RESULTAT", String.valueOf(text));

    }
}*//*



*/
