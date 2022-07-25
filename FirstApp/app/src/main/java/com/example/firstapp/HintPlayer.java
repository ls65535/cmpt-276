package com.example.firstapp;

import android.app.Activity;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.widget.Button;

import java.util.ArrayList;

/*
 * HintPlayer.java
 *
 * Class Description: Plays the audio hint and displays the text hint.
 * Class Invariant: Uses MediaPlayer to play the mp3 hint file.
 *                  Uses Toast to show the hint given as a String.
 *
 */

public class HintPlayer {
    private final Activity activity;
    private MediaPlayer player; // Takes a lot of resources, create only where necessary

    public HintPlayer(Activity activity, ArrayList<QuestionSet> quizModalArrayList) {
        this.activity = activity;
    }

    // Description: Plays hint along with remaining options
    // Precondition: -
    // Postcondition: Hint and remaning options are played
    public void startPlayer(MobileTTS mobileTTS, TextToSpeech mTTS, int questionSetPosition, int questionPosition, Button firstBtn, Button secondBtn, Button thirdBtn, Button fourthBtn, int[] optionNumber) {
        // TODO: Fetch audio hint from database
        //player = MediaPlayer.create(activity, quizModalArrayList.get(questionSetPosition).getQuestionArray(questionPosition).getHint()))
        player = MediaPlayer.create(activity, R.raw.hint15);
        player.start();
        player.setOnCompletionListener(mp -> {
            stopPlayer();
            mobileTTS.TTSQuestionButton(mTTS, firstBtn, 1, questionSetPosition, questionPosition, optionNumber);
            mobileTTS.TTSQuestionButton(mTTS, secondBtn, 2, questionSetPosition, questionPosition, optionNumber);
            mobileTTS.TTSQuestionButton(mTTS, thirdBtn, 3, questionSetPosition, questionPosition, optionNumber);
            mobileTTS.TTSQuestionButton(mTTS, fourthBtn, 4, questionSetPosition, questionPosition, optionNumber);
        });
    }

    // Description: Deallocates resources allocated for mediaPlayer
    // Precondition: mediaPlayer exists
    // Postcondition: Resources are released
    public void stopPlayer() {
        if (player != null)
            player.release();
    }
}
