package com.mg.socialmedia;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,Postref,Likesref;
    private CircleImageView navprofileimage;
    private TextView navprofilename;
    private String CurrentUserId;
    private ImageButton addnewpostbtn;
    private FirebaseRecyclerAdapter<Posts, postsviewholder> firebaseRecyclerAdapter;
    private boolean likecheck = false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        UserRef  = FirebaseDatabase.getInstance().getReference().child("Users");
        Postref = FirebaseDatabase.getInstance().getReference().child("Posts");
        Likesref = FirebaseDatabase.getInstance().getReference().child("Likes");
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Whizingo");
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        View navView = navigationView.inflateHeaderView(R.layout.nav_header);
        addnewpostbtn = (ImageButton) findViewById(R.id.add_new_post_btn); 
        navprofileimage = (CircleImageView) navView.findViewById(R.id.profile_pic);
        navprofilename = (TextView) navView.findViewById(R.id.username);


        UserRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("FullName")) {
                        String fullname = dataSnapshot.child("FullName").getValue().toString();
                        navprofilename.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("ProfileImage")) {
                        String image = dataSnapshot.child("ProfileImage").getValue().toString();
                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(navprofileimage);
                    }
                    else{
                        Toast.makeText(MainActivity.this,"Profile name do not exist",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });
        
        addnewpostbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendusertoaddpostactivity();
            }
        });

        DisplayAllUsersPost();

    }


    private void DisplayAllUsersPost()
    {
        Query displaypostindescendingorder = Postref.orderByChild("Counter");
        final FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(displaypostindescendingorder, Posts.class)
                        .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, postsviewholder>(options)
    {
        @Override
        protected void onBindViewHolder(@NonNull postsviewholder holder, int position, @NonNull Posts model)
        {
            final String PostKey = getRef(position).getKey();
            holder.setFullName(model.getFullName());
            holder.setTime(model.getTime());
            holder.setDate(model.getDate());
            holder.setDescription(model.getDescription());
            holder.setProfileImage(getApplicationContext(),model.getProfileImage());
            holder.setPostImage(getApplicationContext(),model.getPostImage());
            holder.setLikeButtonStatus(PostKey);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                Intent clickpostIntent = new Intent(MainActivity.this,ClickPostActivity.class);
                clickpostIntent.putExtra("PostKey",PostKey);
                startActivity(clickpostIntent);

                }
            });

            holder.LikeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    likecheck = true;

                    Likesref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (likecheck)
                        {
                            if (dataSnapshot.child(PostKey).hasChild(CurrentUserId))
                            {
                                Likesref.child(PostKey).child(CurrentUserId).removeValue();
                                likecheck = false;
                            }
                            else
                            {
                                Likesref.child(PostKey).child(CurrentUserId).setValue(true);
                                likecheck = false;
                            }
                        }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });

            holder.CommentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent CommentIntent = new Intent(MainActivity.this,CommentsActivity.class);
                    CommentIntent.putExtra("PostKey",PostKey);
                    startActivity(CommentIntent);
                }
            });
        }

        @NonNull
        @Override
        public postsviewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) { ;
           View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_post_layout,viewGroup,false);
            return new postsviewholder(view);
        }
    };
    postList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class postsviewholder extends RecyclerView.ViewHolder {
        View mView;
        ImageButton LikeButton,CommentButton;
        TextView DisplayLikes;
        int countlikes;
        String currentUserId;
        DatabaseReference likeref;


        public postsviewholder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            LikeButton = (ImageButton) mView.findViewById(R.id.like_button);
            CommentButton = (ImageButton) mView.findViewById(R.id.comment_button);
            DisplayLikes = (TextView) mView.findViewById(R.id.display_likes);
            likeref = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        }
        public void setFullName(String fullName)
        {
        TextView username = (TextView) mView.findViewById(R.id.post_user_name);
        username.setText(fullName);
        }
        public void setProfileImage(Context ctx, String profileimage) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }
        public void setTime(String time) {
            TextView post_Time = (TextView) mView.findViewById(R.id.post_time);
            post_Time.setText(time);
        }
        public void setDate(String post_date) {
            TextView post_Date = (TextView) mView.findViewById(R.id.date_of_post);
            post_Date.setText(post_date);
        }
        public void setDescription(String description) {
            TextView post_Description = (TextView) mView.findViewById(R.id.post_description);
            post_Description.setText(description);
        }
        public void setPostImage(Context ctx,String postimage) {
            ImageView post_Image = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(postimage).into(post_Image);
        }

        public void setLikeButtonStatus(final String PostKey)
        {
            likeref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                if (dataSnapshot.child(PostKey).hasChild(currentUserId))
                {
                    countlikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                    LikeButton.setImageResource(R.drawable.like);
                    DisplayLikes.setText(countlikes+" Likes");
                }
                else
                    {
                        countlikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikeButton.setImageResource(R.drawable.dislike);
                        DisplayLikes.setText(countlikes+" Likes");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    private void sendusertoaddpostactivity() {
        Intent postIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(postIntent);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
        FirebaseUser currentuser = mAuth.getCurrentUser();
        if (currentuser == null){
            SendUserToLoginActivity();
        }
        else{
            CheckUserExistence();

        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    private void CheckUserExistence() {
        final String Current_User_Id = mAuth.getCurrentUser().getUid();
        UserRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.hasChild(Current_User_Id)){
                    SendUserToSetUpActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void SendUserToSetUpActivity()
    {
        Intent SetupIntent = new Intent(MainActivity.this, SetUpActivity.class);
        SetupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(SetupIntent);
        finish();
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent =new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_post:
                sendusertoaddpostactivity();
                break;
            case R.id.nav_profile:
               SendUserToProfileActivity();
                break;
            case R.id.nav_home:
                Toast.makeText(this,"Home",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                SendUserToFriendsActivity();
                break;
            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                break;
            case R.id.nav_messages:
                SendUserToFriendsActivity();
                break;
            case R.id.nav_settings:
               SendUserToSettingsActivity();
                break;
            case R.id.nav_log_out:
               mAuth.signOut();
               SendUserToLoginActivity();
                break;
        }
        }
    private void SendUserToSettingsActivity()
    {
        Intent SettingIntent =new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(SettingIntent);

    }
    private void SendUserToFriendsActivity()
    {
        Intent FriendsIntent =new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(FriendsIntent);

    }
    private void SendUserToProfileActivity()
    {
        Intent ProfileIntent =new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(ProfileIntent);

    }
    private void SendUserToFindFriendsActivity()
    {
        Intent FFIntent =new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(FFIntent);

    }

}
