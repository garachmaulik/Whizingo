package com.mg.socialmedia;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView status,username,fullname,country,dob,gender,relationshipstatus;
    private CircleImageView userProfileImage;
    private Toolbar mToolbar;
    private Button sendfriendreq,declinefriendreq;
    private DatabaseReference personprofileref,FriendRequestref,Friendsref;
    private FirebaseAuth mAuth;
    private String SenderUserId,recieverUserId,Profilepic,Status,Country,Dob,Gender,Relationship,Username,Fullname;
    private String CURRENT_STATE,saveCurrentDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();

        SenderUserId = mAuth.getCurrentUser().getUid();

        recieverUserId = getIntent().getExtras().get("visit_user_id").toString();

        personprofileref = FirebaseDatabase.getInstance().getReference().child("Users");

        FriendRequestref = FirebaseDatabase.getInstance().getReference().child("FriendRequests");

        Friendsref = FirebaseDatabase.getInstance().getReference().child("Friends");

        mToolbar = findViewById(R.id.personprofiletoolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        IntializeFields();

        setDetails();
        getSupportActionBar().setTitle(Fullname);
    }

    private void setDetails()
    {
        personprofileref.child(recieverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists())
            {
                if (dataSnapshot.hasChild("ProfileImage"))
                {
                    Profilepic = dataSnapshot.child("ProfileImage").getValue().toString();
                    Picasso.with(PersonProfileActivity.this).load(Profilepic).placeholder(R.drawable.profile).into(userProfileImage);
                }
                if (dataSnapshot.hasChild("Status"))
                {
                    Status = dataSnapshot.child("Status").getValue().toString();
                    status.setText(Status);
                }
                if (dataSnapshot.hasChild("Username"))
                {
                    Username = dataSnapshot.child("Username").getValue().toString();
                    username.setText(Username);
                }
                if (dataSnapshot.hasChild("FullName"))
                {
                    Fullname = dataSnapshot.child("FullName").getValue().toString();
                    fullname.setText(Fullname);
                }
                if (dataSnapshot.hasChild("Gender"))
                {
                    Gender = dataSnapshot.child("Gender").getValue().toString();
                    gender.setText(Gender);
                }
                if (dataSnapshot.hasChild("Country"))
                {
                    Country = dataSnapshot.child("Country").getValue().toString();
                    country.setText(Country);
                }
                if (dataSnapshot.hasChild("RelationshipStatus"))
                {
                    Relationship = dataSnapshot.child("RelationshipStatus").getValue().toString();
                    relationshipstatus.setText(Relationship);
                }
                if (dataSnapshot.hasChild("DOB"))
                {
                    Dob = dataSnapshot.child("DOB").getValue().toString();
                    dob.setText(Dob);
                }

                MaintainingButtons();

            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

            declinefriendreq.setVisibility(View.INVISIBLE);
            declinefriendreq.setEnabled(false);

            if (SenderUserId.equals(recieverUserId))
            {
               sendfriendreq.setVisibility(View.INVISIBLE);
               declinefriendreq.setVisibility(View.INVISIBLE);
            }
            else
                {
                    sendfriendreq.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sendfriendreq.setEnabled(false);
                            if (CURRENT_STATE.equals("Not_Friends"))
                            {
                                SendFriendRequest();
                            }
                            if (CURRENT_STATE.equals("Request_Sent"))
                            {
                                CancelFriendRequest();
                            }
                            if (CURRENT_STATE.equals("Request_Received"))
                            {
                                AcceptFriendRequest();
                            }
                            if (CURRENT_STATE.equals("Friends"))
                            {
                                Unfriend();
                            }
                        }
                    });
                }

    }

    private void Unfriend()
    {
        Friendsref.child(SenderUserId).child(recieverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Friendsref.child(recieverUserId).child(SenderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        sendfriendreq.setEnabled(true);
                                        CURRENT_STATE = "Not_Friends";
                                        sendfriendreq.setText("Send Friend Request");

                                        declinefriendreq.setVisibility(View.INVISIBLE);
                                        declinefriendreq.setEnabled(false);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void AcceptFriendRequest()
    {
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Friendsref.child(SenderUserId).child(recieverUserId).child("Date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                   if (task.isSuccessful())
                   {
                       Friendsref.child(recieverUserId).child(SenderUserId).child("Date").setValue(saveCurrentDate)
                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if (task.isSuccessful())
                                       {
                                           FriendRequestref.child(SenderUserId).child(recieverUserId).removeValue()
                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<Void> task) {
                                                           if (task.isSuccessful())
                                                           {
                                                               FriendRequestref.child(recieverUserId).child(SenderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                   @Override
                                                                   public void onComplete(@NonNull Task<Void> task) {
                                                                       if (task.isSuccessful())
                                                                       {
                                                                           sendfriendreq.setEnabled(true);
                                                                           CURRENT_STATE = "Friends";
                                                                           sendfriendreq.setText("Unfriend");

                                                                           declinefriendreq.setVisibility(View.INVISIBLE);
                                                                           declinefriendreq.setEnabled(false);
                                                                       }
                                                                   }
                                                               });
                                                           }
                                                       }
                                                   });
                                       }
                                   }
                               });
                   }
                    }
                });


    }

    private void CancelFriendRequest()
    {
        FriendRequestref.child(SenderUserId).child(recieverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            FriendRequestref.child(recieverUserId).child(SenderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        sendfriendreq.setEnabled(true);
                                        CURRENT_STATE = "Not_Friends";
                                        sendfriendreq.setText("Send Friend Request");

                                        declinefriendreq.setVisibility(View.INVISIBLE);
                                        declinefriendreq.setEnabled(false);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void MaintainingButtons()
    {
        FriendRequestref.child(SenderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(recieverUserId))
                {
                  String reqType = dataSnapshot.child(recieverUserId).child("Request_Type").getValue().toString();

                  if(reqType.equals("Sent"))
                  {
                  CURRENT_STATE = "Request_Sent";
                  sendfriendreq.setText("Cancel Friend Request");
                  declinefriendreq.setVisibility(View.INVISIBLE);
                  declinefriendreq.setEnabled(false);
                  }
                  else if (reqType.equals("Received"))
                  {
                      CURRENT_STATE = "Request_Received";
                      sendfriendreq.setText("Accept Friend Request");
                      declinefriendreq.setVisibility(View.VISIBLE);
                      declinefriendreq.setEnabled(true);
                      declinefriendreq.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View view) {
                              CancelFriendRequest();
                          }
                      });
                  }
                }
                else
                {
                    Friendsref.child(SenderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(recieverUserId))
                            {
                                CURRENT_STATE = "Friends";
                                sendfriendreq.setText("Unfriend");
                                declinefriendreq.setVisibility(View.INVISIBLE);
                                declinefriendreq.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendFriendRequest()
    {
        FriendRequestref.child(SenderUserId).child(recieverUserId).child("Request_Type").setValue("Sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    FriendRequestref.child(recieverUserId).child(SenderUserId).child("Request_Type").setValue("Received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                          if (task.isSuccessful())
                          {
                              sendfriendreq.setEnabled(true);
                              CURRENT_STATE = "Request_Sent";
                              sendfriendreq.setText("Cancel Friend Request");

                              declinefriendreq.setVisibility(View.INVISIBLE);
                              declinefriendreq.setEnabled(false);
                          }
                        }
                    });
                }
            }
        });
    }

    private void IntializeFields()
    {
        userProfileImage = (CircleImageView) findViewById(R.id.person_profile_pic);
        status = (TextView) findViewById(R.id.person_profile_status);
        username = (TextView) findViewById(R.id.person_profile_user_name);
        fullname = (TextView) findViewById(R.id.person_profile_full_name);
        country = (TextView) findViewById(R.id.person_profile_country);
        dob = (TextView) findViewById(R.id.person_profile_dob);
        gender = (TextView) findViewById(R.id.person_profile_gender);
        relationshipstatus = (TextView) findViewById(R.id.person_profile_relationship_status);
        sendfriendreq = (Button) findViewById(R.id.person_send_friend_request_button);
        declinefriendreq = (Button) findViewById(R.id.person_decline_friend_request_button);
        CURRENT_STATE = "Not_Friends";
    }
}
