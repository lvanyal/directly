package com.iph.directly.domain.rest;

import com.iph.directly.domain.apimodel.RouteResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by vanya on 10/17/2016.
 */

public interface DirectionApi {
    @GET("/maps/api/directions/json")
    Observable<RouteResponse> getRoute(
            @Query(value = "origin") String position,
            @Query(value = "destination") String destination,
            @Query("sensor") boolean sensor,
            @Query("mode") String mode,
            @Query("language") String language,
            @Query("key") String apiKey);
}
