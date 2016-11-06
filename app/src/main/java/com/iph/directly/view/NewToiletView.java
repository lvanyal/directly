package com.iph.directly.view;

import com.iph.directly.domain.model.Toilet;

/**
 * Created by vanya on 10/24/2016.
 */

public interface NewToiletView {
    void showAddress(String address);

    String getAddress();

    long getStartTime();

    long getEndTime();

    String getName();

    boolean isFullDay();

    int getPrice();

    void showError(int resourceId);

    void navigateToToiletList(Toilet toilet1);
}
