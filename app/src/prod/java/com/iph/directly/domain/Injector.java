package com.iph.directly.domain;

import android.content.Context;

/**
 * Created by vanya on 10/8/2016.
 */

public class Injector {
    public static LocationRepository provideLocationRepository(Context context) {
        return new LocationRepositoryImpl(context);
    }

    public static ToiletRepository provideToiletRepository() {
        return new ToiletRepositoryImpl();
    }
}
