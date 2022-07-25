package com.example.firstapp;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Objects;

/*
 * Uploader.java
 *
 * Class Description: Uploads media files with certain naming convention based on extension.
 * Class Invariant: Images,
 *                  Videos,
 *                  Audio.
 *
 */

public class Uploader {
    private String typeOfFile;
    private final Context context;
    private final ImageView imageCover;
    private final DatabaseReference databaseRef;
    private final StorageReference storageRef;

    public Uploader(Context context, ImageView imageCover, DatabaseReference databaseRef, StorageReference storageRef) {
        this.context = context;
        this.imageCover = imageCover;
        this.databaseRef = databaseRef;
        this.storageRef = storageRef;
    }

    public void uploadFile(Uri uri, int questionId, int questionSetId) {
        if (uri != null) {
            // Determine whether video, audio, or image
            switch (getFileExtension(uri)) {
                case "png":
                case "jpg":
                case "jpeg":
                    typeOfFile = "image";
                    break;
                case "mp4":
                    typeOfFile = "video";
                    break;
                case "mp3":
                    typeOfFile = "audio";
                default:
                    Toast.makeText(context, "Some error I don't even know what's going on", Toast.LENGTH_SHORT).show();
            }
            StorageReference fileReference;
            System.out.println("The extension inside Uploader is " + getFileExtension(uri));
            if (Objects.equals(typeOfFile, "audio"))
                fileReference = storageRef.child("hint" + questionSetId + questionId + "." + getFileExtension(uri));
            else
                fileReference = storageRef.child(typeOfFile + questionSetId + "." + getFileExtension(uri));
            fileReference.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(context, R.string.uploaded, Toast.LENGTH_SHORT).show();
                        if (Objects.equals(typeOfFile, "image"))
                            imageCover.setImageURI(uri);
                        if (Objects.equals(typeOfFile, "audio"))
                            databaseRef.child("q"+questionId).child("hint").setValue(fileReference.getDownloadUrl().toString());
                        else
                            databaseRef.child(typeOfFile).setValue(fileReference.getDownloadUrl().toString());
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, R.string.upload_failed, Toast.LENGTH_SHORT).show())
                    .addOnProgressListener(snapshot -> Toast.makeText(context, R.string.uploading, Toast.LENGTH_SHORT).show());
        }
    }

    public String getFileExtension(Uri uri) {
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT))
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(uri));
        else
            return MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
    }
}
