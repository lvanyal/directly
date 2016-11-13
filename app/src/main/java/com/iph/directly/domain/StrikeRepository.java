package com.iph.directly.domain;

import com.iph.directly.domain.model.Strike;

import java.util.List;

import rx.Observable;

/**
 * Created by vanya on 11/2/2016.
 */

public interface StrikeRepository {
    Observable<Boolean> isToiletStrikedByUser(String toiletId, String userId);
    Observable<Integer> putStrike(String toiletId, String userId);
    Observable<Object> removeStrike(String toiletId, String userId);
}
