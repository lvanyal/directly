package com.iph.directly.domain;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iph.directly.domain.model.Strike;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vanya on 11/3/2016.
 */

public class StrikeRepositoryImpl implements StrikeRepository {

    private static final String STRIKES_TREE = "strikes";

    private DatabaseReference databaseReference;

    public StrikeRepositoryImpl() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public Observable<Boolean> isToiletStrikedByUser(String toiletId, String userId) {
        return Observable.<Boolean>create(subscriber -> {
            databaseReference.child(STRIKES_TREE).child(toiletId).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean exists = dataSnapshot.exists();
                    subscriber.onNext(exists);
                    subscriber.onCompleted();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    subscriber.onError(databaseError.toException());
                }
            });
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Strike> putStrike(String toiletId, String userId) {
        return Observable.create(new Observable.OnSubscribe<Strike>() {
            @Override
            public void call(Subscriber<? super Strike> subscriber) {
                Strike strike = new Strike(userId);
                databaseReference.child(STRIKES_TREE).child(toiletId).child(userId).setValue(strike).addOnCompleteListener(task -> {
                    subscriber.onNext(strike);
                    subscriber.onCompleted();
                }).addOnFailureListener(subscriber::onError);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Object> removeStrike(String toiletId, String userId) {
        return Observable.create(subscriber -> {
            databaseReference.child(STRIKES_TREE).child(toiletId).child(userId).removeValue((databaseError, databaseReference1) -> {
                if (databaseError != null) {
                    subscriber.onError(databaseError.toException());
                }
                subscriber.onNext(null);
                subscriber.onCompleted();
            });
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
