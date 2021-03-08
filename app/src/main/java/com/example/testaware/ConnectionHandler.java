package com.example.testaware;

import android.content.Context;
import android.net.wifi.aware.WifiAwareManager;

import com.example.testaware.models.Contact;
import com.example.testaware.models.Message;
import com.example.testaware.models.MessageObject;
import com.example.testaware.models.ReceivedPacket;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;

public class ConnectionHandler implements ConnectionListener{
    @Getter
    private AppClient appClient;
    @Getter
    private AppServer appServer;

    private Map<PublicKey, List<Message>> messages;

    private WifiAwareConnectionHandler wifiAwareConnectionHandler;

    private Context mainActivityApplicationContext;


    public ConnectionHandler(Context context){
        this.mainActivityApplicationContext = context;
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onReceivedPacket(Contact contact, ReceivedPacket packet) {
        if(packet instanceof MessageObject){
            Message message = ((MessageObject) packet).getMessage();
            if(!messages.containsKey(message.getFrom())){
                messages.put(message.getFrom(), new ArrayList<>());
            }
            messages.get(message.getFrom()).add(message);
        }
    }

    private void initWifiAware(){
        wifiAwareConnectionHandler = new WifiAwareConnectionHandler(mainActivityApplicationContext);




    }

    @Override
    public void onServerPacket(ReceivedPacket packet) {

    }
}
