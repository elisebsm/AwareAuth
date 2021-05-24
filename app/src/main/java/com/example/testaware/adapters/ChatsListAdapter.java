package com.example.testaware.adapters;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testaware.listitems.ChatListItem;
import com.example.testaware.R;

import java.util.ArrayList;

import lombok.Getter;


public class ChatsListAdapter extends ArrayAdapter<ChatListItem> {
    @Getter
    private ArrayList<ChatListItem> chats;
    Context context;


    public ChatsListAdapter(Context context, ArrayList<ChatListItem> chats) {
        super(context, R.layout.chat_list_elements, chats);
        this.chats = chats;
        this.context = context;
    }
/*
    //inner class to hold views for each row
    public class ViewHolder{
        TextView name;
    }*/
//TODO: change to recyclerview

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ChatListItem chat = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_list_elements, parent, false);
        }

        /*// viewholder object
        final ViewHolder holder = new ViewHolder();
        holder.name = convertView.findViewById(R.id.tvUsername);
        holder.name.setText(chat.getUsername());*/

        // Lookup view for data population
        TextView tvName = convertView.findViewById(R.id.tvUsername);
        // Populate the data into the template view using the data object
        tvName.setText(chat.getUsername());
        // Return the completed view to render on screen

        TextView peerIP = convertView.findViewById(R.id.tvPeerIP);
        // Populate the data into the template view using the data object




        tvName.setText(chat.getPeerIpv6());

        TextView status = convertView.findViewById(R.id.tvStatus);

        status.setText(chat.getStatus());

        TextView certificate = convertView.findViewById(R.id.tvCertificateStatus);

        status.setText(chat.getStatus());

        return convertView;
    }

}
