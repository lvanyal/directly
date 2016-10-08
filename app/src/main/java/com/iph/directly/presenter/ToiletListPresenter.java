package com.iph.directly.presenter;

import com.iph.directly.domain.ToiletRepository;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;
import com.iph.directly.view.ToiletListView;

import java.util.List;

import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by vanya on 10/8/2016.
 */

public class ToiletListPresenter {
    private ToiletListView toiletListView;
    private ToiletRepository toiletRepository;

    private Location location;

    private Subscription currentToiletSubscription;

    public ToiletListPresenter(ToiletListView toiletListView, ToiletRepository toiletRepository, Location location) {
        this.toiletListView = toiletListView;
        this.toiletRepository = toiletRepository;
        this.location = location;
    }

    public void start() {
        toiletListView.showProgress();
        currentToiletSubscription = toiletRepository.getToilets(location).subscribe(toilets -> {
            toiletListView.hideProgress();
            if (toilets == null || toilets.isEmpty()) {
                toiletListView.showEmptyView();
            } else {
                toiletListView.showToiletList(toilets);
            }
        }, throwable -> {
            toiletListView.hideProgress();
            Timber.e(throwable.getMessage(), throwable);
        });
    }

    public void stop() {
        if (currentToiletSubscription != null) {
            currentToiletSubscription.unsubscribe();
            currentToiletSubscription = null;
        }
    }
}
