package com.aaupush.aaupush;

import android.app.Application;

import com.tsengvn.typekit.Typekit;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Typekit.getInstance()
                .addNormal(Typekit.createFromAsset(this, "Regular.otf"))
                .addBold(Typekit.createFromAsset(this, "Bold.otf"))
                .addItalic(Typekit.createFromAsset(this, "LightItalic.otf"))
                .addBoldItalic(Typekit.createFromAsset(this, "RegularItalic.otf"));
    }
}
