package com.example.testaware;

public class Constants {
    public static final int SERVER_PORT = 1026;
    public static final String SERVICE_NAME = "wifiawareEliseLenovo";
    public static final String ENCRYPTION_ALGORITHM_AES = "AES/GCM/NoPadding"; // ChaCha20/Poly1305/NoPadding OR  AES/GCM/NoPadding
    //AES/CBC/PKCS5PADDING
    public static final String KEYSTORE_PATH = "/data/data/com.example.testaware/keystore.jks";

    public static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";  //SHA256withECDSA

    public static final String SUPPORTED_CIPHER_CHACHA = "TLS_CHACHA20_POLY1305_SHA256";
    public static final String SUPPORTED_CIPHER_GCM = "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256";

}
/* Fra øystein:
public static final String KEY_PAIR_TYPE = "RSA";
    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    public static final String ENCRYPTION_ALGORITHM = "RSA/ECB/OAEPPadding";
    public static final String MESSAGE_CHARSET = "UTF-8";
    public static final int KEY_SIZE = 4096;
 */
