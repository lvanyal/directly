package com.iph.directly.domain;

import android.app.Activity;
import android.content.Context;

/**
 * Created by vanya on 10/8/2016.
 */

public class Injector {
    public static LocationRepository provideLocationRepository(Context context) {
        return new LocationRepositoryImpl(context);
    }

    public static ToiletRepository provideToiletRepository(Activity activity) {
        return new ToiletRepositoryImpl(activity);
    }

    public static DirectionRepository provideDirectionRepository(Context activity) {
        return new DirectionRepositoryImpl(activity);
    }

    public static DeviceInfo provideDeviceInfo(Context context) {
        return new DeviceInfoImpl(context);
    }

    public static StrikeRepository provideStrikeRepository() {
        return new StrikeRepositoryImpl();
    }

    public static FeedbackRepository provideFeedbackRepository(){
        return new FeedbackRepositoryImpl();
    }
}
