package com.iph.directly.domain.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by vanya on 10/8/2016.
 */

public class Location {
    private LatLng latLng;

    private String city;

    public Location(double latitude, double longitude, String city) {
        this.latLng = new LatLng(latitude, longitude);
        this.city = city;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
