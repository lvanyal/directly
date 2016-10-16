package com.iph.directly.domain;

import com.iph.directly.domain.apimodel.CityToiletsResponse;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;
import com.iph.directly.domain.rest.ToiletApi;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private ToiletApi toiletApi;

    public ToiletRepositoryImpl() {
        toiletApi = RetrofitHolder.getInstance().getRetrofit().create(ToiletApi.class);
    }

    @Override
    public Observable<List<Toilet>> getToilets(Location location) {
        return toiletApi.toiletList(location.getCity() + "_wc")
                .flatMap(cityToiletsResponse -> toiletApi.getToiletsSource(cityToiletsResponse.getResourceUrl()))
                .flatMap(responseBody -> Observable.create((Observable.OnSubscribe<List<Toilet>>) subscriber -> {
                    try {
                        String string = responseBody.body().string();
                        subscriber.onNext(parseToilets(string));
                        subscriber.onCompleted();
                    } catch (IOException e) {
                        subscriber.onError(e);
                    }
                }))

                .subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
    }

    private List<Toilet> parseToilets(String source) throws IOException {
        CSVReader csvReader = new CSVReader(new StringReader(source));

        List<String[]> items = csvReader.readAll();
        csvReader.close();

        String[] titleRow = items.get(0);
        List<String> titleList = Arrays.asList(titleRow);
        int wcNameIndex = titleList.indexOf(COLUMN_WC_NAME);
        int priceIndex = titleList.indexOf(COLUMN_PRICE);
        int startTimeIndex = titleList.indexOf(COLUMN_START_TIME);
        int stopTimeIndex = titleList.indexOf(COLUMN_STOP_TIME);
        int addressIndex = titleList.indexOf(COLUMN_ADDRESS);
        int is24HoursIndex = titleList.indexOf(COLUMN_IS_24HOURS);

        List<Toilet> result = new ArrayList<>();

        for (int i = 1; i < items.size(); i++) {
            String[] row = items.get(i);
            Toilet toilet = new Toilet();
            toilet.setName(row[wcNameIndex]);
            toilet.setPrice(Float.parseFloat(row[priceIndex]));
            toilet.setStartTime(row[startTimeIndex]);
            toilet.setEndTime(row[stopTimeIndex]);
            toilet.setAddress(row[addressIndex]);
            toilet.setIs24h(Boolean.parseBoolean(row[is24HoursIndex]));

            result.add(toilet);
        }

        return result;
    }
}
