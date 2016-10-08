package com.iph.directly.view;

import com.google.android.gms.maps.model.LatLng;
import com.iph.directly.domain.model.Location;

import java.util.List;

/**
 * Created by vanya on 10/5/2016.
 */
public interface MapView {

    void showLocation(Location currentLocation);

    void showDirection(List<LatLng> latLngs);
}
