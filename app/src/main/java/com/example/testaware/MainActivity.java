package com.example.testaware;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
import android.net.wifi.aware.PeerHandle;
import android.net.wifi.aware.PublishConfig;
import android.net.wifi.aware.PublishDiscoverySession;
import android.net.wifi.aware.SubscribeConfig;
import android.net.wifi.aware.SubscribeDiscoverySession;
import android.net.wifi.aware.WifiAwareManager;
import android.net.wifi.aware.WifiAwareNetworkSpecifier;
import android.net.wifi.aware.WifiAwareSession;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;


public class MainActivity extends AppCompatActivity{

    private boolean supportsAware;
    private String LOG = "LOG-Test-Aware";
    private SubscribeDiscoverySession mainSession;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Messages for whether or not device has WiFi Aware
        Toast hasAware = Toast.makeText(this, "WiFi Aware is available", Toast.LENGTH_LONG);
        Toast noAware = Toast.makeText(this, "WiFi Aware is not available", Toast.LENGTH_LONG);

        supportsAware = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE);
        if(supportsAware){
            hasAware.show();
            Log.i(LOG, "The device supports Wi-Fi Aware");
        } else {
            noAware.show();
            Log.i(LOG, "The device DOES NOT supports Wi-Fi Aware");
        }


        final WifiAwareManager wifiAwareManager = (WifiAwareManager) getSystemService(Context.WIFI_AWARE_SERVICE);

        // Messages for whether or not device has WiFi Aware
        final Toast awareSupported = Toast.makeText(this, "WiFi Aware Supported", Toast.LENGTH_LONG);
        final Toast awareUnsupported = Toast.makeText(this, "WiFi Aware Unsupported", Toast.LENGTH_LONG);
        IntentFilter filter =
                new IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED);
        BroadcastReceiver myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // discard current sessions
                if (wifiAwareManager.isAvailable()) {
                    awareSupported.show();
                    Log.i(LOG, "Wi-Fi Aware is available");

                } else {
                    awareUnsupported.show();
                    Log.i(LOG, "Wi-Fi Aware is NOT available. Do you have  Wi-Fi or Location OFF?");
                }
            }
        };
        this.registerReceiver(myReceiver, filter);

        AttachCallback attachCallback = new AttachCallback();
        Handler handler = new Handler();
        wifiAwareManager.attach(attachCallback, handler);

        publish();
        subscribe();

    }
    //testing by having subscriber subcribing to published service "receive_message_service" and sending message to
    //the publisher

    //make a service discoverable py callin publish()
    private void publish (){
        //specify name of service and optional match filters
        PublishConfig config = new PublishConfig.Builder()
                .setServiceName("Receive_Message_Service")
                .build();

        //specify actions when events occur, such when the subscriber receives a message
        mAwaresession.publish(config, new DiscoverySessionCallback() {
            @Override
            public void onPublishStarted(PublishDiscoverySession session) {
                Log.i(LOG, "publish started");

            }

            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onMessageReceived(PeerHandle peerHandle, byte[] message) {
                ServerSocket ss = null;
                try {
                    ss = new ServerSocket(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int port = ss.getLocalPort();

                //use session (discovery session) and peerhandle obtained from message from subscriber
                NetworkSpecifier networkSpecifier = new WifiAwareNetworkSpecifier.Builder(mainSession, peerHandle)
                        .setPskPassphrase("somePassword")
                        .setPort(port)
                        .build();
                NetworkRequest myNetworkRequest = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
                        .setNetworkSpecifier(networkSpecifier)
                        .build();
                //use connectivityManager to request wifi aware network on the publisher
                ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {

                    }

                    @Override
                    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {

                    }

                    @Override
                    public void onLost(Network network) {

                    }
                };

                ConnectivityManager connectivityManager = new ConnectivityManager();

                ConnectivityManager connMgr.requestNetwork(myNetworkRequest, callback);

            }
        }, null);

    }

    //subsribe() method is used to subscribe to a service
    private void subscribe(){
        //specify name of the service to subscribe to
        SubscribeConfig config = new SubscribeConfig.Builder()
                .setServiceName("Receive_Message_Service")
                .build();
        //DiscoverySessionCallback is specified when events occur, for example when a publisher is discovered
        //can also use this method to communicate with publisher
        mAwaresession.subscribe(config, new DiscoverySessionCallback() {
            //Need discoverySession in order to send message
            @Override
            public void onSubscribeStarted(SubscribeDiscoverySession session) {
                mainSession = session;

            }
            //called when matching publishers come into wifi range
            @Override
            public void onServiceDiscovered(PeerHandle peerHandle,
                                            byte[] serviceSpecificInfo, List<byte[]> matchFilter) {
                String message = "Hei!";
                byte[] messageByte = Base64.encode(message.getBytes(), Base64.DEFAULT);
                mainSession.sendMessage(peerHandle, 1, messageByte);
                //request wifi aware network on subscriber, same as with publisher, just without specifying port.

            }
        }, null);

    }

    private WifiAwareSession mAwaresession;
    /*public void onAttached(final WifiAwareSession session) {
        Log.d("Method called", "Attach operation completed and can now start discovery sessions");
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
                messageReceived = messageFromPub;
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