package com.mg.socialmedia;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText SearchInputText;
    private ImageButton SearchButton;
    private RecyclerView SearchResultList;
    private DatabaseReference Usersref;
    private   FirebaseRecyclerAdapter<FindFriends, FindfriendsViewHolder> firebaseRecyclerAdapter;


    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseRecyclerAdapter!=null) {
            firebaseRecyclerAdapter.stopListening();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mToolbar = (Toolbar) findViewById(R.id.FF_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Usersref = FirebaseDatabase.getInstance().getReference().child("Users");

        SearchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        SearchResultList.setLayoutManager(linearLayoutManager);
        SearchButton = (ImageButton) findViewById(R.id.search_button);
        SearchInputText = (EditText) findViewById(R.id.search_box_input);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchBoxinput = SearchInputText.getText().toString();
                Toast.makeText(FindFriendsActivity.this,"Searching",Toast.LENGTH_LONG).show();
                Query searchquery = Usersref.orderByChild("FullName").startAt(searchBoxinput).endAt(searchBoxinput + "uf8ff" );
                FirebaseRecyclerOptions<FindFriends> options = new FirebaseRecyclerOptions.Builder<FindFriends>().setQuery(searchquery,FindFriends.class).build();
              firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindFriends, FindfriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindfriendsViewHolder holder, final int position, @NonNull FindFriends model) {
                        holder.setFullName(model.getFullName());
                        holder.setProfileImage(getApplicationContext(), model.getProfileImage());
                        holder.setStatus(model.getStatus());

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                             String visit_user_id = getRef(position).getKey();
                             Intent personIntent = new Intent(FindFriendsActivity.this,PersonProfileActivity.class);
                             personIntent.putExtra("visit_user_id",visit_user_id);
                             startActivity(personIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindfriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout, viewGroup, false);
                        return new FindfriendsViewHolder(view);
                    }
                };
                firebaseRecyclerAdapter.startListening();
                SearchResultList.setAdapter(firebaseRecyclerAdapter);
            }
        });

    }

    public static class FindfriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public FindfriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setFullName(String fullName) {
            TextView myname = (TextView) mView.findViewById(R.id.all_users_profile_name);
            myname.setText(fullName);
        }
        public void setProfileImage(Context ctx,String profileImage) {
            CircleImageView myProfileImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileImage).placeholder(R.drawable.profile).into(myProfileImage);
        }
        public void setStatus(String status) {
            TextView mystatus = (TextView) mView.findViewById(R.id.all_users_profile_status);
            mystatus.setText(status);
        }

    }
}
