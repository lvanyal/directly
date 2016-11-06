package com.iph.directly.domain;

import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;

import java.util.List;

import rx.Observable;

/**
 * Created by vanya on 10/8/2016.
 */

public interface ToiletRepository {
    Observable<List<Toilet>> getToilets(Location location);

    Observable<Toilet> saveToilet(Toilet toilet);
}
