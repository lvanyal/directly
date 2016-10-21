package com.iph.directly.presenter;

import com.iph.directly.domain.DirectionRepository;
import com.iph.directly.domain.LocationRepository;
import com.iph.directly.domain.ToiletRepository;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;
import com.iph.directly.view.ToiletListView;

import java.util.ArrayList;
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
                .flatMap(toilet -> directionRepository.initDistanceToToilet(ToiletListPresenter.this.location, toilet))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(toilet -> {
                    toiletListView.updateToiletPositionInList(toilet);
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
        toiletListView.navigateToDirection(toilet, location);
    }
}
