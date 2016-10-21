package com.iph.directly.domain;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;

import java.util.List;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by vanya on 10/8/2016.
 */

public class LocationRepositoryImpl implements LocationRepository {

    private Context context;
    private ReactiveLocationProvider locationProvider;

    public LocationRepositoryImpl(Context context) {
        this.context = context;
        locationProvider = new ReactiveLocationProvider(context);
    }

    public Observable<Location> getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Observable.error(new SecurityException("No location permission granted"));
        }
        return locationProvider.getUpdatedLocation(LocationRequest.create().setInterval(200).setNumUpdates(1)).flatMap(locations ->
                locationProvider.getReverseGeocodeObservable(locations.getLatitude(), locations.getLongitude(), 1))
                .flatMap(addresses -> {
                    Location location = new Location(addresses.get(0).getLatitude(), addresses.get(0).getLongitude(), addresses.get(0).getLocality(), addresses.get(0).getCountryName());
                    return Observable.just(location);
                });
    }

    @Override
    public Observable<Toilet> initPlaceId(Location currentLocation, Toilet toilet) {
        return locationProvider.getPlaceAutocompletePredictions(toilet.getCity() + ", " + toilet.getAddress(), null, null)
                .filter(autocompletePredictions -> autocompletePredictions.getCount() != 0)
                .map(autocompletePredictions -> {
                    String placeId = null;
                    for (AutocompletePrediction prediction: autocompletePredictions) {
                        if (prediction.getSecondaryText(null).toString().contains(toilet.getCity() + ",")) {
                            placeId = prediction.getPlaceId();
                            break;
                        }
                    }
                    toilet.setPlaceId(placeId);
                    return toilet;
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
