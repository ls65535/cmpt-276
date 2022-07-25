package com.example.firstapp;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

/*
 * FirstFragment.java
 *
 * Class Description: Application quiz system.
 * Class Invariant: Biased randomised ordering
 *                  Firebase can't be empty
 *
 */

public class FirstFragment extends Fragment {

    private TextView questionTV;
    private ImageView questionImage;
    private Button firstBtn, secondBtn, thirdBtn, fourthBtn, menuBtn, nextQuestionBtn, nextQuestionSetBtn, restartQuiz;
    private ArrayList<QuestionSet> quizModalArrayList, changesInWeights;
    private TextToSpeech mobileTTS;
    private MobileTTS mTTS;
    private HintPlayer player;
    private Romanisation r;
    private int questionSetPosition = 0, questionPosition = 0;
    private boolean firstIncorrect = false, secondIncorrect = false, firstFetch = true;
    private int[] optionNumber = new int[]{1, 2, 3, 4};

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the Layout for this Fragment
        View fragmentFirstLayout = inflater.inflate(R.layout.fragment_first, container, false);
        // Get the Count and Question TextView
        questionTV = fragmentFirstLayout.findViewById(R.id.question);
        // Get the Buttons
        firstBtn = fragmentFirstLayout.findViewById(R.id.first_choice);
        secondBtn = fragmentFirstLayout.findViewById(R.id.second_choice);
        thirdBtn = fragmentFirstLayout.findViewById(R.id.third_choice);
        fourthBtn = fragmentFirstLayout.findViewById(R.id.fourth_choice);
        menuBtn = fragmentFirstLayout.findViewById(R.id.back_to_menu);
        nextQuestionBtn = fragmentFirstLayout.findViewById(R.id.get_next_question);
        nextQuestionSetBtn = fragmentFirstLayout.findViewById(R.id.get_next_question_set);
        restartQuiz = fragmentFirstLayout.findViewById(R.id.restart_quiz);
        // Get the Image and set resolution
        questionImage = fragmentFirstLayout.findViewById(R.id.imageView);
        questionImage.getLayoutParams().height = Resources.getSystem().getDisplayMetrics().widthPixels;
        // Initialise romanisation
        r = new Romanisation(MyApplication.getAppContext());
        // Get the Questions
        quizModalArrayList = new ArrayList<>();
        changesInWeights = new ArrayList<>();
        questionTV.setText(r.input(getString(R.string.loading), getActivity()));
        fetchQuestions();
        // Give buttons optionAction
        firstBtn.setOnClickListener(v -> optionAction(quizModalArrayList, firstBtn));
        secondBtn.setOnClickListener(v -> optionAction(quizModalArrayList, secondBtn));
        thirdBtn.setOnClickListener(v -> optionAction(quizModalArrayList, thirdBtn));
        fourthBtn.setOnClickListener(v -> optionAction(quizModalArrayList, fourthBtn));

