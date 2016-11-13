package com.iph.directly.view;

import com.directly.iph.directly.R;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;

import java.util.List;

/**
 * Created by vanya on 10/5/2016.
 */

public interface ToiletListView {


    enum ToiletMenuItem {
        STRIKE(R.string.strike), UNSTRIKE(R.string.unstrike)
        , EDIT(R.string.edit_toilet);

        ToiletMenuItem(int resId) {
            this.resId = resId;
        }

        public int resId;
    }

    void showRequestLocationPermission();

    void showToiletList(List<Toilet> toilets);

    void showProgress();

    void hideProgress();

    void showEmptyView();

    void hideEmptyView();

    void navigateToDirection(Toilet toilet, Location location);

    void updateToiletPositionInList(List<Toilet> toilet);

    void navigateToMapsApp(Toilet toilet);

    void navigateToToiletCreation(String userId);

    void navigateToToiletEdition(Toilet toilet);

    void showFeedbackForm();

    void navigateToAuth();

    void updateSignInStatus(boolean isSignedIn);

    void showNoLocationView();

    void hideLocationNotEnabledView();

    void showToiletMenu(Toilet toilet, ToiletMenuItem... toiletMenuItems);

    void showCityName(String cityName);
}
