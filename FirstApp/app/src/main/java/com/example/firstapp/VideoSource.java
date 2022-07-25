package com.example.firstapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class VideoSource extends AppCompatActivity {
    private Button opbtn;
    private Button choseBtn;
    private Button clobtn;
    private VideoView surfaceView;
    private EditText txtView;
    private SurfaceHolder surfaceHolder;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    static final int CHOSE_VIDEO_CAPTURE=2;
    SharedPreferences videoData;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_source);
        opbtn = (Button) this.findViewById(R.id.button1);
        choseBtn= (Button) this.findViewById(R.id.button2);
        videoData = getSharedPreferences("videoData", Context.MODE_PRIVATE);
        txtView=findViewById(R.id.quesEdit);

        choseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
                startActivityForResult(intent, CHOSE_VIDEO_CAPTURE);
            }
        });
        opbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                }else {
                    Log.e("tag", "else.. " );
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {


        String qid = txtView.getText().toString();
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            Cursor cursor = getContentResolver().query(videoUri, null, null,

                    null, null);

            cursor.moveToFirst();


            videoData.edit().putString(qid,cursor.getString(1));
            Log.e("save path", videoUri.getPath());


            Log.e("save qid.",qid);


            // surfaceView.start();
        }else if (requestCode==CHOSE_VIDEO_CAPTURE && resultCode==RESULT_OK)
        {
            Uri uri = intent.getData();

            Cursor cursor = getContentResolver().query(uri, null, null,

                    null, null);

            cursor.moveToFirst();

            videoData.edit().putString(qid,cursor.getString(1));

            Log.e("save path.", uri.getPath());

            Log.e("save qid.",qid);
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    protected void on(int requestCode, int resultCode, Intent intent) {

    }



}
