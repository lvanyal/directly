package com.iph.directly.presenter;

import com.google.android.gms.location.places.Place;
import com.iph.directly.domain.DeviceInfo;
import com.iph.directly.domain.DirectionRepository;
import com.iph.directly.domain.AuthRepository;
import com.iph.directly.domain.FeedbackRepository;
import com.iph.directly.domain.LocationRepository;
import com.iph.directly.domain.StrikeRepository;
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
    private AuthRepository authRepository;
    private StrikeRepository strikeRepository;
    private FeedbackRepository feedbackRepository;

    private Location location;

    private List<Subscription> subscriptions = new ArrayList<>();
    private List<Toilet> toilets = new ArrayList<>();

    private DeviceInfo deviceInfo;

    public ToiletListPresenter(ToiletListView toiletListView
            , ToiletRepository toiletRepository
            , LocationRepository locationRepository
            , DirectionRepository directionRepository
            , AuthRepository authRepository
            , StrikeRepository strikeRepository
            , FeedbackRepository feedbackRepository
            , DeviceInfo deviceInfo
            , Location location) {
        this.toiletListView = toiletListView;
        this.toiletRepository = toiletRepository;
        this.locationRepository = locationRepository;
        this.location = location;
        this.directionRepository = directionRepository;
        this.authRepository = authRepository;
        this.strikeRepository = strikeRepository;
        this.feedbackRepository = feedbackRepository;
        this.deviceInfo = deviceInfo;
    }

    public void start() {
        if (location == null) {
            toiletListView.showNoLocationView();
        } else {
            toiletListView.showCityName(location.getCity());
            if (toilets.isEmpty()) {
                loadToilets();
            } else {
                toiletListView.showToiletList(toilets);
            }
            toiletListView.updateSignInStatus(authRepository.isSignedIn());
        }
    }

    private void loadToilets() {
        Subscription currentToiletSubscription = toiletRepository.getToilets(location)
                .doOnSubscribe(() -> toiletListView.showProgress())
                .subscribe(toilets -> {
                    toiletListView.hideProgress();
                    if (toilets == null || toilets.isEmpty()) {
                        toiletListView.showEmptyView();
                    } else {
                        this.toilets = toilets;
                        toiletListView.showToiletList(toilets);
                        toiletListView.hideLocationNotEnabledView();
                        toiletListView.hideEmptyView();
                        loadDistances();
                    }
                }, throwable -> {
                    toiletListView.hideProgress();
                    toiletListView.hideEmptyView();
                    Timber.e(throwable, throwable.getMessage());
                });
        subscriptions.add(currentToiletSubscription);
    }

    public void enableLocationButtonClick() {
        loadCurrentLocation();
    }

    private void loadCurrentLocation() {
        locationRepository.getCurrentLocation()
                .doOnSubscribe(() -> toiletListView.showProgress())
                .subscribe(location1 -> {
                    this.location = location1;
                    loadToilets();
                    toiletListView.hideLocationNotEnabledView();
                }, throwable -> {
                    toiletListView.hideProgress();
                    Timber.e(throwable, throwable.getMessage());
                    if (throwable instanceof SecurityException) {
                        toiletListView.showRequestLocationPermission();
                    }
                });
    }

    private void loadDistances() {
        Subscription currentLocationSubscription = Observable.from(toilets)
                .flatMap(toilet -> locationRepository.initPlaceId(location, toilet))
                .flatMap(toilet ->
                        toilet.getPlaceId() != null || toilet.getLatitude() != 0 ? directionRepository.initDistanceToToilet(ToiletListPresenter.this.location, toilet) : Observable.just(toilet))
                .toList()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(toilets -> {
                    Collections.sort(toilets, (toilet1, toilet2) -> {
                        if (toilet2.getDistance() == 0) {
                            return -1;
                        } else if (toilet1.getDistance() == 0) {
                            return 1;
                        } else {
                            return toilet1.getDistance() - toilet2.getDistance();
                        }
                    });
                    this.toilets = toilets;
                    toiletListView.updateToiletPositionInList(toilets);
                }, throwable -> {
                    Timber.e(throwable, throwable.getMessage());
                });
        subscriptions.add(currentLocationSubscription);
    }

    public void stop() {
        Observable.from(subscriptions).subscribe(Subscription::unsubscribe);
    }

    public void toiletChoose(Toilet toilet) {
        if (deviceInfo.isMapAppAvailable()) {
            toiletListView.navigateToMapsApp(toilet);
        } else {
            toiletListView.navigateToDirection(toilet, location);
        }
    }

    public void newToiletClicked() {
        if (authRepository.isSignedIn()) {
            toiletListView.navigateToToiletCreation();
        } else {
            toiletListView.navigateToAuth();
        }
    }

    public void onUserSingedIn() {
        toiletListView.navigateToToiletCreation();
    }

    public void loginLogoutPressed() {
        if (authRepository.isSignedIn()) {
            authRepository.signOut();
            toiletListView.updateSignInStatus(false);
        } else {
            toiletListView.navigateToAuth();
        }
    }

    public void toiletContextMenuOpen(Toilet toilet) {
        if (authRepository.isSignedIn()) {
            if (!toilet.hasId()) {
                toilet.generateId();
            }
            Subscription subscription = strikeRepository.isToiletStrikedByUser(toilet.getId(), authRepository.getUserId()).subscribe(alreadyStriked -> {
                        if (alreadyStriked) {
                            toiletListView.showToiletMenu(toilet, ToiletListView.ToiletMenuItem.UNSTRIKE);
                        } else {
                            toiletListView.showToiletMenu(toilet, ToiletListView.ToiletMenuItem.STRIKE);
                        }
                    }, throwable -> toiletListView.hideProgress()
            );
            subscriptions.add(subscription);
        } else {
            toiletListView.navigateToAuth();
        }
    }

    public void toiletMenuItemChosen(Toilet toilet, ToiletListView.ToiletMenuItem toiletMenuItem) {
        switch (toiletMenuItem) {
            case STRIKE:
                Subscription subscription = strikeRepository.putStrike(toilet.getId(), authRepository.getUserId())
                        .doOnSubscribe(() -> toiletListView.showProgress())
                        .subscribe(strike -> {
                                    toiletListView.hideProgress();
                                }
                                , throwable -> toiletListView.hideProgress());
                subscriptions.add(subscription);
                break;
            case UNSTRIKE:
                Subscription subscription1 = strikeRepository.removeStrike(toilet.getId(), authRepository.getUserId())
                        .doOnSubscribe(() -> toiletListView.showProgress())
                        .subscribe(strike -> {
                                    toiletListView.hideProgress();
                                }
                                , throwable -> toiletListView.hideProgress());
                subscriptions.add(subscription1);
                break;
        }
    }

    public void feedbackLeaved(CharSequence input) {
        Subscription subscription = feedbackRepository.putFeedback(authRepository.getUserId(), input.toString())
                .doOnSubscribe(() -> toiletListView.showProgress())
                .subscribe(feedback -> toiletListView.hideProgress(), throwable -> {
                    toiletListView.hideProgress();
                    Timber.e(throwable, throwable.getMessage());
                });
        subscriptions.add(subscription);
    }

    public void feedbackPressed() {
        if (authRepository.isSignedIn()) {
            toiletListView.showFeedbackForm();
        } else {
            toiletListView.navigateToAuth();
        }
    }

    private long lastUpdateTime;
    private static final long REFRESH_PERIOD = 1000 * 5;

    public void refresh() {
        if (System.currentTimeMillis() - lastUpdateTime > REFRESH_PERIOD) {
            loadToilets();
            lastUpdateTime = System.currentTimeMillis();
        } else {
            toiletListView.hideProgress();
        }
    }

    public void locationEnabledSuccess() {
        loadCurrentLocation();
    }

    public void locationPermissionSuccess() {
        loadCurrentLocation();
    }

    public void citySelected(Place place) {
        Subscription subscription = locationRepository.getLocationFromLatLng(place.getLatLng().latitude, place.getLatLng().longitude)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(location1 -> {
                    this.location = location1;
                    toiletListView.showCityName(location1.getCity());
                    loadToilets();
                });
        subscriptions.add(subscription);
    }
}
