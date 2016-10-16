package com.iph.directly.view;

import com.iph.directly.domain.model.Location;

/**
 * Created by vanya on 10/8/2016.
 */

public interface LoadingView {
    void showProgress();

    void navigateToToilets(Location location);

    void showRequestLocationPermission();
}
