package com.mg.socialmedia;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView Clickpostimage;
    private TextView Clickpostdescription;
    private Button Clickpostdelete, Clickpostedit;
    private String description, image, postkey, CurrentUserId, databaseUserId;
    private DatabaseReference ClickPostRef;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);
        mToolbar = (Toolbar) findViewById(R.id.click_post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Post Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        postkey = getIntent().getExtras().get("PostKey").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postkey);
        Clickpostimage = (ImageView) findViewById(R.id.click_post_image);
        Clickpostdescription = (TextView) findViewById(R.id.click_post_description);
        Clickpostedit = (Button) findViewById(R.id.click_post_edit);
        Clickpostdelete = (Button) findViewById(R.id.click_post_delete);
        Clickpostedit.setVisibility(View.INVISIBLE);
        Clickpostdelete.setVisibility(View.INVISIBLE);

        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              if (dataSnapshot.exists()){
                  description = dataSnapshot.child("Description").getValue().toString();
                  image = dataSnapshot.child("PostImage").getValue().toString();
                  databaseUserId = dataSnapshot.child("UID").getValue().toString();

                  Clickpostdescription.setText(description);
                  Picasso.with(ClickPostActivity.this).load(image).into(Clickpostimage);

                  if (CurrentUserId.equals(databaseUserId)) {
                      Clickpostedit.setVisibility(View.VISIBLE);
                      Clickpostdelete.setVisibility(View.VISIBLE);
                  }
                  Clickpostedit.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View view) {
                          EditPost(description);
                      }
                  });
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Clickpostdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteCurrentPost();
            }
        });
    }

    private void EditPost(String description)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");
        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            ClickPostRef.child("Description").setValue(inputField.getText()).toString();
            Toast.makeText(ClickPostActivity.this,"Post Updated",Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }


    private void DeleteCurrentPost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Delete Post");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ClickPostRef.removeValue();
                SendUserToMainActivity();
                Toast.makeText(ClickPostActivity.this, "Post has been deleted.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();

    }
}