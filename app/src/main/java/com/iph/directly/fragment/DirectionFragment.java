package com.iph.directly.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.directly.iph.directly.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.iph.directly.domain.model.Location;
import com.iph.directly.view.MapView;

import java.util.List;

/**
 * Created by vanya on 10/16/2016.
 */

public class DirectionFragment extends SupportMapFragment implements MapView, OnMapReadyCallback {
    public static final String EXTRA_TOILET = "extra_toilet";
    public static final String EXTRA_LOCATION = "extra_location";

    private static final LatLng KYIV_LAT_LNG = new LatLng(50.449549, 30.522830);
    private static final int DEFAULT_MAP_RATIO = 16;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        getMapAsync(this);
    }

    @Override
    public void showLocation(Location currentLocation) {

    }

    @Override
    public void showDirection(List<LatLng> latLngs) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = getArguments() != null
                && getArguments().containsKey(EXTRA_LOCATION)
                ? ((Location) getArguments().getParcelable(EXTRA_LOCATION)).getLatLng()
                : KYIV_LAT_LNG;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_MAP_RATIO));
        if (ActivityCompat
                .checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }
}
