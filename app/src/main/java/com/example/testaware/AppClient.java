package com.example.testaware;


import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.testaware.activities.MainActivity;

//import com.example.testaware.activities.TestChatActivity;

import com.example.testaware.listeners.SSLContextedObserver;
import com.example.testaware.models.AbstractPacket;
import com.example.testaware.models.Contact;
import com.example.testaware.models.Message;
import com.example.testaware.models.MessagePacket;
import com.example.testaware.offlineAuth.Decoder;
import com.example.testaware.offlineAuth.PeerSigner;
import com.example.testaware.offlineAuth.VerifyUser;

import java.io.BufferedInputStream;
import com.example.testaware.activities.TestChatActivity;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.lang.ref.WeakReference;
import java.net.Inet6Address;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.security.auth.x500.X500Principal;

import lombok.Getter;

import static java.lang.System.currentTimeMillis;

public class AppClient implements Runnable{

    private boolean running;
    @Getter
    private SSLSocket sslSocket;
    private SSLContext sslContext;

    private boolean certSelfSigned;

    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private ExecutorService sendService = Executors.newSingleThreadExecutor();

    private boolean userCertificateCorrect =true;

 //   @Getter
   // private List<ConnectionListener> connectionListeners;


    private String LOG = "Log-Client";
    @Getter
    private KeyPair keyPair;

    private Inet6Address inet6Address;

    private int port;
    private long clientStartedTime;
    public int counterValue;
    /*@Getter
    private static WeakReference<TestChatActivity> testChatActivity;
    private long clientStartedTime;
    public int counterValue;

    public static void updateTestChatActivity(TestChatActivity activity) {
          testChatActivity = new WeakReference<>(activity);
        }
*/
    public AppClient(KeyPair keyPair, SSLContext sslContext, int port, long clientStartedTime, int counterValue){
        this.keyPair = keyPair;
        this.sslContext = sslContext;
        this.inet6Address = MainActivity.getPeerIpv6();
        this.port = port;
        this.clientStartedTime = clientStartedTime;
        this.counterValue = counterValue;
        Log.d(LOG, "port: " + port);

        //Thread thread = new Thread(this);
        //thread.start();

       // connectionListeners = new ArrayList<>();

    }


