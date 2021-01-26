package com.example.testaware;

import java.net.Inet6Address;
import java.net.InetAddress;

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


}

