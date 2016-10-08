package com.iph.directly.domain;

import com.iph.directly.domain.model.Location;

import rx.Observable;

/**
 * Created by vanya on 10/8/2016.
 */

public interface LocationRepository {
    Observable<Location> getCurrentLocation();
}
