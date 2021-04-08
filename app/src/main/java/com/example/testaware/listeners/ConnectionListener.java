package com.example.testaware.listeners;


import com.example.testaware.models.Contact;
import com.example.testaware.models.AbstractPacket;
import com.example.testaware.models.Message;

public interface ConnectionListener {
    void onConnect();
    void onDisconnect();
    //void onPacket(Contact contact, AbstractPacket packet);
    void onPacket(Message message);
    void onServerPacket(AbstractPacket packet);
}
