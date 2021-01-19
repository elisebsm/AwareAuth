package com.example.testaware.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.testaware.Chat;
import com.example.testaware.R;

import java.util.ArrayList;


public class ChatsListAdapter extends ArrayAdapter<Chat> {
    private ArrayList<Chat> chats;
    Context context;
    public ChatsListAdapter(Context context, ArrayList<Chat> chats) {
        super(context, R.layout.chat_list_elements, chats);
        this.chats = chats;
        this.context = context;
    }
/*
    //inner class to hold views for each row
    public class ViewHolder{
        TextView name;
    }*/


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Chat chat = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_list_elements, parent, false);
        }

        /*// viewholder object
        final ViewHolder holder = new ViewHolder();
        holder.name = convertView.findViewById(R.id.tvUsername);
        holder.name.setText(chat.getUsername());*/

        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tvUsername);
        // Populate the data into the template view using the data object
        tvName.setText(chat.getUsername());
        // Return the completed view to render on screen
        return convertView;
    }



}
