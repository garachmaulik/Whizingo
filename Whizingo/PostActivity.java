package com.mg.socialmedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class PostActivity extends AppCompatActivity {
        private Toolbar mtoolbar;
        private ImageButton selectpostimage;
        private Button updatepostbtn;
        private EditText postdescription;
        private Uri imageUri;
        private String description,saveCurrentDate,saveCurrentTime,postRandomName,downloadurl,currentuserId;
        private StorageReference PostImagesref;
        private DatabaseReference UsersRef,UserPostref;
        private ProgressDialog loadingbar;
        private FirebaseAuth mAuth;
        private long countposts = 0;
        final static int gallery_pic = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();

        mtoolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentuserId = mAuth.getCurrentUser().getUid();
        UserPostref = FirebaseDatabase.getInstance().getReference().child("Posts");

        loadingbar = new ProgressDialog(this);
        PostImagesref = FirebaseStorage.getInstance().getReference();
        selectpostimage = (ImageButton) findViewById(R.id.select_post_image);

        updatepostbtn = (Button) findViewById(R.id.update_post_btn);

        postdescription = (EditText) findViewById(R.id.post_description);

        selectpostimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        updatepostbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                validatepostinfo();
            }
        });
    }

    private void validatepostinfo() {
        description = postdescription.getText().toString();

        if (imageUri == null)
        {
            Toast.makeText(this,"Please select post image.",Toast.LENGTH_SHORT).show();
        }
        else
            {
                loadingbar.setTitle("Add New Post");
                loadingbar.setMessage("Please wait while we are uploading your post.");
                loadingbar.show();
                loadingbar.setCanceledOnTouchOutside(true);
                StoringImageToFirebaseStorage();
            }
    }

    private void StoringImageToFirebaseStorage()
    {

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());
        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH-mm");
        saveCurrentTime = currentTime.format(callForTime.getTime());
        postRandomName = saveCurrentDate+saveCurrentTime;
        StorageReference filepath = PostImagesref.child("Post Images").child(imageUri.getLastPathSegment()+postRandomName+".jpg");
        filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!urlTask.isSuccessful());
                Uri download = urlTask.getResult();
                downloadurl = String.valueOf(download);
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
           if (task.isSuccessful()){
               Toast.makeText(PostActivity.this,"Post uploaded successfully.",Toast.LENGTH_SHORT).show();
               SavingPostInfoToDatabase();

           }
           else{
               String message = task.getException().getMessage();
               loadingbar.dismiss();
               Toast.makeText(PostActivity.this,"Error Occurred: "+message,Toast.LENGTH_SHORT).show();
           }
            }
        });
    }

    private void SavingPostInfoToDatabase()
    {
        UserPostref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
           if (dataSnapshot.exists())
           {
               countposts = dataSnapshot.getChildrenCount();
           }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UsersRef.child(currentuserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                       final String userfullname = dataSnapshot.child("FullName").getValue().toString();
                       final String userprofileimage = dataSnapshot.child("ProfileImage").getValue().toString();

                    HashMap postmap = new HashMap();
                    postmap.put("UID", currentuserId);
                    postmap.put("Date", saveCurrentDate);
                    postmap.put("Time", saveCurrentTime);
                    postmap.put("Description", description);
                    postmap.put("PostImage", downloadurl);
                    postmap.put("FullName", userfullname);
                    postmap.put("ProfileImage",userprofileimage);
                    postmap.put("Counter", countposts);

                    UserPostref.child(currentuserId + postRandomName).updateChildren(postmap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                loadingbar.dismiss();
                                SendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "Post Uploaded Successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                loadingbar.dismiss();
                                String message = task.getException().getMessage();
                                Toast.makeText(PostActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });




                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,gallery_pic);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == gallery_pic && resultCode == RESULT_OK && data!=null)
        {
            imageUri = data.getData();
            selectpostimage.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this,MainActivity.class);
        startActivity(mainIntent);
    }
}
