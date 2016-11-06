package com.iph.directly.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.directly.iph.directly.R;
import com.iph.directly.domain.Injector;
import com.iph.directly.domain.model.Location;
import com.iph.directly.presenter.LoadingPresenter;
import com.iph.directly.view.LoadingView;

/**
 * Created by vanya on 10/8/2016.
 */

public class LoadingFragment extends Fragment implements LoadingView {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 12321;
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
        loadingPresenter = new LoadingPresenter(this, Injector.provideLocationRepository(getActivity()));
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
        ActivityCompat.requestPermissions(getActivity()
                , new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                        , Manifest.permission.ACCESS_COARSE_LOCATION}
                , LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
          if (resultCode == Activity.RESULT_OK) {
              loadingPresenter.locationPermissionSuccess();
          } else {
              loadingPresenter.locationPermissionFailed();
          }
      }
    }
}
