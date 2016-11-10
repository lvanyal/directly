package com.iph.directly.presenter;

import android.text.TextUtils;

import com.directly.iph.directly.R;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.iph.directly.domain.LocationRepository;
import com.iph.directly.domain.ToiletRepository;
import com.iph.directly.domain.model.Toilet;
import com.iph.directly.view.NewToiletView;

import java.util.Locale;

import timber.log.Timber;

/**
 * Created by vanya on 10/26/2016.
 */

public class NewToiletPresenter {
    private NewToiletView newToiletView;

    private ToiletRepository toiletRepository;
    private LocationRepository locationRepository;
    private LatLng latLng;
    private String city;

    public NewToiletPresenter(NewToiletView newToiletView, ToiletRepository toiletRepository, LocationRepository locationRepository) {
        this.newToiletView = newToiletView;
        this.toiletRepository = toiletRepository;
        this.locationRepository = locationRepository;
    }

    public void start() {
        showCurrentLocation();
    }

    private void showCurrentLocation() {
        locationRepository.getCurrentLocation()
                .subscribe(location -> {
                    this.latLng = location.getLatLng();
                    this.city = location.getCity();
                    newToiletView.showAddress(String.format(Locale.getDefault(), "%s, %s", location.getStreet(), location.getBuildingNumber()));
                }, throwable -> Timber.e(throwable, throwable.getMessage()));
    }

    public void stop() {

    }

    public void currentLocationButtonClicked() {
        showCurrentLocation();
    }

    public void placeSelected(Place place) {
        locationRepository.getLocationFromLatLng(place.getLatLng().latitude, place.getLatLng().longitude).subscribe(location -> {
            this.latLng = place.getLatLng();
            this.city = location.getCity();
            newToiletView.showAddress(place.getAddress().toString());
        });
    }

    public void createButtonClicked() {
        if (latLng == null) {
            newToiletView.showError(R.string.wait_location);
            return;
        }
        Toilet toilet = new Toilet();
        toilet.setLatitude(latLng.latitude);
        toilet.setLongitude(latLng.longitude);
        toilet.setStartTime(newToiletView.getStartTime());
        toilet.setEndTime(newToiletView.getEndTime());
        toilet.setName(newToiletView.getName());
        toilet.setAddress(newToiletView.getAddress());
        toilet.setCity(city);
        toilet.setPrice(newToiletView.getPrice());
        toilet.setIs24h(newToiletView.isFullDay());

        if (validateToilet(toilet)) {
            toilet.generateId();
            saveToilet(toilet);
        }

    }

    private void saveToilet(Toilet toilet) {
        toiletRepository.saveToilet(toilet)
                .subscribe(toilet1 -> newToiletView.navigateToToiletList(toilet1)
                        , throwable -> Timber.e(throwable, throwable.getMessage()));
    }

    private boolean validateToilet(Toilet toilet) {
        if (TextUtils.isEmpty(toilet.getAddress())) {
            newToiletView.showError(R.string.empty_address);
            return false;
        } else if (toilet.getLatitude() == 0) {
            newToiletView.showError(R.string.wait_location);
            return false;
        } else if (!toilet.is24h()) {
            if (toilet.getStartTime() == 0) {
                newToiletView.showError(R.string.empty_start_time);
                return false;
            } else if (toilet.getEndTime() == 0) {
                newToiletView.showError(R.string.empty_end_time);
                return false;
            }
        }
        if (TextUtils.isEmpty(toilet.getName())) {
            newToiletView.showError(R.string.empty_name);
            return false;
        }
        return true;
    }
}
