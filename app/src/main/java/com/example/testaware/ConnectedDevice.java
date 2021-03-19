package com.example.testaware;

import android.util.Log;

import com.example.testaware.models.AbstractPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;

public class ConnectedDevice implements Runnable{

    private SSLSocket sslClientSocket;
    private AppServer appServer;

    private boolean running;

    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    private String LOG = "LOG-Test-Aware-ConnectedDevice";

    public ConnectedDevice(AppServer appServer, SSLSocket sslClientSocket) {
        this.sslClientSocket = sslClientSocket;
        this.appServer = appServer;
        //Thread thread = new Thread(this);
        //thread.start();
    }

    public void stop() {
        running = false;
    }


    public X509Certificate getUserIdentity() {
        try {
            Certificate[] certs = sslClientSocket.getSession().getPeerCertificates(); // liste med sertifikater
            if(certs.length > 0 && certs[0] instanceof X509Certificate) {    // liste med setifikate til clienten i gjeldene session
                return (X509Certificate) certs[0];
            }
        } catch (SSLPeerUnverifiedException | NullPointerException ignored) {
            ignored.printStackTrace();
        }
        return null;
    }


    public void send(AbstractPacket packet) {
        try {
            outputStream.writeObject(packet);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            running = false;
        }
    }

    @Override
    public void run() {

    }
/*
    @Override
    public void run() {
        running = true;

        Log.d(LOG, sslClientSocket.getInetAddress().getHostAddress() + " connected");

        try {
            outputStream = new ObjectOutputStream(sslClientSocket.getOutputStream());
            inputStream = new ObjectInputStream(sslClientSocket.getInputStream());

            while(running){
                AbstractPacket receivedPacket = (AbstractPacket) inputStream.readObject();
                appServer.onPacketReceived(this, receivedPacket);
            }
            appServer.removeClient(this);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Log.e(LOG, e.getMessage());
            running = false;
        }
    }
    */


}
