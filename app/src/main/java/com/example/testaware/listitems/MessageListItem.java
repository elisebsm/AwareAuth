package com.example.testaware.listitems;

public class MessageListItem {
    String message;

    String ipv6Address;

    public MessageListItem(String message, String ipv6Address) {
        this.message = message;
        this.ipv6Address= ipv6Address;

    }

    public String getMessage() {
      return message;

    }


    public String getIpv6Address(){
        return ipv6Address;
    }

    public void setIpv6Address(String ipv6Address){
        this.ipv6Address = ipv6Address;

    }


}






