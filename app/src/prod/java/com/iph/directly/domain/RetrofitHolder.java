package com.iph.directly.domain;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by vanya on 10/15/2016.
 */
public class RetrofitHolder {
    private static RetrofitHolder ourInstance = new RetrofitHolder();

    private Retrofit retrofit;

    public static RetrofitHolder getInstance() {
        return ourInstance;
    }

    private RetrofitHolder() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://data.danimist.org.ua/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
