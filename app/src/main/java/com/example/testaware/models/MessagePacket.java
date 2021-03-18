package com.example.testaware.models;

import lombok.Getter;

public class MessagePacket extends AbstractPacket {
    private static final long serialVersionUID = 2545855896325861508L;

    @Getter
    Message message;

    public MessagePacket(Message message) {
        this.message = message;
    }
}
