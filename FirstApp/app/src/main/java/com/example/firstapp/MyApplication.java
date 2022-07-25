package com.example.firstapp;

import android.app.Application;
import android.content.Context;

/*
 * MyApplication.java
 *
 * Class Description: Helper class, returns context of the application
 *                    that is required for some methods.
 * Class Invariant: -
 *
 */

public class MyApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
