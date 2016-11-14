package com.iph.directly.domain;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
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
import com.iph.directly.fragment.LoadingFragment;

import java.util.List;
import java.util.Locale;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import pl.charmas.android.reactivelocation.observables.geocode.ReverseGeocodeObservable;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by vanya on 10/8/2016.
 */

public class LocationRepositoryImpl implements LocationRepository {

    private Activity activity;
    private Fragment fragment;
    private ReactiveLocationProvider locationProvider;
    private int locationRequestCode;

    public LocationRepositoryImpl(Fragment fragment, Activity activity, int locationRequestCode) {
        this.activity = activity;
        this.fragment = fragment;
        locationProvider = new ReactiveLocationProvider(activity);
        this.locationRequestCode = locationRequestCode;
    }

    public Observable<Location> getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Observable.error(new SecurityException("No location permission granted"));
        }
        LocationRequest locationRequest = LocationRequest.create().setInterval(200).setNumUpdates(1).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationProvider.checkLocationSettings(new LocationSettingsRequest.Builder().setAlwaysShow(true).addLocationRequest(locationRequest).build()).flatMap(locationSettingsResult -> {
            if (locationSettingsResult.getStatus().isSuccess()) {
                return locationProvider.getUpdatedLocation(locationRequest)
                        .doOnNext(location -> {
                            Timber.d("Location", location.toString());
                        })
                        .flatMap(locations -> getLocationFromLatLng(locations.getLatitude(), locations.getLongitude()));
            } else {
                try {
                    fragment.startIntentSenderForResult(locationSettingsResult.getStatus().getResolution().getIntentSender(), locationRequestCode, null, 0, 0, 0, null);
                } catch (IntentSender.SendIntentException e) {
                    return Observable.error(e);
                }
                return Observable.error(new RuntimeException("Location not enabled"));
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Location> getLocationFromLatLng(double latitude, double longitude) {
        return ReverseGeocodeObservable.createObservable(activity, Locale.UK, latitude, longitude, 10).flatMap((Func1<List<Address>, Observable<Location>>) addresses -> {

            Location myLocation = null;
            for (Address address : addresses) {
                if (address.getLocality() != null) {
                    myLocation = new Location(latitude
                            , longitude
                            ,address.getLocality()
                            ,address.getCountryName()
                            ,address.getThoroughfare()
                            ,address.getSubThoroughfare());
                    break;
                }
            }
            return Observable.just(myLocation);
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
                            Timber.e("Location prediction fail: %s", prediction.getSecondaryText(null));
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
        if (ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
