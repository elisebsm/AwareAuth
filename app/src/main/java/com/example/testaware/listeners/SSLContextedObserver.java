package com.example.testaware.listeners;

import javax.net.ssl.SSLContext;

public class SSLContextedObserver {

    private OnSSLContextChangedListener listener;

    private SSLContext sslContext;

    public void setListener(OnSSLContextChangedListener listener) {
        this.listener = listener;
    }

    public SSLContext getSslContext(){
        return sslContext;
    }


    public void setSslContext(SSLContext sslContext){
        this.sslContext = sslContext;
        if(listener != null){
            listener.onSSLContextChanged(sslContext);
        }
    }
}
