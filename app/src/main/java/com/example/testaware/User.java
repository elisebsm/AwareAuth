package com.example.testaware;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

public class User {
    String name;
    String ipv6Address;
    Boolean isSender;
    //mac etc

    //class not in use yet
    public User(String name, String ipv6Address,Boolean isSender){
        this.name = name;

    }
    public String getName(){
        return name;
    }
    public String getIpv6Address(){
        return ipv6Address;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setIpv6Address(String ipv6Address){
        this.ipv6Address = ipv6Address;

    }

    public void setIsSender(Boolean isSender){
        this.isSender = isSender;
    }
    public Boolean getIsSender(){
        return isSender;
    }


    //Fra øystein
   /* private X509Certificate certificate;

    public Contact (X509Certificate certificate){
        this.certificate = certificate;
    }

    public String getUserName(){
        X509Certificate x509Certificate = certificate;
        X500Principal x500Principal = x509Certificate.getIssuerX500Principal();
        String [] split = x500Principal.getName().split(",");
        for (String string : split){
            if (string.contains("CN=")){
                return string.trim().substring(3);
            }
        }
        return "User unknown";
    }*/


}

