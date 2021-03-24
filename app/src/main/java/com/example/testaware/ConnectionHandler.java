package com.example.testaware;

import android.content.Context;
import android.util.Log;

import com.example.testaware.activities.MainActivity;
import com.example.testaware.listeners.ConnectionListener;
import com.example.testaware.models.AbstractPacket;
import com.example.testaware.models.Contact;
import com.example.testaware.models.Message;
import com.example.testaware.models.MessagePacket;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.net.ssl.SSLContext;

import lombok.Getter;

public class ConnectionHandler implements ConnectionListener{

    private String LOG = "LOG-Test-Aware-Connection-Handler";

    private Context context;
    private SSLContext sslContext;
    private KeyPair keyPair;

    @Getter
    private AppServer appServer;

    @Getter
    private AppClient appClient;

   // private WiFiAwareConnectionManager wiFiDirectConnectionManager;

    private Map<PublicKey, List<Message>> messages;

    private ArrayList<AbstractPacket> packets;

    public ConnectionHandler (Context context, SSLContext sslContext, KeyPair keyPair){
        this.context = context;
        this.sslContext = IdentityHandler.getSSLContext(context);
        this.keyPair = IdentityHandler.getKeyPair();
        this.messages = new HashMap<>();
        this.packets = new ArrayList<>();

        //appClient = new AppClient(keyPair, sslContext);
        //appClient.registerConnectionListener(this);

        Thread thread = new Thread(appServer);
        thread.start();
         //TODO initialize wifi aware here?
    }

    public void setAppServer(AppServer appServer){
        this.appServer = appServer;
    }

    public void setAppClient(AppClient appClient){
        this.appClient = appClient;
        Thread thread = new Thread(appClient);
        thread.start();
    }

    public void registerConnectionListener(ConnectionListener listener) {
        appClient.registerConnectionListener(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        appClient.removeConnectionListener(listener);
    }


    public void sendMessage(Message message) {
        if(appClient == null) {
            Log.d(LOG, "Failed to send message, applicationClient == null");
            return;
        }
        if(!messages.containsKey(message.getTo())) {
            messages.put(message.getTo(), new ArrayList<>());
        }
        messages.get(message.getTo()).add(message);
        appClient.sendMessage(message);
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onPacket(Contact contact, AbstractPacket packet) {
        if( packet instanceof MessagePacket) {
            Message message = ((MessagePacket) packet).getMessage();
            if(!messages.containsKey(message.getFrom())) {
                messages.put(message.getFrom(), new ArrayList<>());
            }
            messages.get(message.getFrom()).add(message);
        }
    }

    @Override
    public void onServerPacket(AbstractPacket packet) {

    }

    public List<Message> getMessagesFromm(PublicKey publicKey) {
        if(!messages.containsKey(publicKey)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Objects.requireNonNull(messages.get(publicKey)));
    }
}
