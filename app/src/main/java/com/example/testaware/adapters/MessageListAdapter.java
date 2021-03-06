package com.example.testaware.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.testaware.listitems.MessageListItem;
import com.example.testaware.R;

import java.util.ArrayList;

public class MessageListAdapter extends RecyclerView.Adapter{
    private ArrayList<MessageListItem> mMessageListItemList;    //list of messages beeing sent


    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;


    public MessageListAdapter(Context context, ArrayList<MessageListItem> messageListItemList) {
        mMessageListItemList = messageListItemList;
    }

    @Override
    public int getItemCount() {
        return mMessageListItemList.size();
    }


    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        MessageListItem message = (MessageListItem) mMessageListItemList.get(position);

        if (message.getIpv6Address().equals("User2")) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_SENT;



        }

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_message, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.other_message, parent, false);
            return new ReceivedMessageHolder(view);
        }
        return null;

    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MessageListItem messageListItem = (MessageListItem) mMessageListItemList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(messageListItem);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(messageListItem);
        }



    }



    public class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, nameText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.textViewOtherChatBubble);

            //nameText = (TextView) itemView.findViewById(R.id.textChatUserOther);   //bind name of message sender

        }

        void bind(MessageListItem messageListItem) {
            messageText.setText(messageListItem.getMessage());


            //nameText.setText("Elise");


        }
    }

    public class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;


        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.textViewVMyChatBubble);

        }

        void bind(MessageListItem messageListItem) {
            messageText.setText(messageListItem.getMessage());


        }

    }





}