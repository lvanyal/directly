package com.iph.directly.domain;

import android.content.Context;

import com.directly.iph.directly.R;
import com.google.android.gms.maps.model.LatLng;
import com.iph.directly.domain.apimodel.RouteResponse;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;
import com.iph.directly.domain.rest.DirectionApi;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by vanya on 10/17/2016.
 */

public class DirectionRepositoryImpl implements DirectionRepository {

    private DirectionApi directionApi;

    private String directionApiKey;

    public DirectionRepositoryImpl(Context context) {
        directionApiKey = context.getString(R.string.directions_api_key);
        directionApi = RetrofitHolder
                .getInstance()
                .getGoogleApiRetrofit()
                .create(DirectionApi.class);
    }

    @Override
    public Observable<RouteResponse> getDirectionToToilet(Location currentLocation, Toilet toilet) {
        LatLng currentLatLng = currentLocation.getLatLng();
        return directionApi.getRoute(currentLatLng.latitude + "," + currentLatLng.longitude, "place_id:" + toilet.getPlaceId(), true, "walking", "en", directionApiKey);
    }

    @Override
    public Observable<Toilet> initDistanceToToilet(Location currentLocation, Toilet toilet) {
        return getDirectionToToilet(currentLocation, toilet)
                .map(routeResponse -> {
                    toilet.setDistance(routeResponse.getDistance());
                    return toilet;
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
