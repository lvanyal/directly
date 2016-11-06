package com.iph.directly.domain;

import com.iph.directly.domain.model.Feedback;

import rx.Observable;

/**
 * Created by vanya on 11/5/2016.
 */

public interface FeedbackRepository {
    Observable<Feedback> putFeedback(String userId, String text);
}
