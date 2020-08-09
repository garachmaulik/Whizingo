package com.mg.socialmedia;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText resetpasswordEmail;
    private Button resetpasswordbutton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mToolbar = (Toolbar) findViewById(R.id.resetactivitytoolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Reset Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        resetpasswordEmail = (EditText) findViewById(R.id.reset_email);
        resetpasswordbutton = (Button) findViewById(R.id.reset_button);

        resetpasswordbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String useremail = resetpasswordEmail.getText().toString();
                if (TextUtils.isEmpty(useremail))
                {
                    Toast.makeText(ResetPasswordActivity.this,"Please Enter Your Registered Email",Toast.LENGTH_SHORT).show();

                }
                else
                    {
                      mAuth.sendPasswordResetEmail(useremail).addOnCompleteListener(new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful())
                           {
                             Toast.makeText(ResetPasswordActivity.this,"Please check your email",Toast.LENGTH_LONG).show();
                             startActivity(new Intent(ResetPasswordActivity.this,LoginActivity.class));
                           }
                           else
                               {
                                   String message = task.getException().getMessage();
                                   Toast.makeText(ResetPasswordActivity.this,"Error Occurred:"+message,Toast.LENGTH_SHORT).show();
                               }
                          }
                      })  ;
                }
            }
        });


    }
}
