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
import android.widget.TextView;
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

public class SetUpActivity extends AppCompatActivity {

    private EditText UserName,FullName,CountryName;
    private Button SaveInfoButton;
    private CircleImageView ProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private String currentuserID;
    private ProgressDialog loadingbar;
    private StorageReference UserProfileRef;
    private String downloadurl;
    final static int gallery_pic = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        mAuth = FirebaseAuth.getInstance();

        currentuserID = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentuserID);

        UserProfileRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        UserName = (EditText) findViewById(R.id.set_username);

        FullName = (EditText) findViewById(R.id.set_fullname);

        CountryName = (EditText) findViewById(R.id.set_country);

        SaveInfoButton = (Button) findViewById(R.id.setup_info_btn);

        ProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);

        loadingbar = new ProgressDialog(this);

        SaveInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveAccountSetUpInfo();
            }
        });
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,gallery_pic);
            }
        });
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                if (dataSnapshot.hasChild("ProfileImage")) {
                    String image = dataSnapshot.child("ProfileImage").getValue().toString();
                    Picasso.with(SetUpActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);
                }
                else{
                    Toast.makeText(SetUpActivity.this,"Please select profile image first.",Toast.LENGTH_SHORT).show();
                }
            }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                StorageReference filepath = UserProfileRef.child(currentuserID + ".jpg");
                filepath.putFile(resulturi).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                        loadingbar.dismiss();
                        Uri download = urlTask.getResult();

                        downloadurl = String.valueOf(download);

                        UsersRef.child("ProfileImage").setValue(downloadurl).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Intent selfintent = new Intent(SetUpActivity.this,SetUpActivity.class);
                                    startActivity(selfintent);
                                    Toast.makeText(SetUpActivity.this,"Profile Image Stored Successfully to Database",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    String message = task.getException().getMessage();
                                    Toast.makeText(SetUpActivity.this,"Error Occurred: "+message,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }



                });
            }
            else{
                loadingbar.dismiss();
                Toast.makeText(SetUpActivity.this,"Error Occurred: Image can't be cropped. Try again.",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SaveAccountSetUpInfo() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this,"Please Enter Username",Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(fullname)){
            Toast.makeText(this,"Please Enter Full Name",Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(country)){
            Toast.makeText(this,"Please Enter Country",Toast.LENGTH_SHORT).show();
        }
        else {
            loadingbar.setTitle("Saving Info");
            loadingbar.setMessage("Please wait while we are creating your account.");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);
            HashMap usermap = new HashMap();
                usermap.put("Username",username);
                usermap.put("FullName",fullname);
                usermap.put("Country",country);
                usermap.put("Status","Whizingo is so cool, developed by Maulik Garach");
                usermap.put("Gender","none");
                usermap.put("DOB","none");
                usermap.put("RelationshipStatus","none");
                UsersRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if (task.isSuccessful()){
                            loadingbar.dismiss();
                            SendUserToMainActivity();
                            Toast.makeText(SetUpActivity.this,"Your Account is created Successfully",Toast.LENGTH_LONG).show();
                        }
                        else{
                            loadingbar.dismiss();
                            String message = task.getException().getMessage();
                            Toast.makeText(SetUpActivity.this,"Error Occurred: "+message,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }

    }

    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(SetUpActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}
