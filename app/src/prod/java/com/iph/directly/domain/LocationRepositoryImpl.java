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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
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
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;

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
        return locationProvider.getUpdatedLocation(LocationRequest.create().setInterval(200).setNumUpdates(1))
                .doOnNext(location -> {
                    Timber.d("Location", location.toString());
                })
                .flatMap(locations -> locationProvider.getReverseGeocodeObservable(locations.getLatitude(), locations.getLongitude(), 1), (location, addresses) -> {
                    Location myLocation = new Location(location.getLatitude()
                            , location.getLongitude()
                            , addresses.get(0).getLocality()
                            , addresses.get(0).getCountryName()
                            , addresses.get(0).getThoroughfare()
                            , addresses.get(0).getSubThoroughfare());
                    return myLocation;
                });
    }

    @Override
    public Observable<Toilet> initPlaceId(Location currentLocation, Toilet toilet) {
        String requestString = (toilet.getCity() + ", " + toilet.getAddress()).replace(", б/н", "");
        if (toilet.getLatitude() != 0) {
            return Observable.just(toilet);
        }
        return locationProvider.getPlaceAutocompletePredictions(requestString, null, null)
                .map(autocompletePredictions -> {
                    String placeId = null;
                    for (AutocompletePrediction prediction : autocompletePredictions) {
                        if (prediction.getSecondaryText(null).toString().contains(toilet.getCity() + ",")) {
                            placeId = prediction.getPlaceId();
                            break;
                        } else {
                            Timber.e("Location prediction", prediction);
                        }
                    }
                    toilet.setPlaceId(placeId);
                    return toilet;
                })
                .flatMap(toilet1 -> toilet.getPlaceId() == null ? Observable.just(toilet1) : locationProvider.getPlaceById(toilet1.getPlaceId()).flatMap(places -> {
                    LatLng latLng = places.get(0).getLatLng();
                    if (latLng != null) {
                        toilet1.setLatitude(latLng.latitude);
                        toilet1.setLongitude(latLng.longitude);
                    }
                    return Observable.just(toilet1);
                }))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<String> getCurrentLocationText() {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Observable.error(new SecurityException("No location permission granted"));
        }
        return locationProvider.getCurrentPlace(null)
                .map(placeLikelihoods -> {
                    Place place = placeLikelihoods.get(0).getPlace();
                    return place.getAddress().toString();
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
