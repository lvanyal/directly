package com.iph.directly.presenter;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
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
    private Toilet toilet;

    private static final LatLng KYIV_LAT_LNG = new LatLng(50.449549, 30.522830);

    public MapPresenter(MapView mapView, DirectionRepository directionRepository) {
        this.mapView = mapView;
        this.directionRepository = directionRepository;
    }

    public void mapReady(Location currentLocation, Toilet toilet) {
        this.currentLocation = currentLocation;
        this.toilet = toilet;
        mapView.showLocation(currentLocation != null && currentLocation.getLatLng() != null ? currentLocation.getLatLng() : KYIV_LAT_LNG);
        loadDirection();
    }

    private void loadDirection() {
        currentSubscription = directionRepository.getDirectionToToilet(currentLocation, toilet).subscribe(routeResponse -> {
            List<LatLng> latLngs = PolyUtil.decode(routeResponse.getPoints());
            mapView.showDirection(latLngs, toilet);
        }, throwable -> {
            Timber.e(throwable, throwable.getMessage());
        });
    }

    public void stop() {
        if (currentSubscription != null) {
            currentSubscription.unsubscribe();
            currentSubscription = null;
        }
    }
}
