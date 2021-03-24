package com.example.testaware.listeners;

import android.net.ConnectivityManager;

import javax.net.ssl.SSLContext;

public class SSLContextedObserver {

    private OnSSLContextChangedListener listener;

    private SSLContext sslContext;
    private ConnectivityManager connectivityManager;

    public void setListener(OnSSLContextChangedListener listener) {
        this.listener = listener;
    }

    public SSLContext getSslContext(){
        return sslContext;
    }

    public ConnectivityManager getConnectivityManager(){
        return connectivityManager;
    }

    public void setSslContext(SSLContext sslContext){
        this.sslContext = sslContext;
        if(listener != null){
            listener.onSSLContextChanged(sslContext);
        }
    }
}
