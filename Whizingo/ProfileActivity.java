package com.mg.socialmedia;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView status,username,fullname,country,dob,gender,relationshipstatus;
    private CircleImageView userProfileImage;
    private DatabaseReference UserProfileref;
    private FirebaseAuth mAuth;
    private String CurrentUserId;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        UserProfileref = FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserId);

        userProfileImage = (CircleImageView) findViewById(R.id.my_profile_pic);
        status = (TextView) findViewById(R.id.my_profile_status);
        username = (TextView) findViewById(R.id.my_profile_user_name);
        fullname = (TextView) findViewById(R.id.my_profile_full_name);
        country = (TextView) findViewById(R.id.my_profile_country);
        dob = (TextView) findViewById(R.id.my_profile_dob);
        gender = (TextView) findViewById(R.id.my_profile_gender);
        relationshipstatus = (TextView) findViewById(R.id.my_profile_relationship_status);


        UserProfileref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists())
            {
                String profileStatus = dataSnapshot.child("Status").getValue().toString();
                String Username = dataSnapshot.child("Username").getValue().toString();
                String Fullname = dataSnapshot.child("FullName").getValue().toString();
                String Country = dataSnapshot.child("Country").getValue().toString();
                String DOB = dataSnapshot.child("DOB").getValue().toString();
                String Gender = dataSnapshot.child("Gender").getValue().toString();
                String RelationStatus = dataSnapshot.child("RelationshipStatus").getValue().toString();
                String profileImage = dataSnapshot.child("ProfileImage").getValue().toString();

                Picasso.with(ProfileActivity.this).load(profileImage).placeholder(R.drawable.profile).into(userProfileImage);
                status.setText(profileStatus);
                username.setText("@"+Username);
                fullname.setText(Fullname);
                country.setText("Country: "+Country);
                dob.setText("DOB: "+DOB);
                gender.setText("Gender"+Gender);
                relationshipstatus.setText("Relationship: "+RelationStatus);
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
