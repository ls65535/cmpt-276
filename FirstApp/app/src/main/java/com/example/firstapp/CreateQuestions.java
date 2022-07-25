package com.example.firstapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

/*
 * CreateQuestions.java
 *
 * Class Description: UI to create new Question Set in the Firebase.
 * Class Invariant: Deletes Question Set if none of the necessary information is given
 *
 */

public class CreateQuestions extends AppCompatActivity implements View.OnClickListener {

    private final int PICK_VIDEO = 1;
    private LinearLayout container;
    private DatabaseReference databaseReference;
    private Uploader uploader;
    private QuestionSet questionSet;
    private int questionSetId = 0, counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_questions);
        Romanisation r = new Romanisation(MyApplication.getAppContext());
        // Get the IDs
        ImageView imageCover = findViewById(R.id.image_cover);
        FloatingActionButton addImage = findViewById(R.id.add_image);
        Button addQuestions = findViewById(R.id.add_questions);
        Button addVideo = findViewById(R.id.add_video);
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            questionSetId = extras.getInt("id");
        // Hard-coded images for testing purposes
        switch (questionSetId) {
            case 1:
                imageCover.setImageResource(R.drawable.george_wilson_chinese);
                break;
            case 2:
                imageCover.setImageResource(R.drawable.george_wilson_french);
                break;
            case 4:
                imageCover.setImageResource(R.drawable.gift);
                break;
            case 5:
                imageCover.setImageResource(R.drawable.q3picture);
                break;
            default:
                imageCover.setImageResource(R.drawable.george_wilson);
                break;
        }
        // Set text
        addQuestions.setText(r.input(getString(R.string.add_questions), this));
        addVideo.setText(r.input(getString(R.string.add_video), this));
        databaseReference = FirebaseDatabase.getInstance().getReference("questionSets").child(questionSetId +"");
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("createQuestions");
        showcaseQuestions();
        uploader = new Uploader(this, imageCover, databaseReference, storageRef);
        addQuestions.setOnClickListener(v -> {
            Intent moveToWriteQuestions = new Intent(CreateQuestions.this, WriteQuestions.class);
            moveToWriteQuestions.putExtra("questionSetId", questionSetId);
            CreateQuestions.this.startActivity(moveToWriteQuestions);
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        imageCover.setOnClickListener(this);
        addImage.setOnClickListener(this);

        // Select Video
        addVideo.setOnClickListener(v -> {
            View customView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialogue_choose, null);
            AppCompatTextView leftPickText = customView.findViewById(R.id.left_pick_text), rightPickText = customView.findViewById(R.id.right_pick_text);
            AppCompatImageView leftPickIcon = customView.findViewById(R.id.left_pick_icon), rightPickIcon = customView.findViewById(R.id.right_pick_icon);
            leftPickText.setText(R.string.title_camera);
            rightPickText.setText(R.string.title_gallery);
            leftPickIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_photo_camera_black_48dp));
            rightPickIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_photo_black_48dp));
            AlertDialog dialogue = new AlertDialog.Builder(CreateQuestions.this)
                    .setTitle(R.string.title_choose)
                    .setView(customView)
                    .setNegativeButton(R.string.action_cancel, null)
                    .setOnCancelListener(null).create();
            dialogue.show();

            // Handle Record option click
            customView.findViewById(R.id.left_pick).setOnClickListener(v1 -> {
                Toast.makeText(this, "This is where we start recording the video innit", Toast.LENGTH_LONG).show();
                dialogue.dismiss();
            });

            // Handle Browse option click
            customView.findViewById(R.id.right_pick).setOnClickListener(v2 -> {
                getVideoByGallery();
                dialogue.dismiss();
            });
        });
    }

    // Return to previous Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final int PICK_IMAGE = 2404;
        final int VIDEO_RECORD_CODE = 101;
        switch(requestCode) {
            case PICK_IMAGE:
            case PICK_VIDEO:
            case VIDEO_RECORD_CODE:
                if (resultCode == RESULT_OK) {
                    assert data != null;
                    uploader.uploadFile(data.getData(), 0, questionSetId);
                }
                break;
        }
    }

    // Description: A list of questions that are in User's database,
    // Precondition: Database exists
    // Postcondition: Shows all questions in the linear layout and allows user
    //                to call WriteQuestions in order to modify them
    private void showcaseQuestions() {
        container = findViewById(R.id.questions_list);
        // TODO: Add all Questions from database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                container.removeAllViews();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Check if dataSnapshot is not 'counter' since it is the only child
                    // that is not of class Question
                    if (!Objects.equals(dataSnapshot.getKey(), "counter") && !Objects.equals(dataSnapshot.getKey(), "image")
                    && !Objects.equals(dataSnapshot.getKey(), "video") && !Objects.equals(dataSnapshot.getKey(), "complete")
                    && !Objects.equals(dataSnapshot.getKey(), "weight")) {
                        System.out.println("The key is " + dataSnapshot.getKey());
                        Question question = dataSnapshot.getValue(Question.class);
                        Button button = new Button(CreateQuestions.this);
                        assert question != null;
                        button.setText(dataSnapshot.getKey() + ". " + question.getQuestion());
                        button.setGravity(Gravity.CENTER);
                        // Include question id into extra
                        int ting = Integer.parseInt(Objects.requireNonNull(dataSnapshot.getKey()).substring(1));
                        button.setOnClickListener(v -> {
                            Intent moveToWriteQuestions = new Intent(CreateQuestions.this, WriteQuestions.class);
                            moveToWriteQuestions.putExtra("questionId", ting);
                            moveToWriteQuestions.putExtra("questionSetId", questionSetId);
                            CreateQuestions.this.startActivity(moveToWriteQuestions);
                        });
                        container.addView(button);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getVideoByGallery() {
        Intent video = new Intent();
        video.setType("video/*");
        video.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(Intent.createChooser(video, "Select Video"), PICK_VIDEO);
    }

    @Override
    public void onClick(View v) {
        View customView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialogue_choose, null);
        AppCompatTextView leftPickText = customView.findViewById(R.id.left_pick_text), rightPickText = customView.findViewById(R.id.right_pick_text);
        AppCompatImageView leftPickIcon = customView.findViewById(R.id.left_pick_icon), rightPickIcon = customView.findViewById(R.id.right_pick_icon);
        leftPickText.setText(R.string.title_camera);
        rightPickText.setText(R.string.title_gallery);
        leftPickIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_photo_camera_black_48dp));
        rightPickIcon.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_photo_black_48dp));
        AlertDialog dialogue = new AlertDialog.Builder(CreateQuestions.this)
                .setTitle(R.string.title_choose)
                .setView(customView)
                .setNegativeButton(R.string.action_cancel, null)
                .setOnCancelListener(null).create();
        dialogue.show();

        // Handle Record option click
        customView.findViewById(R.id.left_pick).setOnClickListener(v1 -> {
            ImagePicker.Companion.with(CreateQuestions.this).cropSquare().cameraOnly().start();
            dialogue.dismiss();
        });

        // Handle Browse option click
        customView.findViewById(R.id.right_pick).setOnClickListener(v2 -> {
            ImagePicker.Companion.with(CreateQuestions.this).cropSquare().galleryOnly().start();
            dialogue.dismiss();
        });
    }

    public void onDestroy() {
        super.onDestroy();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questionSet = snapshot.getValue(QuestionSet.class);
                assert questionSet != null;
                if (questionSet.getCounter() == 5 && questionSet.getImage().isEmpty()
                        && questionSet.getVideo().isEmpty()) {
                    databaseReference.child("complete").setValue(true);
                    Toast.makeText(CreateQuestions.this, R.string.question_set_saved, Toast.LENGTH_SHORT).show();
                } else if (questionSet.getCounter() == 0 && questionSet.getImage().isEmpty()
                        && questionSet.getVideo().isEmpty()) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("questionSets").child("counter");
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            counter = snapshot.getValue(Integer.class);
                            DatabaseReference remove = FirebaseDatabase.getInstance().getReference("questionSets").child(counter+"");
                            remove.child("complete").removeValue();
                            remove.child("counter").removeValue();
                            remove.child("image").removeValue();
                            remove.child("video").removeValue();
                            remove.child("weight").removeValue();
                            ref.setValue(--counter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.w("Database Error", "loadPost:onCancelled", error.toException());
                        }
                    });
                } else {
                    Toast.makeText(CreateQuestions.this, R.string.question_set_draft, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Database Error", "loadPost:onCancelled", error.toException());
            }
        });
    }
}