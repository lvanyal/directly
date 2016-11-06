package com.iph.directly.domain;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.iph.directly.domain.model.Feedback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rx.Observable;

/**
 * Created by vanya on 11/5/2016.
 */

public class FeedbackRepositoryImpl implements FeedbackRepository {
    private static final String FEEDBACK_TREE = "feedbacks";
    private DatabaseReference databaseReference;
    private SimpleDateFormat dateFormat;

    public FeedbackRepositoryImpl() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        dateFormat = new SimpleDateFormat("dd MM yyyy HH:mm:ss", Locale.getDefault());
    }

    @Override
    public Observable<Feedback> putFeedback(String userId, String text) {
        return Observable.create(subscriber -> {
            Feedback feedback = new Feedback(text);
            String date = dateFormat.format(new Date());
            databaseReference.child(FEEDBACK_TREE).child(userId).child(date).setValue(feedback).addOnCompleteListener(task -> {
                subscriber.onNext(feedback);
                subscriber.onCompleted();
            }).addOnFailureListener(subscriber::onError);
        });
    }
}
