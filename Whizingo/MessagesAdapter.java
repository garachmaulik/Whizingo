package com.mg.socialmedia;

import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>
{
    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersDatabaseRef;

    public MessagesAdapter(List<Messages> userMessageList)
    {
        this.userMessageList = userMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_layout_of_user,viewGroup,false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i)
    {
        String messagesenderid = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(i);
        String fromUserId = messages.getFrom();
        String messageType = messages.getType();

        UsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        UsersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists())
            {
                String image = dataSnapshot.child("ProfileImage").getValue().toString();
                Picasso.with(messageViewHolder.receiverProfileImage.getContext()).load(image).placeholder(R.drawable.profile).into(messageViewHolder.receiverProfileImage);
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (messageType.equals("Text"))
        {
            messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);

            if (fromUserId.equals(messagesenderid))
            {
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_text_background);
                messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
                messageViewHolder.senderMessageText.setGravity(Gravity.LEFT);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
            }
            else
                {
                    messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);
                    messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);

                    messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_text_background);
                    messageViewHolder.receiverMessageText.setTextColor(Color.WHITE);
                    messageViewHolder.receiverMessageText.setGravity(Gravity.LEFT);
                    messageViewHolder.receiverMessageText.setText(messages.getMessage());
                }

        }

    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView senderMessageText,receiverMessageText;
            public CircleImageView receiverProfileImage;
        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
        }
    }
}
