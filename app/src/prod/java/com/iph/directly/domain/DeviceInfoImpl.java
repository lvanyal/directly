package com.iph.directly.domain;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Created by vanya on 10/30/2016.
 */

public class DeviceInfoImpl implements DeviceInfo {

    private Context context;

    public DeviceInfoImpl(Context context) {
        this.context = context;
    }

    @Override
    public boolean isMapAppAvailable() {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
