package com.mg.socialmedia;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private ImageButton postCommentbtn;
    private Button dComment,rComment;
    private DatabaseReference Usersref, Postref ;
    private EditText commentInput;
    private RecyclerView CommentList;
    private Toolbar mToolbar;
    private String Post_Key, current_user_id, profileimage;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;
    private FirebaseRecyclerAdapter<Comments, commentViewholder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mAuth = FirebaseAuth.getInstance();

        current_user_id = mAuth.getCurrentUser().getUid();

        mToolbar = (Toolbar) findViewById(R.id.commentToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Comments:");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Post_Key = getIntent().getExtras().get("PostKey").toString();
        Usersref = FirebaseDatabase.getInstance().getReference().child("Users");
        Postref = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");
        postCommentbtn = (ImageButton) findViewById(R.id.post_comment_button);
        commentInput = (EditText) findViewById(R.id.comment_input);
        CommentList = (RecyclerView) findViewById(R.id.commentsList);
        CommentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentList.setLayoutManager(linearLayoutManager);

        loadingbar = new ProgressDialog(this);

        postCommentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Usersref.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String username = dataSnapshot.child("FullName").getValue().toString();
                            profileimage = dataSnapshot.child("ProfileImage").getValue().toString();
                            validateComment(username);

                            commentInput.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Comments> options = new FirebaseRecyclerOptions.Builder<Comments>().setQuery(Postref, Comments.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, commentViewholder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull commentViewholder holder, int position, @NonNull final Comments model) {
                holder.setUsername(model.getUsername());
                holder.setComment(model.getComment());
                holder.setDate(model.getDate());
                holder.setTime(model.getTime());
                holder.setProfileImage(getApplicationContext(), model.getProfileImage());

                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CommentsActivity.this);
                        if (current_user_id.equals(model.getUID()))
                        {
                            View view1 = getLayoutInflater().inflate(R.layout.comment_options,null);
                            builder.setView(view1);
                            dComment = (Button) view1.findViewById(R.id.deletecomment);
                            rComment = (Button) view1.findViewById(R.id.reportcomment);
                            dComment.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            });

                            Dialog dialog = builder.create();
                            dialog.show();

                        }
                        else
                            {
                                View view1 = getLayoutInflater().inflate(R.layout.commentoptionsother,null);
                                builder.setView(view1);
                                rComment = (Button) findViewById(R.id.reportcomment1);
                                Dialog dialog = builder.create();
                                dialog.show();
                            }

                        return false;
                    }
                });

            }

            @NonNull
            @Override
            public commentViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_comments_layout, viewGroup, false);
                return new commentViewholder(view);
            }
        };
        CommentList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseRecyclerAdapter != null){
        firebaseRecyclerAdapter.stopListening();}
    }

    public static class commentViewholder extends RecyclerView.ViewHolder {
        View mView;

        public commentViewholder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setUsername(String username) {
            TextView myusername = (TextView) mView.findViewById(R.id.comment_user_name);
            myusername.setText(username);
        }

        public void setComment(String comment) {
            TextView mycomment = (TextView) mView.findViewById(R.id.comment_text);
            mycomment.setText(comment);
        }

        public void setDate(String date) {
            TextView mydate = (TextView) mView.findViewById(R.id.comment_date);
            mydate.setText(date);
        }

        public void setTime(String time) {
            TextView mytime = (TextView) mView.findViewById(R.id.comment_time);
            mytime.setText(time);
        }

        public void setProfileImage(Context ctx, String profileImage) {
            CircleImageView profile = mView.findViewById(R.id.comment_user_pic);
            Picasso.with(ctx).load(profileImage).placeholder(R.drawable.profile).into(profile);
        }
    }

    private void validateComment(String username) {
        String commentText = commentInput.getText().toString();

        if (TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this,"Please enter the comment to post",Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingbar.setTitle("Comment");
            loadingbar.setMessage("Please wait while we update your comment");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();
            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
            final String saveCurrentDate = currentDate.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH-mm");
            final String saveCurrentTime = currentTime.format(callForTime.getTime());

            final String randomKey = current_user_id + saveCurrentDate + saveCurrentTime;

            HashMap commentmap = new HashMap();
            commentmap.put("UID", current_user_id);
            commentmap.put("ProfileImage",profileimage);
            commentmap.put("Comment",commentText);
            commentmap.put("Date",saveCurrentDate);
            commentmap.put("Time",saveCurrentTime);
            commentmap.put("Username",username);

            Postref.child(randomKey).updateChildren(commentmap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {   loadingbar.dismiss();
                        Toast.makeText(CommentsActivity.this,"Comment Updated",Toast.LENGTH_SHORT).show();
                    }
                    else
                        {
                            loadingbar.dismiss();
                            Toast.makeText(CommentsActivity.this,"Comment failed, Please try again.",Toast.LENGTH_SHORT).show();
                        }
                }
            });
        }
    }

}