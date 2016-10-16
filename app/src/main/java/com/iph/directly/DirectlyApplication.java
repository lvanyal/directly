package com.iph.directly;

import android.app.Application;
import android.content.Context;

import com.directly.iph.directly.BuildConfig;

import timber.log.Timber;

/**
 * Created by vanya on 10/8/2016.
 */

public class DirectlyApplication extends Application {

    private static Application application;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
