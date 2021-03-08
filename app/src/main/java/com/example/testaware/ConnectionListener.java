package com.example.testaware;

import com.example.testaware.models.Contact;
import com.example.testaware.models.ReceivedPacket;

public interface ConnectionListener {
    void onConnect();
    void onDisconnect();
    void onReceivedPacket(Contact contact, ReceivedPacket packet);
    void onServerPacket(ReceivedPacket packet);
}
