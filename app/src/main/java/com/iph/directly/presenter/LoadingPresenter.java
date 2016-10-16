package com.iph.directly.presenter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.iph.directly.domain.LocationRepository;
import com.iph.directly.domain.model.Location;
import com.iph.directly.view.LoadingView;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by vanya on 10/8/2016.
 */

public class LoadingPresenter {
    private LoadingView loadingView;

    private LocationRepository locationRepository;

    private Subscription currentSubscription;

    public LoadingPresenter(LoadingView loadingView, LocationRepository locationRepository) {
        this.loadingView = loadingView;
        this.locationRepository = locationRepository;
    }

    public void start() {
        loadingView.showProgress();
        currentSubscription = locationRepository.getCurrentLocation()
                .observeOn(AndroidSchedulers.mainThread()).subscribe(location -> {
            loadingView.navigateToToilets(location);
        }, throwable -> {
            if (throwable instanceof SecurityException) {
                loadingView.showRequestLocationPermission();
            } else
            Timber.e(throwable.getMessage(), throwable);
        });
    }

    public void stop() {
        if (currentSubscription != null) {
            currentSubscription.unsubscribe();
            currentSubscription = null;
        }
    }

    public void locationPermissionSuccess() {
        start();
    }

    public void locationPermissionFailed() {
        loadingView.navigateToToilets(null);
    }
}
