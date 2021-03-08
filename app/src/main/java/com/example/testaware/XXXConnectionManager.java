package com.example.testaware;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.aware.DiscoverySessionCallback;
import android.net.wifi.aware.PeerHandle;
import android.net.wifi.aware.PublishConfig;
import android.net.wifi.aware.PublishDiscoverySession;
import android.net.wifi.aware.SubscribeDiscoverySession;
import android.net.wifi.aware.WifiAwareManager;
import android.net.wifi.aware.WifiAwareSession;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.testaware.activities.MainActivity;


public class XXXConnectionManager {
    private String LOG = "WiFiAwareLOG-ConnectoinManager";
    private WifiAwareManager wifiAwareManager;
    private WifiAwareSession wifiAwareSession;
    private PublishDiscoverySession   publishDiscoverySession;
    private SubscribeDiscoverySession subscribeDiscoverySession;

    private Context context;

    public XXXConnectionManager(Context context){
        this.context = context;
    }


    private void publish () {
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
                super.onMessageReceived(peerHandle_, message);
                if(message.length == 2) {
                    //portToUse = byteToPortInt(message);
                    Log.d(LOG, "will use port number "+ Constants.SERVER_PORT);
                } else if (message.length == 6){
   //                 setOtherMacAddress(message);
                } else if (message.length == 16) {
   //                 setOtherIPAddress(message);
                } else if (message.length > 16) {
   //                 setMessage(message);
                }
   //             peerHandle = peerHandle_;
   //             if (!otherMacList.contains(macAddress)){
   //                 otherMacList.add(macAddress);
   //                 Log.d(LOG, "mac list"+ otherMacList);

 //                   peerHandleList.add(peerHandle);
  //                  int numOfSubscribers = peerHandleList.size();   //number of subscribers
   //                 Toast.makeText(MainActivity.this, "Subscribers: "+ numOfSubscribers, Toast.LENGTH_LONG).show();
                }
 //           }
        }, null);
    }
}
