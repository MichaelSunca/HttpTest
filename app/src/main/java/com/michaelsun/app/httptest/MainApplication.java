package com.michaelsun.app.httptest;

import android.app.Application;

public class MainApplication extends Application {


    private static MainApplication mainApplication;

    @Override
    public void onCreate() {
        super.onCreate();
    }


    public static MainApplication getMainApplication(){
        return mainApplication;
    }
}
