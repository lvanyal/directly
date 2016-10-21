package com.iph.directly.domain;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import rx.Observable;

/**
 * Created by vanya on 6/12/2016.
 */
public interface RemoteRouteRepository {
    Observable<List<LatLng>> getRoute(LatLng currentLocation, LatLng toiletLocation);
}
