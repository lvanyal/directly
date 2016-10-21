package com.iph.directly.presenter;

import com.google.android.gms.maps.model.LatLng;
import com.iph.directly.domain.DirectionRepository;
import com.iph.directly.domain.apimodel.RouteResponse;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;
import com.iph.directly.view.MapView;

import java.util.List;

import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by vanya on 10/8/2016.
 */

public class MapPresenter {
    private final MapView mapView;
    private DirectionRepository directionRepository;
    private Location currentLocation;
    private Subscription currentSubscription;

    public MapPresenter(MapView mapView, DirectionRepository directionRepository) {
        this.mapView = mapView;
        this.directionRepository = directionRepository;
    }

    public void mapReady(Location currentLocation) {
        this.currentLocation = currentLocation;
        mapView.showLocation(currentLocation);
    }

    public void toiletChosen(Toilet toilet) {
        currentSubscription = directionRepository.getDirectionToToilet(currentLocation, toilet).subscribe(new Action1<RouteResponse>() {
            @Override
            public void call(RouteResponse routeResponse) {
                //// TODO: 10/20/2016 show route
            }
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
