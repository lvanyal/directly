package com.iph.directly.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.directly.iph.directly.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.iph.directly.domain.Injector;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;
import com.iph.directly.presenter.MapPresenter;
import com.iph.directly.view.MapView;

import java.util.List;
import java.util.Locale;

/**
 * Created by vanya on 10/16/2016.
 */

public class DirectionFragment extends SupportMapFragment implements MapView, OnMapReadyCallback {
    public static final String EXTRA_TOILET = "extra_toilet";
    public static final String EXTRA_LOCATION = "extra_location";

    private static final int DEFAULT_MAP_ZOOM = 16;

    private MapPresenter mapPresenter;
    private GoogleMap googleMap;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mapPresenter = new MapPresenter(this, Injector.provideDirectionRepository(getActivity()));
        setRetainInstance(true);
        getMapAsync(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapPresenter.stop();
    }

    @Override
    public void showLocation(LatLng currentLocation) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_MAP_ZOOM));
        if (ActivityCompat
                .checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void showDirection(List<LatLng> latLngs, Toilet toilet) {
        googleMap.addPolyline(new PolylineOptions().color(getResources().getColor(R.color.mapRouteColor)).width(getResources().getDimensionPixelOffset(R.dimen.map_route_width))).setPoints(latLngs);
        Marker marker = googleMap.addMarker(new MarkerOptions().position(latLngs.get(latLngs.size() - 1)).title(toilet.getName()).snippet(getFormattedDistance(toilet.getDistance())));
        marker.showInfoWindow();
    }

    private String getFormattedDistance(int meters) {
        String formattedDistance;
        if (meters < 1000) {
            formattedDistance = String.format(Locale.getDefault(), "%d%s", meters, getString(R.string.meters));
        } else {
            formattedDistance = String.format(Locale.getDefault(), "%d.2%s", meters / 1000, getString(R.string.km));
        }
        return formattedDistance;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        toiletSelected();
    }

    public void toiletSelected() {
        Location location = getArguments().getParcelable(EXTRA_LOCATION);
        Toilet toilet = getArguments().getParcelable(EXTRA_TOILET);
        mapPresenter.mapReady(location, toilet);
    }
}
