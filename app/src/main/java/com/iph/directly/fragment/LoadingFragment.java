package com.iph.directly.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.directly.iph.directly.R;
import com.google.android.gms.common.api.Status;
import com.iph.directly.domain.Injector;
import com.iph.directly.domain.model.Location;
import com.iph.directly.presenter.LoadingPresenter;
import com.iph.directly.view.LoadingView;

import timber.log.Timber;

/**
 * Created by vanya on 10/8/2016.
 */

public class LoadingFragment extends Fragment implements LoadingView {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int LOCATION_ENABLE_REQUEST_CODE = 1002;
    private LoadingPresenter loadingPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loading, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        loadingPresenter = new LoadingPresenter(this, Injector.provideLocationRepository(this, getActivity(), LOCATION_ENABLE_REQUEST_CODE));
    }

    @Override
    public void onStart() {
        super.onStart();
        loadingPresenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        loadingPresenter.stop();
    }

    @Override
    public void showProgress() {

    }

    @Override
    public void navigateToToilets(Location location) {
        Fragment fragment = new ToiletListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ToiletListFragment.EXTRA_LOCATION, location);
        fragment.setArguments(bundle);
        getFragmentManager().
                beginTransaction()
                .replace(R.id.container, fragment, ToiletListFragment.TAG)
                .commit();
    }

    @Override
    public void showRequestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                        , Manifest.permission.ACCESS_COARSE_LOCATION}
                , LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        new Handler().post(() -> {
            if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    loadingPresenter.locationPermissionSuccess();
                } else {
                    loadingPresenter.locationPermissionFailed();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        new Handler().post(() -> {
            if (requestCode == LOCATION_ENABLE_REQUEST_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    loadingPresenter.locationEnabledSuccess();
                } else {
                    loadingPresenter.locationEnabledFailed();
                }
            }
        });
    }
}
