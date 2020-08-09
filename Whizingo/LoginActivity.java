package com.mg.socialmedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private Button login_button;
    private EditText User_Email,User_Password;
    private TextView Need_new_account_link,Forgotpasswordlink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;
    private ImageView googleSignInButton;
    private static final int RC_SIGN_IN =1;
    private GoogleApiClient mGoogleSignInClient;
    private static final String TAG ="LoginActivity" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        Need_new_account_link = (TextView) findViewById(R.id.register_account_link);
        User_Email = (EditText) findViewById(R.id.login_email);
        Forgotpasswordlink = (TextView) findViewById(R.id.forgot_password_link);
        User_Password = (EditText) findViewById(R.id.login_password);
        login_button = (Button) findViewById(R.id.login_btn);
        mAuth = FirebaseAuth.getInstance();
        loadingbar = new ProgressDialog(this);
        googleSignInButton = (ImageView) findViewById(R.id.google_signin_btn);
        Need_new_account_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();
            }
        });

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowingUserToLogin();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
            {
            Toast.makeText(LoginActivity.this,"Connection to Google Sign in failed.",Toast.LENGTH_SHORT).show();
            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                signIn();
            }
        });
        Forgotpasswordlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });

    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN)
        {
            loadingbar.setTitle("Google Sign In");
            loadingbar.setMessage("Please wait while we are logging you in using your google account.");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess())
            {
             GoogleSignInAccount account = result.getSignInAccount();
             firebaseAuthWithGoogle(account);
             Toast.makeText(LoginActivity.this,"Please wait while we sign you in.",Toast.LENGTH_SHORT).show();
            }
            else
                {
                    loadingbar.dismiss();
                    Toast.makeText(LoginActivity.this,"There was problem, please try again later",Toast.LENGTH_SHORT).show();
                }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "signInWithCredential:success");
                            SendUserToMainActivity();
                            loadingbar.dismiss();

                        } else
                            {
                                String message = task.getException().getMessage();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            SendUserToLoginActivity();
                            Toast.makeText(LoginActivity.this,"Error Occurred: "+message,Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                            }
                    }
                });
    }

    private void SendUserToLoginActivity()
    {
        Intent LoginIntent = new Intent(LoginActivity.this, LoginActivity.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentuser = mAuth.getCurrentUser();
        if (currentuser != null){
            SendUserToMainActivity();
        }
    }

    private void AllowingUserToLogin() {
        String email,password;
        email = User_Email.getText().toString();
        password = User_Password.getText().toString();
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter Email!",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter Password!",Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingbar.setTitle("Log In");
            loadingbar.setMessage("Please wait while we are logging you in.");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "You are logged in successfully.", Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                        SendUserToMainActivity();
                    } else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(LoginActivity.this,"Error Occurred: "+message,Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent MainIntent = new Intent(LoginActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();

    }

    public void SendUserToRegisterActivity(){
        Intent RegisterIntent = new Intent(LoginActivity.this, com.mg.socialmedia.RegisterActivity.class);
        startActivity(RegisterIntent);
    }
}
