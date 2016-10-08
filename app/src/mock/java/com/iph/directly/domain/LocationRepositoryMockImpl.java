package com.iph.directly.domain;

import com.iph.directly.domain.model.Location;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by vanya on 10/8/2016.
 */

public class LocationRepositoryMockImpl implements LocationRepository{

    private final Location location;

    public LocationRepositoryMockImpl(Location location) {
        this.location = location;
    }

    public Observable<Location> getCurrentLocation() {
        return Observable.just(location);
    }
}
