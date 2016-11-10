package com.iph.directly.domain;

import android.app.PendingIntent;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;

import rx.Observable;

/**
 * Created by vanya on 10/8/2016.
 */

public interface LocationRepository {
    Observable<Location> getCurrentLocation();

    Observable<Location> getLocationFromLatLng(double latitude, double longitude);

    Observable<Toilet> initPlaceId(Location currentLocation, Toilet toilet);
    Observable<String> getCurrentLocationText();

    class LocationNotEnabledException extends RuntimeException {
        private Status status;

        public LocationNotEnabledException(Status status) {
            this.status = status;
        }

        public Status getStatus() {
            return status;
        }
    }
}
