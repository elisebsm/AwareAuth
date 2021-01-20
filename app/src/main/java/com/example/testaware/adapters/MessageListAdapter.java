package com.example.testaware.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testaware.MessageListItem;
import com.example.testaware.R;

import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {
    private ArrayList<MessageListItem> mMessageListItemList;    //list of messages beeing sent
    Context context;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;



    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;


        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (TextView) view.findViewById(R.id.textViewVMyChatBubble);
        }

        public TextView getTextView() {
            return textView;
        }
    }


     public MessageListAdapter(Context context, ArrayList<MessageListItem> messageListItemList) {

         mMessageListItemList = messageListItemList;
         context = context;
     }




    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
/*
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
*/
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.my_message, viewGroup, false);

        return new ViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        MessageListItem messageListItem = (MessageListItem) mMessageListItemList.get(position);
        String message = messageListItem.getMessage();
       // ((SentMessageHolder) holder).bind(messageListItem);
        /*
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(messageListItem);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(messageListItem);
        }

         */

        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.getTextView().setText(message);

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    /*

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        MessageListItem message = (MessageListItem) mMessageListItemList.get(position);

        if (true) {  // if sender is this user, //TODO: change to actually chech who sender is
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }

    }


    public class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, nameText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.textViewOtherChatBubble);

            nameText = (TextView) itemView.findViewById(R.id.textChatUserOther);   //bind name of message sender

        }

        void bind(MessageListItem messageListItem) {
            messageText.setText(messageListItem.getMessage());


            nameText.setText(messageListItem.getSender());


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


     */
}
