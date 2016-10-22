package com.iph.directly.presenter;

import android.text.TextUtils;

import com.iph.directly.domain.DirectionRepository;
import com.iph.directly.domain.LocationRepository;
import com.iph.directly.domain.ToiletRepository;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;
import com.iph.directly.view.ToiletListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by vanya on 10/8/2016.
 */

public class ToiletListPresenter {
    private final DirectionRepository directionRepository;
    private ToiletListView toiletListView;
    private ToiletRepository toiletRepository;
    private LocationRepository locationRepository;

    private Location location;

    private Subscription currentToiletSubscription;
    private Subscription currentLocationSubscription;
    private List<Toilet> toilets = new ArrayList<>();

    public ToiletListPresenter(ToiletListView toiletListView, ToiletRepository toiletRepository, LocationRepository locationRepository, DirectionRepository directionRepository, Location location) {
        this.toiletListView = toiletListView;
        this.toiletRepository = toiletRepository;
        this.locationRepository = locationRepository;
        this.location = location;
        this.directionRepository = directionRepository;
    }

    public void start() {
        toiletListView.showProgress();
        if (toilets.isEmpty()) {
            currentToiletSubscription = toiletRepository.getToilets(location).subscribe(toilets -> {
                toiletListView.hideProgress();
                if (toilets == null || toilets.isEmpty()) {
                    toiletListView.showEmptyView();
                } else {
                    this.toilets = toilets;
                    toiletListView.showToiletList(toilets);

                    loadDistances();
                }
            }, throwable -> {
                toiletListView.hideProgress();
                Timber.e(throwable.getMessage(), throwable);
            });
        } else {
            toiletListView.showToiletList(toilets);
        }
    }

    private void loadDistances() {
        currentLocationSubscription = Observable.from(toilets)
                .flatMap(toilet -> locationRepository.initPlaceId(location, toilet))
                .flatMap(toilet -> toilet.getPlaceId() != null ? directionRepository.initDistanceToToilet(ToiletListPresenter.this.location, toilet) : Observable.just(toilet))
                .toList()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(toilets -> {
                    Collections.sort(toilets, (toilet1, toilet2) -> {
                        if (toilet2.getDistance() == 0) {
                            return -1;
                        } else if (toilet1.getDistance() == 0){
                            return 1;
                        } else {
                            return toilet1.getDistance() - toilet2.getDistance();
                        }
                    });
                    this.toilets = toilets;
                    toiletListView.updateToiletPositionInList(toilets);
                }, throwable -> {
                    Timber.e(throwable.getMessage(), throwable);
                });
    }

    public void stop() {
        if (currentToiletSubscription != null) {
            currentToiletSubscription.unsubscribe();
            currentToiletSubscription = null;
        }

        if (currentLocationSubscription != null) {
            currentLocationSubscription.unsubscribe();
            currentLocationSubscription = null;
        }
    }

    public void toiletChoose(Toilet toilet) {
        if (TextUtils.isEmpty(toilet.getPlaceId())) {
            toiletListView.navigateToMapsApp(toilet);
        } else {
            toiletListView.navigateToDirection(toilet, location);
        }
    }
}
