package com.iph.directly.domain;

import com.directly.iph.directly.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by vanya on 10/15/2016.
 */
public class RetrofitHolder {
    private static RetrofitHolder ourInstance = new RetrofitHolder();

    private static final String DIRECTION_API_URL = "https://maps.googleapis.com";
    private static final String SERVER_URL = "https://data.danimist.org.ua/";


    private Retrofit retrofit;
    private Retrofit googleApiRetrofit;

    static RetrofitHolder getInstance() {
        return ourInstance;
    }

    private RetrofitHolder() {
        Retrofit.Builder builder = new Retrofit.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            httpClient.addInterceptor(logging);
            builder.client(httpClient.build());
        }
        retrofit = builder
                .baseUrl(SERVER_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        googleApiRetrofit = builder
                .baseUrl(DIRECTION_API_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    Retrofit getRetrofit() {
        return retrofit;
    }

    Retrofit getGoogleApiRetrofit() {
        return googleApiRetrofit;
    }
}
