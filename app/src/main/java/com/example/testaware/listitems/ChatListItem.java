package com.example.testaware.listitems;

import java.net.Inet6Address;

public class ChatListItem {
    private String username;
    private String peerIpv6;

    public ChatListItem(String username, String peerIpv6){
        this.username = username;
        this.peerIpv6 = peerIpv6;
    }

    public String getUsername(){
        return username;
    }

    public String getPeerIpv6(){
        return peerIpv6;
    }
}
