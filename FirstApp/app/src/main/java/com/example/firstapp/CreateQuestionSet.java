package com.example.firstapp;

import static java.lang.Integer.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/*
 * CreateQuestionSet.java
 *
 * Class Description: UI to edit existing Question Sets.
 * Class Invariant: Updates the list automatically on any change in Firebase
 *
 */

public class CreateQuestionSet extends AppCompatActivity {

    private LinearLayout container;
    private DatabaseReference databaseReference;
    private int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_question_set);
        Button addQuestion = findViewById(R.id.question_set_button);
        databaseReference = FirebaseDatabase.getInstance().getReference("questionSets");
        addQuestion.setOnClickListener(v -> {
            DatabaseReference ref = databaseReference.child("counter");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    counter = snapshot.getValue(Integer.class);
                    databaseReference.child("counter").setValue(++counter);
                    System.out.println("Counter is " + counter);
                    QuestionSet set = new QuestionSet(null, null, null, null, null, "", "", 0, false, 50);
                    databaseReference.child(counter + "").setValue(set);
                    Intent moveToWriteQuestions = new Intent(CreateQuestionSet.this, CreateQuestions.class);
                    moveToWriteQuestions.putExtra("id", counter);
                    CreateQuestionSet.this.startActivity(moveToWriteQuestions);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("Database Error", "loadPost:onCancelled", error.toException());
                }
            });
        });
        showcaseQuestions();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    // Return to previous Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Description: A list of questions that are in User's database,
    // Precondition: Database exists
    // Postcondition: Shows all questions in the linear layout and allows user
    //                to call WriteQuestions in order to modify them
    private void showcaseQuestions() {
        container = findViewById(R.id.questions_list);
        // TODO: Add all QuestionSets from database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                container.removeAllViews();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Check if dataSnapshot is not 'counter' since it is the only child
                    // that is not of class QuestionSet
                    if (!Objects.equals(dataSnapshot.getKey(), "counter")) {
                        System.out.println("The key is " + dataSnapshot.getKey());
                        ImageView imageView = new ImageView(CreateQuestionSet.this);
                        imageView.setAdjustViewBounds(true);
                        // Include question id into extra
                        int ting = parseInt(Objects.requireNonNull(dataSnapshot.getKey()));
                        imageView.setOnClickListener(v -> {
                            Intent moveToWriteQuestions = new Intent(CreateQuestionSet.this, CreateQuestions.class);
                            moveToWriteQuestions.putExtra("id", ting);
                            CreateQuestionSet.this.startActivity(moveToWriteQuestions);
                        });
                        // TODO: Fetch image from database
                        // Hard-coded images for testing purposes
                        switch (ting) {
                            case 1:
                                imageView.setImageResource(R.drawable.george_wilson_chinese);
                                break;
                            case 2:
                                imageView.setImageResource(R.drawable.george_wilson_french);
                                break;
                            case 4:
                                imageView.setImageResource(R.drawable.gift);
                                break;
                            case 5:
                                imageView.setImageResource(R.drawable.q3picture);
                                break;
                            default:
                                imageView.setImageResource(R.drawable.george_wilson);
                                break;
                        }
                        container.addView(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}