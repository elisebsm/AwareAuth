package com.example.testaware.listeners;

import com.example.testaware.models.Message;


public class MessageReceivedObserver {

    private OnMessageReceivedListener listener;

    private Message message;

    public void setListener(OnMessageReceivedListener listener) {
        this.listener = listener;
    }

    public Message getMessage(){
        return message;
    }


    public void setMessage(Message message){
        this.message = message;
        if(listener != null){
            listener.onMessagReceived(message);
        }
    }
}
