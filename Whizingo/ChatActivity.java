package com.mg.socialmedia;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton select_image,send_message;
    private EditText input_message;
    private RecyclerView userMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;
    private String messageReceiverId,messageReceiverName,SenderId,saveCurrentDate,saveCurrentTime;
    private TextView userName;
    private CircleImageView ReceiverProfileImage;
    private DatabaseReference Rootref;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        SenderId = mAuth.getCurrentUser().getUid();

        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("Username").toString();

        Rootref = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        DisplayReceiverInfo();

        send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

        FetchMessages();

    }

    private void FetchMessages()
    {
        Rootref.child("Messages").child(SenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
           if (dataSnapshot.exists())
           {
               Messages messages = dataSnapshot.getValue(Messages.class);
               messagesList.add(messages);
               messagesAdapter.notifyDataSetChanged();
           }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage()
    {
        String messageText = input_message.getText().toString();
        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this,"Please enter a message",Toast.LENGTH_SHORT).show();
        }
        else
            {
                String message_sender_ref = "Messages/" + SenderId + "/" + messageReceiverId;
                String message_receiver_ref = "Messages/" + messageReceiverId + "/" + SenderId;

                DatabaseReference Users_message_key = Rootref.child("Messages").child(SenderId).child(messageReceiverId)
                        .push();
                String message_push_id = Users_message_key.getKey();

                Calendar callForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
                saveCurrentDate = currentDate.format(callForDate.getTime());
                Calendar callForTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH-mm aa");
                saveCurrentTime = currentTime.format(callForTime.getTime());

                Map messageTextBody = new HashMap();
                    messageTextBody.put("Message",messageText);
                    messageTextBody.put("Time",saveCurrentTime);
                    messageTextBody.put("Date",saveCurrentDate);
                    messageTextBody.put("Type","Text");
                    messageTextBody.put("From",SenderId);

                Map messageBodyDetails = new HashMap();
                    messageBodyDetails.put(message_sender_ref + "/" + message_push_id , messageTextBody);
                    messageBodyDetails.put(message_receiver_ref + "/"  + message_push_id , messageTextBody);

                Rootref.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                       if (task.isSuccessful())
                       {
                           input_message.setText("");
                       }
                       else
                           {
                               String message = task.getException().getMessage();
                               Toast.makeText(ChatActivity.this,"Error Occurred: "+message,Toast.LENGTH_SHORT).show();
                               input_message.setText("");
                           }
                    }
                });

            }
    }

    private void DisplayReceiverInfo()
    {

        userName.setText(messageReceiverName);

        Rootref.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
           if (dataSnapshot.exists())
           {
               final String profileimage = dataSnapshot.child("ProfileImage").getValue().toString();
               Picasso.with(ChatActivity.this).load(profileimage).placeholder(R.drawable.profile).into(ReceiverProfileImage);
           }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void InitializeFields()
    {
        mToolbar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        getSupportActionBar().setCustomView(action_bar_view);

        userName = findViewById(R.id.custom_profile_name);
        ReceiverProfileImage = findViewById(R.id.custom_profile_image);

        select_image = findViewById(R.id.send_image_file);
        send_message = findViewById(R.id.send_message);
        input_message = findViewById(R.id.input_message);
        userMessagesList = findViewById(R.id.messages_list_users);

        messagesAdapter = new MessagesAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setHasFixedSize(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);
    }
}
