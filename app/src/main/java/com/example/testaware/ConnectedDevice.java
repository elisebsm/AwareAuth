package com.example.testaware;

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

    public ConnectedDevice(AppServer appServer, SSLSocket sslClientSocket) {
        this.sslClientSocket = sslClientSocket;
        this.appServer = appServer;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        running = true;

        try {
            outputStream = new ObjectOutputStream(sslClientSocket.getOutputStream());
            inputStream = new ObjectInputStream(sslClientSocket.getInputStream());

           /* while(running){
                ReceivedPacket receivedPacket = (ReceivedPacket) inputStream.readObject();
                appServer.onPacketReceived(this, receivedPacket);
            }*/
            appServer.removeClient(this);
        } catch (IOException e) {
            e.printStackTrace();
            running = false;
        }
    }


    protected X509Certificate getUserIdentity() {
        try {
            Certificate[] certs = sslClientSocket.getSession().getPeerCertificates();
            if(certs.length > 0 && certs[0] instanceof X509Certificate) {
                return (X509Certificate) certs[0];
            }
        } catch (SSLPeerUnverifiedException | NullPointerException ignored) {

        }
        return null;
    }


    protected void stop(){
        running = false;
    }


    public void sendMessage() {

    }
}
