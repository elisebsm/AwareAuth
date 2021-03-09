package com.example.testaware;


import java.util.ArrayList;
import java.util.List;

public class RolesChangedListener {

    public Boolean status = Boolean.FALSE;
    public ConnectivityListener listener;

    public Boolean getStatus(){
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
        if (status) {
            listener.onChange();
        }

    }

    public void addConnectivityListener(ConnectivityListener l) {
        this.listener = l;
    }

    public interface ConnectivityListener{
        void onChange();
    }

}
