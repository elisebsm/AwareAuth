package com.example.testaware.listitems;

import lombok.Getter;
import lombok.Setter;

public class ChatListItem {
    private String username;
    @Getter
    private String peerIpv6;
    @Getter
    @Setter
    private String status;

    @Setter
    @Getter
    private String cert;

    public ChatListItem(String username, String peerIpv6, String status, String cert){
        this.username = username;
        this.peerIpv6 = peerIpv6;
        this.status = status;
        this.cert = cert;
    }

    public String getUsername(){
        return username;
    }

}
