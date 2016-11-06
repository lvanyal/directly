package com.iph.directly.presenter;

import com.iph.directly.domain.DeviceInfo;
import com.iph.directly.domain.DirectionRepository;
import com.iph.directly.domain.AuthRepository;
import com.iph.directly.domain.FeedbackRepository;
import com.iph.directly.domain.LocationRepository;
import com.iph.directly.domain.StrikeRepository;
import com.iph.directly.domain.ToiletRepository;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Strike;
import com.iph.directly.domain.model.Toilet;
import com.iph.directly.view.ToiletListView;

import java.util.ArrayList;
import java.util.Calendar;
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

    private Subscription currentToiletSubscription;
    private Subscription currentLocationSubscription;
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
        if (toilets.isEmpty()) {
            loadToilets();
        } else {
            toiletListView.showToiletList(toilets);
        }
        toiletListView.updateSignInStatus(authRepository.isSignedIn());
    }

    private void loadToilets() {
        currentToiletSubscription = toiletRepository.getToilets(location)
                .doOnSubscribe(() -> toiletListView.showProgress())
                .subscribe(toilets -> {
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
    }

    private void loadDistances() {
        currentLocationSubscription = Observable.from(toilets)
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
            toiletListView.showProgress();
            if (!toilet.hasId()) {
                toilet.generateId();
            }
            strikeRepository.isToiletStrikedByUser(toilet.getId(), authRepository.getUserId()).subscribe(alreadyStriked -> {
                        if (alreadyStriked) {
                            toiletListView.showToiletMenu(toilet, ToiletListView.ToiletMenuItem.UNSTRIKE);
                        } else {
                            toiletListView.showToiletMenu(toilet, ToiletListView.ToiletMenuItem.STRIKE);
                        }
                    }, throwable -> toiletListView.hideProgress()
            );
        } else {
            toiletListView.navigateToAuth();
        }
    }

    public void toiletMenuItemChosen(Toilet toilet, ToiletListView.ToiletMenuItem toiletMenuItem) {
        switch (toiletMenuItem) {
            case STRIKE:
                strikeRepository.putStrike(toilet.getId(), authRepository.getUserId())
                        .doOnSubscribe(() -> toiletListView.showProgress())
                        .subscribe(strike -> {
                                    toiletListView.hideProgress();
                                }
                                , throwable -> toiletListView.hideProgress());
                break;
            case UNSTRIKE:
                strikeRepository.removeStrike(toilet.getId(), authRepository.getUserId())
                        .doOnSubscribe(() -> toiletListView.showProgress())
                        .subscribe(strike -> {
                                    toiletListView.hideProgress();
                                }
                                , throwable -> toiletListView.hideProgress());
                break;
        }
    }

    public void feedbackLeaved(CharSequence input) {
        feedbackRepository.putFeedback(authRepository.getUserId(), input.toString())
                .doOnSubscribe(() -> toiletListView.showProgress())
                .subscribe(feedback -> toiletListView.hideProgress(), throwable -> {
                    toiletListView.hideProgress();
                    Timber.e(throwable.getMessage(), throwable);
                });
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
}
