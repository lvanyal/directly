package com.iph.directly.domain;

import com.google.android.gms.maps.model.LatLng;
import com.iph.directly.domain.apimodel.RouteResponse;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;

import java.util.List;

import rx.Observable;

/**
 * Created by vanya on 10/8/2016.
 */

public interface DirectionRepository {
    Observable<RouteResponse> getDirectionToToilet(Location currentLocation, Toilet toilet);
    Observable<Toilet> initDistanceToToilet(Location currentLocation, Toilet toilet);
}
