package com.iph.directly.domain;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by vanya on 10/8/2016.
 */

public class Injector {
    public static LocationRepository provideLocationRepository(Fragment fragment, Activity activity, int requestCode) {
        return new LocationRepositoryImpl(fragment, activity, requestCode);
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
