package com.iph.directly.presenter;

import com.iph.directly.domain.LocationRepository;
import com.iph.directly.domain.LocationRepositoryMockImpl;
import com.iph.directly.domain.model.Location;
import com.iph.directly.view.LoadingView;

import rx.Subscription;
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
        loadingView.showProgress(0);
        currentSubscription = locationRepository.getCurrentLocation().subscribe(location -> {
            loadingView.navigateToToilets(location);
        }, throwable -> {
            Timber.e(throwable.getMessage(), throwable);
        });
    }

    public void stop() {
        if (currentSubscription != null) {
            currentSubscription.unsubscribe();
            currentSubscription = null;
        }
    }
}
