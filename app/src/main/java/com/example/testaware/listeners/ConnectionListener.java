package com.example.testaware.listeners;


import com.example.testaware.models.Contact;
import com.example.testaware.models.AbstractPacket;

public interface ConnectionListener {
    void onConnect();
    void onDisconnect();
    void onPacket(Contact contact, AbstractPacket packet);
    void onServerPacket(AbstractPacket packet);
}
