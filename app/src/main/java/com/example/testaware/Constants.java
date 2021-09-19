package com.example.testaware;

public class Constants {
    public static final int SERVER_PORT = 1026;
    public static final String SERVICE_NAME = "wifiaware";
    public static final int SERVER_PORT_NO_AUTH = 1029;
    public static final String ENCRYPTION_ALGORITHM_AES = "AES/GCM/NoPadding"; // ChaCha20/Poly1305/NoPadding OR  AES/GCM/NoPadding
    public static final String KEYSTORE_PATH = "/data/data/com.example.testaware/keystore.jks";

    public static final String SUPPORTED_CIPHER_CHACHA = "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256";
    public static final String SUPPORTED_CIPHER_GCM = "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256";

}
