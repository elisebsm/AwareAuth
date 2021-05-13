package com.example.testaware.listeners;

import android.net.ConnectivityManager;

public class BooleanObserver {


    private BooleanChangedListener listener;

    private boolean messageSentStatus;
    private ConnectivityManager connectivityManager;

    public void setListener(BooleanChangedListener listener) {
        this.listener = listener;
    }

    public boolean getMessageSentStatus(){
        return messageSentStatus;
    }

    public void setMessageSentStatus(boolean messageSentStatus){
        this.messageSentStatus = messageSentStatus;
        if(listener != null){
            listener.onBooleanChanged(messageSentStatus);
        }
    }


}
