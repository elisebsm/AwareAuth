package com.example.testaware.listeners;

import javax.net.ssl.SSLContext;

public interface OnSSLContextChangedListener {
    void onSSLContextChanged(SSLContext sslContext);

}
