package com.mg.socialmedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private EditText status,username,fullname,country,dob,gender,relationshipstatus;
    private CircleImageView profileimage;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference SettingsUserRef;
    private StorageReference UserProfileRef;
    private String Currentuser;
    private Button UpdateButton;
    private  ProgressDialog loadingbar;
    private String downloadurl;
    final static int gallery_pic = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        Currentuser = mAuth.getCurrentUser().getUid();
        SettingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Currentuser);

        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadingbar = new ProgressDialog(this);
        UserProfileRef = FirebaseStorage.getInstance().getReference().child("ProfileImages");

        profileimage = (CircleImageView) findViewById(R.id.settings_profile_image);
        status = (EditText) findViewById(R.id.settings_status);
        username = (EditText) findViewById(R.id.settings_username);
        fullname = (EditText) findViewById(R.id.settings_fullname);
        country = (EditText) findViewById(R.id.settings_country);
        dob = (EditText) findViewById(R.id.settings_DOB);
        gender = (EditText) findViewById(R.id.settings_gender);
        relationshipstatus = (EditText) findViewById(R.id.settings_relationship_status);
        UpdateButton = (Button) findViewById(R.id.settings_update_account_button);

        SettingsUserRef.addValueEventListener(new ValueEventListener() {
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

                    Picasso.with(SettingsActivity.this).load(profileImage).placeholder(R.drawable.profile).into(profileimage);
                    status.setText(profileStatus);
                    username.setText(Username);
                    fullname.setText(Fullname);
                    country.setText(Country);
                    dob.setText(DOB);
                    gender.setText(Gender);
                    relationshipstatus.setText(RelationStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
            UpdateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ValidateAccountInfo();
                }
            });
            profileimage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent,gallery_pic);
                }
            });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == gallery_pic && resultCode == RESULT_OK && data != null)
        {
            Uri ImageUri = data.getData();
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){
                loadingbar.setTitle("Profile Image");
                loadingbar.setMessage("Please wait while we are updating your profile image.");
                loadingbar.show();
                loadingbar.setCanceledOnTouchOutside(true);
                Uri resulturi = result.getUri();
                StorageReference filepath = UserProfileRef.child(Currentuser + ".jpg");
                filepath.putFile(resulturi).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                        loadingbar.dismiss();
                        Uri download = urlTask.getResult();

                        downloadurl = String.valueOf(download);

                        SettingsUserRef.child("ProfileImage").setValue(downloadurl).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Intent selfintent = new Intent(SettingsActivity.this,SettingsActivity.class);
                                    startActivity(selfintent);
                                    Toast.makeText(SettingsActivity.this,"Profile Image Stored Successfully to Database",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    String message = task.getException().getMessage();
                                    Toast.makeText(SettingsActivity.this,"Error Occurred: "+message,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }



                });
            }
            else{
                loadingbar.dismiss();
                Toast.makeText(SettingsActivity.this,"Error Occurred: Image can't be cropped. Try again.",Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void ValidateAccountInfo()
    {
        String userName = username.getText().toString();
        String Fullname = fullname.getText().toString();
        String Status = status.getText().toString();
        String Country = country.getText().toString();
        String Gender = gender.getText().toString();
        String DOB = dob.getText().toString();
        String Relationstatus = relationshipstatus.getText().toString();

        if (TextUtils.isEmpty(userName))
        {
            Toast.makeText(SettingsActivity.this,"Please enter Username",Toast.LENGTH_SHORT).show();
        }
        else   if (TextUtils.isEmpty(Fullname))
        {
            Toast.makeText(SettingsActivity.this,"Please enter Fullname",Toast.LENGTH_SHORT).show();
        }
        else   if (TextUtils.isEmpty(Status))
        {
            Toast.makeText(SettingsActivity.this,"Please enter Status",Toast.LENGTH_SHORT).show();
        }
        else   if (TextUtils.isEmpty(Country))
        {
            Toast.makeText(SettingsActivity.this,"Please enter Country",Toast.LENGTH_SHORT).show();
        }
        else   if (TextUtils.isEmpty(Gender))
        {
            Toast.makeText(SettingsActivity.this,"Please enter Gender",Toast.LENGTH_SHORT).show();
        }
        else   if (TextUtils.isEmpty(DOB))
        {
            Toast.makeText(SettingsActivity.this,"Please enter DOB",Toast.LENGTH_SHORT).show();
        }
        else   if (TextUtils.isEmpty(Relationstatus))
        {
            Toast.makeText(SettingsActivity.this,"Please enter Relationship Status",Toast.LENGTH_SHORT).show();
        }
        else
            {
                UpdateAccountInfo(userName,Fullname,Status,Gender,DOB,Country,Relationstatus);
            }
    }

    private void UpdateAccountInfo(String userName, String fullname, String status, String gender, String dob, String country, String relationstatus)
    {
        loadingbar.setTitle("Updating Info");
        loadingbar.setMessage("Please wait while your info is updated.");
        loadingbar.setCanceledOnTouchOutside(true);
        loadingbar.show();
        HashMap usermap = new HashMap();
        usermap.put("Username",userName);
        usermap.put("FullName",fullname);
        usermap.put("Country",country);
        usermap.put("DOB",dob);
        usermap.put("Gender",gender);
        usermap.put("RelationshipStatus",relationstatus);
        usermap.put("Status",status);
        SettingsUserRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
           if (task.isSuccessful())
           {    loadingbar.dismiss();
               Toast.makeText(SettingsActivity.this,"Account info updated Successfully.",Toast.LENGTH_SHORT).show();
               SendUserToMainActivity();
           }
           else
               {
                   loadingbar.dismiss();
                   String message = task.getException().getMessage();
                   Toast.makeText(SettingsActivity.this, "Error Occurred: "+message, Toast.LENGTH_SHORT).show();
               }
            }
        });
    }
    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();

    }

}
