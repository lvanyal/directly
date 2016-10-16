package com.iph.directly.domain.rest;

import com.iph.directly.domain.apimodel.CityToiletsResponse;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by vanya on 10/15/2016.
 */

public interface ToiletApi {
    @GET("api/action/package_search")
    Observable<CityToiletsResponse> toiletList(@Query("q") String city);

    @GET
    Observable<Response<ResponseBody>> getToiletsSource(@Url String url);
}
