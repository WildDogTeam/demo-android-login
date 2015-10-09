package com.wilddog.samples.logindemo;

import android.app.Application;
import com.wilddog.client.Wilddog;

/**
 * Created by Jeen on 2015/9/24.
 *
 * Initialize Wilddog with the application context. This must happen before the client is used.
 */

public class LoginDemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Wilddog.setAndroidContext(this);
    }
}
