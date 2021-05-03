package com.example.testaware.listitems;

import java.net.Inet6Address;

import lombok.Getter;

public class ChatListItem {
    private String username;
    @Getter
    private String peerIpv6;

    public ChatListItem(String username, String peerIpv6){
        this.username = username;
        this.peerIpv6 = peerIpv6;
    }

    public String getUsername(){
        return username;
    }

  /*  public String getPeerIpv6(){
        return peerIpv6;
    }*/
}
