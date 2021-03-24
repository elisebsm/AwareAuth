package com.example.testaware.models;

import java.io.Serializable;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import lombok.Getter;

public class Contact implements Serializable {

    @Getter
    private X509Certificate certificate;

    public Contact(X509Certificate certificate){
        this.certificate = certificate;
    }

    public String getCommonName(){
        X509Certificate x509Certificate = certificate;
        X500Principal x500Principal = x509Certificate.getIssuerX500Principal();
        String [] split = x500Principal.getName().split(",");
        for (String string : split){
            if (string.contains("CN=")){
                return string.trim().substring(3);
            }
        }
        return "User unknown";
    }
}