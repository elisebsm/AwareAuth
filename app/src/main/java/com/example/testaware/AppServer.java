package com.example.testaware;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;

import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.testaware.activities.MainActivity;

import com.example.testaware.listeners.MessageReceivedObserver;
import com.example.testaware.listeners.OnMessageReceivedListener;
import com.example.testaware.listeners.OnSSLContextChangedListener;
import com.example.testaware.listeners.SSLContextedObserver;
import com.example.testaware.listitems.MessageListItem;
import com.example.testaware.models.Message;
import com.example.testaware.models.MessagePacket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import lombok.Getter;

import static java.lang.System.currentTimeMillis;


//client can also instantiate connection.
//implements runnable in order to be extecuted by a thread. must implement run(). Intended for objects that need to execute code while they are active.
public class AppServer {

    private final String LOG = "Log-App-Server";
    //private ObjectInputStream inputStream;
    //private ObjectOutputStream outputStream;   //TODO: use so client can also send messages

    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private boolean running;

    private final String [] tlsVersion;



    @Getter
    private static WeakReference<MainActivity> mainActivity;

    public static void updateActivity(MainActivity activity) {
        mainActivity = new WeakReference<>(activity);
    }

    private ClientHandler client;

    @Getter
    private SSLServerSocket serverSocket;

    @Getter
    private int localPort;

    private int counterValue = 0;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public AppServer(SSLContext serverSSLContext, Network network){
    //public AppServer(SSLContext serverSSLContext){
        running = true;
        String[] protocolGCM = new String[1];
        protocolGCM[0]= Constants.SUPPORTED_CIPHER_GCM;

        String[] protocolCHACHA = new String[1];
        protocolCHACHA[0]= Constants.SUPPORTED_CIPHER_CHACHA;

        tlsVersion = new String[1];
        tlsVersion [0] = "TLSv1.2";


        counterValue = mainActivity.get().getCountervalue();


        Runnable serverTask = () -> {
            running  = true;
            try {
                //20.05            //serverSocket = (SSLServerSocket) serverSSLContext.getServerSocketFactory().createServerSocket(0  );
                serverSocket = (SSLServerSocket) serverSSLContext.getServerSocketFactory().createServerSocket(1025);
                //20.05            //localPort = serverSocket.getLocalPort();
     //20.05           mainActivity.get().setServerPort(network, "server", localPort);


                serverSocket.setEnabledProtocols(tlsVersion);
                Log.d(LOG, "Port: "+ localPort);

               /* serverSocket = (SSLServerSocket) serverSSLContext.getServerSocketFactory().createServerSocket(1025  );
                //localPort = serverSocket.getLocalPort();
                serverSocket.setEnabledProtocols(tlsVersion);*/

                serverSocket.setEnabledCipherSuites(protocolCHACHA);
                serverSocket.setNeedClientAuth(true);


               while (running) {
                    SSLSocket sslClientSocket = (SSLSocket) serverSocket.accept();
                    serverSocket.getEnabledCipherSuites();
                    sslClientSocket.getPort();

                    sslClientSocket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
                        @Override
                        public void handshakeCompleted(HandshakeCompletedEvent event) {
                            long handshakeCompletedServer = currentTimeMillis();
                            //Log.d("TESTING-LOG-TIME-TLS-HANDSHAKE-COMPLETED-SERVER",  String.valueOf(handshakeCompletedServer));
                            BufferedWriter writer = null;
                            try {
                                String outputText = String.valueOf(handshakeCompletedServer);
                                writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/handshakeCompletedServer", true));
                                writer.append("Counter:" + counterValue);
                                writer.append("\n");
                                writer.append(outputText);
                                writer.append("\n");
                                writer.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });


                    Log.d(LOG, "client accepted");

                    inputStream = new DataInputStream(sslClientSocket.getInputStream());
                    outputStream = new DataOutputStream(sslClientSocket.getOutputStream());

                    client = new ClientHandler(inputStream, outputStream , sslClientSocket, counterValue );
                    Thread t = new Thread(client);
                    t.start();
                    Log.d(LOG, "Starting new Thread -");
                    outputStream.flush();


                  /*  while(running){
                        if (inputStream != null){

                            //AbstractPacket abstractPacket = (AbstractPacket) inputStream.readObject();
                            //onPacketReceived(abstractPacket);

                            String strMessageFromClient = String.valueOf(inputStream.readObject());  //FEIL
                            Log.d(LOG, "Reading message " + strMessageFromClient);

                            MessageListItem chatMsg = new MessageListItem(strMessageFromClient, "ipv6_other_user");
                           // MessageListItem chatMsg = new MessageListItem(strMessageFromClient, "ipv6_other_user");    //TODO: GET USERNAME FROM CHATLISTITEM
                        }
                    }*/
                }
            }  catch (IOException  e) {
               Log.d(LOG, "Printing message: " +  Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
                Log.d(LOG, "Exception in AppServer in constructor");
            }
            //TODO: close socket
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }


    public void stop(){
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(client!=null){
            client.setRunning(false);
        }
    }


    public void sendMessage(String message, long sendingMessageTime){
        if(client != null){
            client.sendMessage(message, sendingMessageTime);
        } else {
            Log.d(LOG, "Client is null");
        }
    }
}


