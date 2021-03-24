
package com.example.testaware.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.testaware.R;
import com.example.testaware.models.Message;

import java.security.KeyPair;
import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {
    private final Context context;
    private final List<Message> messages;
    private KeyPair key;

    public MessageAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
        this.context = context;
        this.messages = objects;
    }

    public MessageAdapter(Context context, int resource, List<Message> objects, KeyPair key) {
        this(context, resource, objects);
        this.key = key;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;

        if(messages.get(position).getFrom().equals(key.getPublic())) {
            rowView = inflater.inflate(R.layout.my_message, parent, false);
        } else {
            rowView = inflater.inflate(R.layout.other_message, parent, false);
        }

        TextView textView = rowView.findViewById(R.id.textViewChatBubble);
        textView.setText(messages.get(position).getPlaintext(key.getPrivate()));

        return rowView;
    }
}
