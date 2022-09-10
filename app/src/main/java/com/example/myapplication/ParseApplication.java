package com.example.myapplication;

import android.app.Application;

import com.example.myapplication.models.Post;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Post.class);
//        ParseObject.registerSubclass(Post.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("WsjCFSRCMtYqCU0ddPrhYJZ28MBdtTDHCBG8AmY9")
                .clientKey("sNQRburR6Vr8DsWMJIkMz1Bbf8fOP2v6P1S4a3Ch")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
