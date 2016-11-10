package com.iph.directly.domain;

import android.app.Activity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iph.directly.domain.apimodel.CityToiletsResponse;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;
import com.iph.directly.domain.rest.ToiletApi;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by vanya on 10/15/2016.
 */

public class ToiletRepositoryImpl implements ToiletRepository {

    private static final String COLUMN_WC_NAME = "wc_name_full";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_START_TIME = "time_start";
    private static final String COLUMN_STOP_TIME = "time_stop";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_IS_24HOURS = "24hours";
    private static final String COLUMN_CITY = "city";

    private static final String TOILETS_TREE = "toilets";
    private final Activity activity;

    private ToiletApi toiletApi;
    private DatabaseReference databaseReference;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h.mm", Locale.getDefault());

    ToiletRepositoryImpl(Activity activity) {
        this.activity = activity;
        toiletApi = RetrofitHolder.getInstance().getRetrofit().create(ToiletApi.class);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public Observable<List<Toilet>> getToilets(Location location) {
        return Observable.zip(toiletApi.toiletList(location.getCity().replace("\'", "") + "_wc")
                        .onErrorResumeNext(Observable.empty())
                        .switchIfEmpty(Observable.just(CityToiletsResponse.EMPTY))
                        .flatMap(cityToiletsResponse -> {
                            if (cityToiletsResponse == CityToiletsResponse.EMPTY) {
                                return Observable.just(null);
                            } else {
                                return toiletApi.getToiletsSource(cityToiletsResponse.getResourceUrl());
                            }
                        })
                        .flatMap(responseBody ->
                                Observable.create((Observable.OnSubscribe<List<Toilet>>) subscriber -> {
                                    try {
                                        String string = responseBody.body().string();
                                        subscriber.onNext(parseToilets(string));
                                        subscriber.onCompleted();
                                    } catch (IOException | ParseException e) {
                                        subscriber.onError(e);
                                    }
                                })).onErrorResumeNext(Observable.just(Collections.emptyList()))

                , getToiletsFromFirebase(location.getCity()), (toilets, toilets2) -> {
                    List<Toilet> resultToilets = new ArrayList<Toilet>(toilets);
                    resultToilets.addAll(toilets2);
                    return resultToilets;
                })

                .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<List<Toilet>> getToiletsFromFirebase(String city) {
        return Observable.create(new Observable.OnSubscribe<List<Toilet>>() {
            @Override
            public void call(Subscriber<? super List<Toilet>> subscriber) {
                databaseReference.child(TOILETS_TREE).child(city).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Toilet> toilets = new ArrayList<Toilet>();
                        for (DataSnapshot dataSnap : dataSnapshot.getChildren()) {
                            toilets.add(dataSnap.getValue(Toilet.class));
                        }
                        subscriber.onNext(toilets);
                        subscriber.onCompleted();
                        databaseReference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        subscriber.onError(databaseError.toException());
                    }
                });
            }
        });
    }

    @Override
    public Observable<Toilet> saveToilet(Toilet toilet) {
        return Observable.<Toilet>create(subscriber -> {
            databaseReference.child(TOILETS_TREE).child(toilet.getCity()).child(toilet.getId()).setValue(toilet).addOnCompleteListener(activity, task -> {
                subscriber.onNext(toilet);
                subscriber.onCompleted();
            });
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private List<Toilet> parseToilets(String source) throws IOException, ParseException {
        CsvParserSettings settings = new CsvParserSettings();

        settings.getFormat().setLineSeparator("\n");
        // creates a CSV parser
        CsvParser parser = new CsvParser(settings);

        List<String[]> items = parser.parseAll(new StringReader(source));

        String[] titleRow = items.get(0);
        List<String> titleList = Arrays.asList(titleRow);
        int wcNameIndex = titleList.indexOf(COLUMN_WC_NAME);
        int priceIndex = titleList.indexOf(COLUMN_PRICE);
        int startTimeIndex = titleList.indexOf(COLUMN_START_TIME);
        int stopTimeIndex = titleList.indexOf(COLUMN_STOP_TIME);
        int addressIndex = titleList.indexOf(COLUMN_ADDRESS);
        int is24HoursIndex = titleList.indexOf(COLUMN_IS_24HOURS);
        int cityIndex = titleList.indexOf(COLUMN_CITY);

        List<Toilet> result = new ArrayList<>();

        for (int i = 1; i < items.size(); i++) {
            String[] row = items.get(i);
            Toilet toilet = new Toilet();
            toilet.setName(row[wcNameIndex]);
            toilet.setPrice(Float.parseFloat(row[priceIndex]));
            long startTime = timeFormat.parse(row[startTimeIndex]).getTime();
            toilet.setStartTime(startTime);
            long endTime = timeFormat.parse(row[stopTimeIndex]).getTime();
            toilet.setEndTime(endTime);
            toilet.setAddress(row[addressIndex]);
            toilet.setIs24h(Boolean.parseBoolean(row[is24HoursIndex]));
            toilet.setCity(row[cityIndex]);

            result.add(toilet);
        }

        return result;
    }
}