        // Set colour for all buttons (temporary fix, need to change colours in selector)
        firstBtn.setTextColor(Color.parseColor("#ffffff"));
        secondBtn.setTextColor(Color.parseColor("#ffffff"));
        thirdBtn.setTextColor(Color.parseColor("#ffffff"));
        fourthBtn.setTextColor(Color.parseColor("#ffffff"));
        // Call mTTS on Long Click
        firstBtn.setOnLongClickListener(v -> {
            player.stopPlayer();
            mobileTTS.stop();
            replaceColours();
            mTTS.TTSQuestionButtonLongClick(mobileTTS, firstBtn, 1, questionSetPosition, questionPosition, optionNumber);
            return true;
        });
        secondBtn.setOnLongClickListener(v -> {
            player.stopPlayer();
            mobileTTS.stop();
            replaceColours();
            mTTS.TTSQuestionButtonLongClick(mobileTTS, secondBtn, 2, questionSetPosition, questionPosition, optionNumber);
            return true;
        });
        thirdBtn.setOnLongClickListener(v -> {
            player.stopPlayer();
            mobileTTS.stop();
            replaceColours();
            mTTS.TTSQuestionButtonLongClick(mobileTTS, thirdBtn, 3, questionSetPosition, questionPosition, optionNumber);
            return true;
        });
        fourthBtn.setOnLongClickListener(v -> {
            player.stopPlayer();
            mobileTTS.stop();
            replaceColours();
            mTTS.TTSQuestionButtonLongClick(mobileTTS, fourthBtn, 4, questionSetPosition, questionPosition, optionNumber);
            return true;
        });
        // Go back to MainMenu
        menuBtn.setOnClickListener(v -> NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_MainMenuFragment));
        // Enable all option buttons after getting next question
        nextQuestionBtn.setOnClickListener(v -> {
            firstBtn.setEnabled(true);
            secondBtn.setEnabled(true);
            thirdBtn.setEnabled(true);
            fourthBtn.setEnabled(true);
            setDataToViews();
        });

        nextQuestionSetBtn.setOnClickListener(v -> setDataToViews());

        questionTV.setOnClickListener(v -> {
            mobileTTS.stop();
            if (quizModalArrayList.size() == 0)
                mTTS.speak(mobileTTS, getString(R.string.answered_all_questions));
            else
                mTTS.speak(mobileTTS, quizModalArrayList.get(questionSetPosition).getQuestionArray(questionPosition).getQuestion());
        });

        restartQuiz.setOnClickListener(v -> {
            Toast.makeText(MyApplication.getAppContext(), "Pressed", Toast.LENGTH_SHORT).show();
            System.out.println("Restart called");
        });

        return fragmentFirstLayout;
    }

    // Description: Updates Image, Options, QuestionTV after new Question is chosen.
    // Precondition: -
    // Postcondition: TextViews and Image are updated.
    private void setDataToViews() {
        optionNumber = new int[]{1,2,3,4};
        nextQuestionSetBtn.setVisibility(View.GONE);
        nextQuestionBtn.setVisibility(View.GONE);
        firstBtn.setVisibility(View.VISIBLE);
        secondBtn.setVisibility(View.VISIBLE);
        thirdBtn.setVisibility(View.VISIBLE);
        fourthBtn.setVisibility(View.VISIBLE);
        questionTV.setVisibility(View.VISIBLE);
        questionImage.setVisibility(View.VISIBLE);
        firstIncorrect = false;
        secondIncorrect = false;
        if (questionPosition == 5 || quizModalArrayList.size() == 0) {
            if (quizModalArrayList.size() == 0) {
                firstBtn.setVisibility(View.GONE);
                secondBtn.setVisibility(View.GONE);
                thirdBtn.setVisibility(View.GONE);
                fourthBtn.setVisibility(View.GONE);
                questionImage.setVisibility(View.INVISIBLE);
                questionTV.setText(r.input(getString(R.string.answered_all_questions), getActivity()));
                mTTS.speak(mobileTTS, getString(R.string.answered_all_questions));
                menuBtn.setText(r.input(getString(R.string.back_to_menu), getActivity()));
                menuBtn.setVisibility(View.VISIBLE);
                restartQuiz.setVisibility(View.VISIBLE);
                return;
            }
            finishedQuestionSet();
            return;
        }
        // TODO: Fetch image from database
        //quizModalArrayList.get(questionSetPosition).getImage()
        // Hard-coded images for testing purposes
        switch (quizModalArrayList.get(questionSetPosition).getId()) {
            case "1":
                questionImage.setImageResource(R.drawable.george_wilson_chinese);
                break;
            case "2":
                questionImage.setImageResource(R.drawable.george_wilson_french);
                break;
            case "4":
                questionImage.setImageResource(R.drawable.gift);
                break;
            case "5":
                questionImage.setImageResource(R.drawable.q3picture);
                break;
            default:
                questionImage.setImageResource(R.drawable.george_wilson);
                break;
        }
        firstBtn.setText(r.input(quizModalArrayList.get(questionSetPosition).getQuestionArray(questionPosition).getOptions(0), getActivity()));
        secondBtn.setText(r.input(quizModalArrayList.get(questionSetPosition).getQuestionArray(questionPosition).getOptions(1), getActivity()));
        thirdBtn.setText(r.input(quizModalArrayList.get(questionSetPosition).getQuestionArray(questionPosition).getOptions(2), getActivity()));
        fourthBtn.setText(r.input(quizModalArrayList.get(questionSetPosition).getQuestionArray(questionPosition).getOptions(3), getActivity()));
        questionTV.setText(r.input(quizModalArrayList.get(questionSetPosition).getQuestionArray(questionPosition).getQuestion(), getActivity()));
        mTTS.TTSQuestion(mobileTTS, player, questionSetPosition, questionPosition, firstIncorrect, secondIncorrect, optionNumber);
    }

    // Description: Reduces number of Options, provides Hint,
    //              sets next Question.
    // Precondition: -
    // Postcondition: Fam I don't even know what to write here atm
    private void optionAction(@NonNull ArrayList<QuestionSet> quizModalArrayList, @NonNull Button button) {
        player.stopPlayer();
        mobileTTS.stop();
        replaceColours();
        if (quizModalArrayList.get(questionSetPosition).getQuestionArray(questionPosition).getAnswer() == getButtonPosition(button)) {
            addErrTimeQuestion(false);
            displayCorrectAnswer();
        } else if (!firstIncorrect && !secondIncorrect) {
            addErrTimeQuestion(true);
            firstIncorrect = true;
            button.setVisibility(View.GONE);
            mTTS.TTSQuestion(mobileTTS, player, questionSetPosition, questionPosition, firstIncorrect, secondIncorrect, optionNumber);
        } else if (firstIncorrect && !secondIncorrect) {
            addErrTimeQuestion(true);
            secondIncorrect = true;
            button.setVisibility(View.GONE);
            mTTS.TTSQuestion(mobileTTS, player, questionSetPosition, questionPosition, firstIncorrect, secondIncorrect, optionNumber);
        } else {
            addErrTimeQuestion(true);
            displayCorrectAnswer();
        }
    }

    // Description: Returns the index of button's text in Question
    //              Helper function to compare Integers instead of Strings
    // Precondition: -
    // Postcondition: Index is returned
    private int getButtonPosition(Button button) {
        if (button == firstBtn) return 0;
        if (button == secondBtn) return 1;
        if (button == thirdBtn) return 2;
        else return 3;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // Description: Displays the correct answer to the user
    // Precondition: Answered correctly or 2 times incorrectly
    // Postcondition: Sets visibility of incorrect buttons to GONE
    //                Disables ClickListener of the buttons
    //                Sets visibility of the nextQuestionBtn to VISIBLE
    private void displayCorrectAnswer() {
        firstBtn.setEnabled(false);
        secondBtn.setEnabled(false);
        thirdBtn.setEnabled(false);
        fourthBtn.setEnabled(false);
        if (quizModalArrayList.get(questionSetPosition).getQuestionArray(questionPosition).getAnswer() != getButtonPosition(firstBtn))
            firstBtn.setVisibility(View.GONE);
        if (quizModalArrayList.get(questionSetPosition).getQuestionArray(questionPosition).getAnswer() != getButtonPosition(secondBtn))
            secondBtn.setVisibility(View.GONE);
        if (quizModalArrayList.get(questionSetPosition).getQuestionArray(questionPosition).getAnswer() != getButtonPosition(thirdBtn))
            thirdBtn.setVisibility(View.GONE);
        if (quizModalArrayList.get(questionSetPosition).getQuestionArray(questionPosition).getAnswer() != getButtonPosition(fourthBtn))
            fourthBtn.setVisibility(View.GONE);
        nextQuestionBtn.setVisibility(View.VISIBLE);
        nextQuestionBtn.setText(r.input(getString(R.string.tap_to_continue), getActivity()));
        ++questionPosition;
    }

    // Description: Displays the question video at the end of the question set
    // Precondition: All questions of the question set have been answered
    // Postcondition: Option buttons, question textview, question image disappear
    //                nextQuestion button appears
    //                plays the question video on loop until the nextQuestion is pressed
    private void finishedQuestionSet() {
        // TODO: Fetch video from database
        // quizModalArrayList.get(questionSetPosition).getVideo()
        int video = R.raw.piano;
        Intent intent = new Intent(getActivity(), QuestionVideo.class);
        intent.putExtra("video", video);
        startActivity(intent);
        firstBtn.setVisibility(View.GONE);
        secondBtn.setVisibility(View.GONE);
        thirdBtn.setVisibility(View.GONE);
        fourthBtn.setVisibility(View.GONE);
        questionTV.setVisibility(View.GONE);
        questionImage.setVisibility(View.GONE);
        nextQuestionSetBtn.setVisibility(View.VISIBLE);
        nextQuestionSetBtn.setText(r.input(getString(R.string.next_question), getActivity()));
        changesInWeights.add(quizModalArrayList.remove(questionSetPosition));
        if (quizModalArrayList.size() != 0)
            questionSetPosition = getRandomQuestion();
        questionPosition = 0;
    }

    // Description: Uses biased randomisation to get next question set
    // Precondition: quizModalArrayList is not empty
    // Postcondition: Next question set's index is returned
    private int getRandomQuestion() {
        RandomAlgorithm<Integer> rc = new RandomAlgorithm<>();
        for (int i = 0; i < quizModalArrayList.size(); i++)
            rc.add(quizModalArrayList.get(i).getWeight(), i);
        return rc.next();
    }

    // Description: Adjusts the weights of the question sets
    // Precondition: The option button is pressed
    // Postcondition: If correct option decreases weight by 5
    //                Else increases weight by 5
    private void addErrTimeQuestion(boolean increase) {
        int weight = 5;
        if (increase)
            quizModalArrayList.get(questionSetPosition).setWeight(quizModalArrayList.get(questionSetPosition).getWeight()+weight);
        else
            quizModalArrayList.get(questionSetPosition).setWeight(quizModalArrayList.get(questionSetPosition).getWeight()-weight);
        System.out.println("Adjusted weight: " + quizModalArrayList.get(questionSetPosition).getWeight());
    }

    // Description: Populates quizModalArray with complete QuestionSets from database
    // Precondition: -
    // Postcondition: The QuestionSets' weights in database are replaced with new ones
    private void fetchQuestions() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("questionSets");
        // TODO: Add all QuestionSets from database
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (firstFetch) {
                    firstFetch = false;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        // Check if dataSnapshot is not 'counter' since it is the only child
                        // that is not of class QuestionSet
                        if (!Objects.equals(dataSnapshot.getKey(), "counter")) {
                            QuestionSet questionSet = dataSnapshot.getValue(QuestionSet.class);
                            assert questionSet != null;
                            if (questionSet.getComplete()) {
                                questionSet.setId(dataSnapshot.getKey());
                                quizModalArrayList.add(questionSet);
                            }
                        }
                    }
                    firstBtn.setVisibility(View.VISIBLE);
                    secondBtn.setVisibility(View.VISIBLE);
                    thirdBtn.setVisibility(View.VISIBLE);
                    fourthBtn.setVisibility(View.VISIBLE);
                    player = new HintPlayer(getActivity(), quizModalArrayList);
                    // Initialise TextToSpeech
                    mTTS = new MobileTTS(getActivity(), MyApplication.getAppContext(), quizModalArrayList, firstBtn, secondBtn, thirdBtn, fourthBtn, r);
                    questionSetPosition = getRandomQuestion();
                    mobileTTS = mTTS.initialiseTextToSpeechEngine(questionSetPosition, questionPosition);
                    setDataToViews();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Description: Resets the colours of the buttons after TTS showcase
    // Precondition: -
    // Postcondition: Buttons' colours are returned to their original state
    // Exception: Avoid the presence of other buttons that have not yet finished playing
    //            or whose colors have not yet returned to their original colors
    private void replaceColours() {
        firstBtn.setBackgroundColor(ContextCompat.getColor(MyApplication.getAppContext(), R.color.button_background));
        secondBtn.setBackgroundColor(ContextCompat.getColor(MyApplication.getAppContext(), R.color.button_background));
        thirdBtn.setBackgroundColor(ContextCompat.getColor(MyApplication.getAppContext(), R.color.button_background));
        fourthBtn.setBackgroundColor(ContextCompat.getColor(MyApplication.getAppContext(), R.color.button_background));
    }

    // Description: Disconnects from TTS engine
    // Precondition: mTTS exists
    // Postcondition: App disconnected from TTS engine
    @Override
    public void onDestroyView() {
        // TODO: Push back new weights into database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("questionSets");
        for (int i = 0; i < changesInWeights.size(); i++) {
            System.out.println("Pushing new weights");
            System.out.println(changesInWeights.get(i).getId() + ": " + changesInWeights.get(i).getWeight());
        }
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                while (!changesInWeights.isEmpty()) {
                    DatabaseReference addQuestionSet = ref.child(changesInWeights.get(0).getId()).child("weight");
                    addQuestionSet.setValue(changesInWeights.remove(0).getWeight());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Database Error", "loadPost:onCancelled", error.toException());
            }
        });
        if (mobileTTS != null) {
            mobileTTS.stop();
            mobileTTS.shutdown();
        }
        player.stopPlayer();
        super.onDestroyView();
    }
}