    private X509Certificate getServerIdentity() {

        try {
            Certificate[] certs = sslSocket.getSession().getPeerCertificates();
            if(certs.length > 0 && certs[0] instanceof X509Certificate) {
                return (X509Certificate) certs[0];
            }

        } catch (SSLPeerUnverifiedException ignored)       {
            userCertificateCorrect =false;
            Log.d(LOG, "Cert not valid");


        }     catch(NullPointerException ignored){
                ignored.printStackTrace();
        }

        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean sendMessage(String message, long sendingMessageTime){
        if(outputStream == null){
            Log.d(LOG, "outputstream is null");
            return false;
        }
        Runnable sendMessageRunnable = () -> {
            try {
                //MessagePacket messagePacket = (new MessagePacket(message));
                Log.d(LOG, "outputstream " + message);
                outputStream.writeUTF(message);
                outputStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
                //if (e.ge)
                Log.d(LOG, "Exception in Appclient  in sendMessage()");
                running = false;
            }
        };
        sendService.submit(sendMessageRunnable);
        BufferedWriter writer = null;
        try {
            String outputText = String.valueOf(sendingMessageTime);
            writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/messageSentClient", true));
            writer.append("Counter:" + counterValue);
            writer.append("\n");
            writer.append(outputText);
            writer.append("\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


    private void onPacket(AbstractPacket packet) {
        Contact from = new Contact(getServerIdentity());
        Log.d(LOG, packet.getClass().getSimpleName() + " from " + from.getCommonName());

    }

    private String [] protocolGCM;
    private String [] protocolCHACHA;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void run() {
        certSelfSigned(IdentityHandler.getCertificate());
        running = true;
        sslSocket = null;
        String [] tlsVersion = new String[1];
        tlsVersion [0] = "TLSv1.2";

        protocolGCM = new String[1];
        protocolGCM [0]= Constants.SUPPORTED_CIPHER_GCM;

        protocolCHACHA = new String[1];
        protocolCHACHA [0]= Constants.SUPPORTED_CIPHER_CHACHA;

        //this.port = Constants.SERVER_PORT;

        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        try {
            while(running){
                if(certSelfSigned){
                    sslSocket = (SSLSocket) socketFactory.createSocket(inet6Address, Constants.SERVER_PORT_NO_AUTH);
                    Log.d(LOG, "Connecting to NO_AUTH_SERVER PORT");
                }
                else{
                    sslSocket = (SSLSocket) socketFactory.createSocket(inet6Address, port);
                }
                Log.d(LOG, "port: " + port);

                //sslSocket.setEnabledProtocols(tlsVersion);
                sslSocket.setEnabledCipherSuites(protocolCHACHA);

                sslSocket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
                    @Override
                    public void handshakeCompleted(HandshakeCompletedEvent event) {

                        long handshakeCompletedClient = currentTimeMillis();
                        //Log.d("TESTING-LOG-TIME-TLS-HANDSHAKE-COMPLETED-CLIENT",  String.valueOf(handshakeCompletedClient));

                        BufferedWriter writer = null;
                        try {
                            String outputText = String.valueOf(handshakeCompletedClient);
                            writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/handshakeCompletedServer", true));
                            writer.append("Counter:" + counterValue);
                            writer.append("\n");
                            writer.append(outputText);
                            writer.append("\n");
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(event.getSession().isValid()){
                            Log.d(LOG, "Handshake completed");
                            X509Certificate peerCert = getServerIdentity();
                            if(userCertificateCorrect && !certSelfSigned && peerCert != null) {
                                addPeerAuthInfo(peerCert);
                            }
                        }
                        else{
                            Log.d(LOG, "Handshake failed");
                        }

                    }
                });

                outputStream = new DataOutputStream(sslSocket.getOutputStream());
                inputStream = new DataInputStream (sslSocket.getInputStream()); //FEIL: .StreamCorruptedException: invalid stream header
                // SSLException: Read error: ssl=0x7c46091508: I/O error during system call, Software caused connection abort
                //outputStream.writeU("clientHello");
                outputStream.flush();

                BufferedWriter writer = null;
                try {
                    String outputText = String.valueOf(clientStartedTime);
                    writer = new BufferedWriter(new FileWriter("/data/data/com.example.testaware/clientStarted", true));
                    writer.append("Counter:" + counterValue);
                    writer.append("\n");
                    writer.append(outputText);
                    writer.append("\n");
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while(running){
                    if (inputStream != null){
                        String message =  inputStream.readUTF();
                        long readinMessageAtClient = currentTimeMillis();
                        Log.d("TESTING-LOG-TIME-TLS-MESSAGE-INPUTSTREAM-CLIENT",  String.valueOf(readinMessageAtClient));
                     //MessagePacket messagePacket = (MessagePacket) abstractPacket;
                     //Message message = messagePacket.getMessage() ;
                        new Handler(Looper.getMainLooper()).post(()-> {
                            TestChatActivity.setChat(message, counterValue);

                        });
                    }
                }
            }
        } catch (IOException  e) {
            e.printStackTrace();
            Log.d(LOG, "Exception in Appclient  in run()");


            if(sslSocket != null){
                try {
                    sslSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private X509Certificate getPeerIdentity()  {
        Certificate[] certificates = new Certificate[0];
        try {
            certificates = sslSocket.getSession().getPeerCertificates();
        } catch (SSLPeerUnverifiedException e) {
            e.printStackTrace();
        }
        if (certificates.length > 0 && certificates[0] instanceof X509Certificate){
            return (X509Certificate) certificates[0];
        }
        return null;
    }

    private void addPeerAuthInfo(X509Certificate peerCert){
        PublicKey peerPubKey = peerCert.getPublicKey();
        VerifyUser.setValidatedAuthenticator(peerPubKey);
        ArrayList<String> listOfSingedStrings = PeerSigner.getTmpPeerAuthInfo(true);
       // ArrayList<String> listOfTrustedAuthenticators = PeerSigner.getTmpPeerAuthInfo(false);
        if (listOfSingedStrings != null) {
            for (int i = 0; i < listOfSingedStrings.size(); i++) {
                PeerSigner.saveSignedKeyToFile(listOfSingedStrings.get(i));
            }
        }
       /* if(listOfTrustedAuthenticators != null){
            for (int i = 0; i < listOfTrustedAuthenticators.size(); i++) {
                PublicKey pubKeyDecoded = Decoder.getPubKeyGenerated(listOfTrustedAuthenticators.get(i));
                VerifyUser.setValidatedAuthenticator(pubKeyDecoded);
            }
            PeerSigner.deleteTmpFile();
        }
        */

        else{

            Log.d(LOG, "PeerCert is null");
        }
        PeerSigner.deleteTmpFile();

    }

    public boolean certSelfSigned(X509Certificate cert){
        certSelfSigned=false;
        X500Principal subject = cert.getSubjectX500Principal();
        X500Principal issuer = cert.getIssuerX500Principal();
        if(subject.equals(issuer)) {
            certSelfSigned= true;
        }
        return certSelfSigned;
    }


}


/*


      boolean send(AbstractPacket packet) {
        if (outputStream == null) return false;
        Runnable runnable = () -> {
            try {
                outputStream.writeObject(packet);
                outputStream.flush();
            } catch (IOException e) {
                Log.e(LOG, e.getMessage());
                running = false;
            }
        };

        sendService.submit(runnable);
        return true;
    }*/


   /* @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean sendMessage(Message message){
        if(outputStream == null){
            Log.d(LOG, "outputstream is null");
            return false;
        }
        Runnable sendMessageRunnable = () -> {
            try {
                Log.d(LOG, "outputstream " + message);
                outputStream.writeObject(message);
                outputStream.flush();

                *//*MessageListItem chatMsg = new MessageListItem(message, ChatActivity.getLocalIp()); //TODO
                ChatActivity.messageList.add(chatMsg);

                //EditText textT = (EditText) findViewById(R.id.eTChatMsg);
                //textT.getText().clear();*//*
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(LOG, "Exception in Appclient  in sendMessage()");
                running = false;
            }
        };
        sendService.submit(sendMessageRunnable);
        return true;
    }*/




