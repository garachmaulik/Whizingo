package com.mg.socialmedia;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myfriendlist;
    private DatabaseReference Friendsref, Usersref;
    private FirebaseAuth mAuth;
    private String OnlineUserId;
    private Toolbar mToolbar;
    FirebaseRecyclerAdapter<Friends,friendsviewholder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Usersref = FirebaseDatabase.getInstance().getReference().child("Users");

        OnlineUserId = mAuth.getCurrentUser().getUid();

        myfriendlist = (RecyclerView) findViewById(R.id.friend_list);
        myfriendlist.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myfriendlist.setLayoutManager(linearLayoutManager);

        Friendsref = FirebaseDatabase.getInstance().getReference().child("Friends").child(OnlineUserId);

        DiplayAllFriends();
    }

    private void DiplayAllFriends()
    {
        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(Friendsref,Friends.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, friendsviewholder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final friendsviewholder holder, int position, @NonNull Friends model)
            {
                holder.setDate(model.getDate());
                final String userIDs = getRef(position).getKey();


                Usersref.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                    if (dataSnapshot.exists())
                    {
                        final String username = dataSnapshot.child("FullName").getValue().toString();
                        final String profileimage = dataSnapshot.child("ProfileImage").getValue().toString();

                        holder.setFullName(username);
                        holder.setProfileImage(getApplicationContext(),profileimage);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[] = new CharSequence[]{
                                        username + "'s Profile","Send Message"
                                };
                                AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i == 0)
                                    {
                                        Intent profileIntent = new Intent(FriendsActivity.this,PersonProfileActivity.class);
                                        profileIntent.putExtra("visit_user_id", userIDs);
                                        startActivity(profileIntent);
                                    }
                                    if (i == 1)
                                    {
                                        Intent chatIntent = new Intent(FriendsActivity.this,ChatActivity.class);
                                        chatIntent.putExtra("visit_user_id", userIDs);
                                        chatIntent.putExtra("Username",username);
                                        startActivity(chatIntent);
                                    }
                                    }
                                });
                                builder.show();
                            }
                        });

                    }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public friendsviewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout,viewGroup,false);
                return new friendsviewholder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        myfriendlist.setAdapter(firebaseRecyclerAdapter);
    }


    public static class friendsviewholder extends RecyclerView.ViewHolder
    {
        View mView;
        public friendsviewholder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }
        public void setFullName(String fullName) {
            TextView myname = (TextView) mView.findViewById(R.id.all_users_profile_name);
            myname.setText(fullName);
        }
        public void setProfileImage(Context ctx, String profileImage) {
            CircleImageView myProfileImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileImage).placeholder(R.drawable.profile).into(myProfileImage);
        }
        public void setDate(String Date)
        {
            TextView friendsdate = (TextView) mView.findViewById(R.id.all_users_profile_status);
            friendsdate.setText("Friends since: "+ Date);
        }
    }
}
