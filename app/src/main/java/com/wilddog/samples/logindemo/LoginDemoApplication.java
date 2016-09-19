package com.wilddog.samples.logindemo;

import android.app.Application;

import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;


public class LoginDemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //TODO change to your app url
        String loginUrl=getString(R.string.APP_ID);
        WilddogOptions wilddogOptions=new WilddogOptions.Builder().setSyncUrl(loginUrl).build();
        WilddogApp wilddogApp= WilddogApp.initializeApp(this,wilddogOptions);
    }
}
