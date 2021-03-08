package com.example.testaware.models;

import lombok.Getter;

public class MessageObject extends ReceivedPacket {

    @Getter
    Message message;
    public MessageObject(Message message){
        this.message = message;
    }
}
