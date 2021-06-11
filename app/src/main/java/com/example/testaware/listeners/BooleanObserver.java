package com.example.testaware.listeners;


public class BooleanObserver {


    private BooleanChangedListener listener;

    public void setListener(BooleanChangedListener listener) {
        this.listener = listener;
    }

    public void setMessageSentStatus(boolean messageSentStatus){
        if(listener != null){
            listener.onBooleanChanged(messageSentStatus);
        }
    }


}
