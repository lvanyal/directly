package com.iph.directly;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.directly.iph.directly.BuildConfig;

import io.fabric.sdk.android.Fabric;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;
import timber.log.Timber;

/**
 * Created by vanya on 10/8/2016.
 */

public class DirectlyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
