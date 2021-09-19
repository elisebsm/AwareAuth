package com.example.testaware;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.testaware.activities.ChatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.Getter;
import lombok.Setter;

public class ClientHandler extends Thread  {

    private String LOG = "Log-Client-handler";
    private DataInputStream in;
    private DataOutputStream out;
    private ExecutorService sendService = Executors.newSingleThreadExecutor();

    @Getter
    @Setter
    private boolean running;


    public ClientHandler(DataInputStream in, DataOutputStream out)  {
        this.in = in;
        this.out = out;
        ChatActivity.updateActivityClientHandler(this);
    }


    @Override
    public void run(){
        running = true;
         try {
             while (running) {
                 String message = in.readUTF();
                 new Handler(Looper.getMainLooper()).post(()-> {
                     ChatActivity.setChat(message);
                 });
            }
        } catch (IOException e) {
             e.printStackTrace();
         }
    }


    public boolean sendMessage(String message){
        if(out == null){
            Log.d(LOG, "outputstream is null");
            return false;
        }
        Runnable sendMessageRunnable = () -> {
            try {
                out.writeUTF(message);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(LOG, "Exception in AppServer  in sendMessage()");
                running = false;
            }
        };
        sendService.submit(sendMessageRunnable);
        return true;
    }
}
