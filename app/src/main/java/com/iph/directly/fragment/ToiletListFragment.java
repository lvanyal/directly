package com.iph.directly.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.directly.iph.directly.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.iph.directly.domain.Injector;
import com.iph.directly.domain.auth.Authorizer;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;
import com.iph.directly.presenter.ToiletListPresenter;
import com.iph.directly.view.ToiletListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * A placeholder fragment containing a simple view.
 */
public class ToiletListFragment extends Fragment implements ToiletListView {

    public static final String EXTRA_LOCATION = "extra_location";
    public static final String MAP_TAG = "mapTag";
    public static final String NEW_TOILET_TAG = "newToilet";
    public static final String TAG = ToiletListFragment.class.getName();
    public static final String EXTRA_NEW_TOILET = "extra_new_toilet";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1105;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1108;

    private static int LOCATION_ENABLE_REQUEST_CODE = 2345;

    private SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm", Locale.getDefault());

    private Comparator<Toilet> DISTANCE_COMPARATOR = (toilet1, toilet2) -> {
        if (toilet2.getDistance() == 0) {
            return -1;
        } else if (toilet1.getDistance() == 0) {
            return 1;
        } else {
            return toilet1.getDistance() - toilet2.getDistance();
        }
    };

    private Comparator<Toilet> PRICE_COMPARATOR = (toilet1, toilet2) -> Float.compare(toilet1.getPrice(), toilet2.getPrice());

    private Comparator<Toilet> TIME_COMPARATOR = (toilet1, toilet2) -> {
        return (int) (toilet1.getStartTime() - toilet2.getStartTime());
    };

    private boolean isSignedIn;

    private ToiletListPresenter toiletListPresenter;

    private RecyclerView toiletListView;
    private ToiletsAdapter toiletsAdapter;
    private FloatingActionButton addToiletButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;
    private View locationOffView;
    private View enableLocationButton;
    private TextView cityNameView;

    private Authorizer authorizer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_toilet_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).setSupportActionBar((android.support.v7.widget.Toolbar) view.findViewById(R.id.toolbar));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        addToiletButton = (FloatingActionButton) view.findViewById(R.id.add_toilet_button);
        addToiletButton.setOnClickListener(addToiletClickListener);
        cityNameView = (TextView) view.findViewById(R.id.toolbar_title);
        cityNameView.setOnClickListener(view1 -> showLocationChooser());
        emptyView = view.findViewById(R.id.empty_view);
        locationOffView = view.findViewById(R.id.location_off_view);
        enableLocationButton = locationOffView.findViewById(R.id.enable_location_button);
        enableLocationButton.setOnClickListener(view1 -> toiletListPresenter.enableLocationButtonClick());
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> toiletListPresenter.refresh());
        toiletListView = (RecyclerView) view.findViewById(R.id.toilet_list);
        toiletListView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        toiletsAdapter = new ToiletsAdapter();
        toiletListView.setAdapter(toiletsAdapter);
    }

    View.OnClickListener addToiletClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            toiletListPresenter.newToiletClicked();
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        authorizer = new Authorizer(this, () -> toiletListPresenter.onUserSingedIn());

        Location location = getArguments().getParcelable(EXTRA_LOCATION);
        toiletListPresenter = new ToiletListPresenter(this
                , Injector.provideToiletRepository(getActivity())
                , Injector.provideLocationRepository(this, getActivity(), LOCATION_ENABLE_REQUEST_CODE)
                , Injector.provideDirectionRepository(getActivity())
                , authorizer
                , Injector.provideStrikeRepository()
                , Injector.provideFeedbackRepository()
                , Injector.provideDeviceInfo(getActivity())
                , location);
    }

    @Override
    public void showCityName(String cityName) {
        this.cityNameView.setText(cityName);
    }

    public void showLocationChooser() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(getActivity());
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Timber.e(e, "", "");
        }
    }

    @Override
    public void navigateToToiletCreation() {
        NewToiletFragment fragment;
        if (getActivity().getSupportFragmentManager().findFragmentByTag(NEW_TOILET_TAG) == null) {
            fragment = new NewToiletFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, NEW_TOILET_TAG)
                    .addToBackStack(NEW_TOILET_TAG)
                    .commit();
        } else {
            fragment = (NewToiletFragment) getActivity().getSupportFragmentManager().findFragmentByTag(NEW_TOILET_TAG);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, NEW_TOILET_TAG)
                    .addToBackStack(NEW_TOILET_TAG)
                    .commit();
        }
    }

    @Override
    public void updateSignInStatus(boolean isSignedIn) {
        this.isSignedIn = isSignedIn;
        getActivity().invalidateOptionsMenu();
    }


    @Override
    public void showToiletMenu(Toilet toilet, ToiletMenuItem... toiletMenuItems) {
        Observable.from(toiletMenuItems)
                .map(toiletMenuItem -> (CharSequence) getString(toiletMenuItem.resId))
                .toList()
                .subscribe(text -> {
                            new MaterialDialog.Builder(getActivity())
                                    .items(text.toArray(new CharSequence[0]))
                                    .itemsCallback((dialog, itemView, position, text1) ->
                                            toiletListPresenter.toiletMenuItemChosen(toilet, toiletMenuItems[position]))
                                    .show();
                        }
                );

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem loginLogoutItem = menu.findItem(R.id.login_logout);
        loginLogoutItem.setTitle(isSignedIn ? R.string.menu_item_logout : R.string.menu_item_login);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login_logout:
                toiletListPresenter.loginLogoutPressed();
                return true;
            case R.id.feedback:
                toiletListPresenter.feedbackPressed();
                return true;
            case R.id.distance:
                Collections.sort(toiletsAdapter.toilets, DISTANCE_COMPARATOR);
                toiletsAdapter.notifyDataSetChanged();
                return true;
            case R.id.price:
                Collections.sort(toiletsAdapter.toilets, PRICE_COMPARATOR);
                toiletsAdapter.notifyDataSetChanged();
                return true;
            case R.id.time:
                Collections.sort(toiletsAdapter.toilets, TIME_COMPARATOR);
                toiletsAdapter.notifyDataSetChanged();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void showFeedbackForm() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.your_feedback_is_very_important)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(getString(R.string.leave_your_feedback), getString(R.string.app_is_normal), (dialog, input) -> {
                    toiletListPresenter.feedbackLeaved(input);
                }).show();

    }

    @Override
    public void navigateToAuth() {
        authorizer.signIn();
    }

    @Override
    public void onStart() {
        super.onStart();
        toiletListPresenter.start();
        authorizer.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        toiletListPresenter.stop();
        authorizer.stop();
    }

    @Override
    public void showToiletList(List<Toilet> toilets) {
        toiletsAdapter.setToilets(toilets);
        toiletsAdapter.notifyDataSetChanged();
    }

    @Override
    public void showProgress() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showEmptyView() {
        emptyView.setVisibility(View.VISIBLE);
        toiletsAdapter.setToilets(Collections.emptyList());
        toiletsAdapter.notifyDataSetChanged();
        toiletListView.setVisibility(View.GONE);
    }

    @Override
    public void hideEmptyView() {
        emptyView.setVisibility(View.GONE);
        toiletListView.setVisibility(View.VISIBLE);
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
                    toiletListPresenter.locationPermissionSuccess();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        authorizer.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == LOCATION_ENABLE_REQUEST_CODE) {
                new Handler().post(() -> {
                    toiletListPresenter.locationEnabledSuccess();
                });
            }
            if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                toiletListPresenter.citySelected(place);
            }
        }


    }

    @Override
    public void navigateToDirection(Toilet toilet, Location location) {
        DirectionFragment fragment;
        if (getFragmentManager().findFragmentByTag(MAP_TAG) == null) {
            fragment = new DirectionFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, MAP_TAG)
                    .hide(this)
                    .addToBackStack(MAP_TAG)
                    .commit();
        } else {
            fragment = (DirectionFragment) getFragmentManager().findFragmentByTag(MAP_TAG);
            getFragmentManager()
                    .beginTransaction()
                    .hide(this)
                    .show(fragment)
                    .commit();
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(DirectionFragment.EXTRA_TOILET, toilet);
        bundle.putParcelable(DirectionFragment.EXTRA_LOCATION, location);
        fragment.setArguments(bundle);
    }

    private class ToiletHolder extends RecyclerView.ViewHolder {
        private TextView toiletName;
        private TextView workTime;
        private TextView address;
        private TextView price;
        private TextView distance;

        ToiletHolder(View itemView) {
            super(itemView);
            toiletName = (TextView) itemView.findViewById(R.id.toilet_name);
            workTime = (TextView) itemView.findViewById(R.id.work_time);
            address = (TextView) itemView.findViewById(R.id.address);
            price = (TextView) itemView.findViewById(R.id.price);
            distance = (TextView) itemView.findViewById(R.id.distance);
        }
    }

    @Override
    public void updateToiletPositionInList(List<Toilet> toilets) {
        toiletsAdapter.setToilets(toilets);
        toiletsAdapter.notifyDataSetChanged();
    }

    @Override
    public void navigateToMapsApp(Toilet toilet) {
        Uri gmmIntentUri;
        if (toilet.getLatitude() != 0) {
            gmmIntentUri = Uri.parse(String.format(Locale.getDefault(), "http://maps.google.com/maps?daddr=%s,%s&mode=walking", toilet.getLatitude(), toilet.getLongitude()));
        } else if (toilet.getPlaceId() != null) {
            gmmIntentUri = Uri.parse(String.format(Locale.getDefault(), "http://maps.google.com/maps?dplace_id=%s&mode=walking", toilet.getPlaceId()));
        } else {
            gmmIntentUri = Uri.parse(String.format(Locale.getDefault(), "geo:0,0?q=%s+%s&mode=walking", toilet.getCity(), toilet.getAddress()));
        }
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    @Override
    public void showNoLocationView() {
        locationOffView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLocationNotEnabledView() {
        locationOffView.setVisibility(View.GONE);
    }

    private class ToiletsAdapter extends RecyclerView.Adapter<ToiletHolder> {

        private List<Toilet> toilets = new ArrayList<>();

        public void setToilets(List<Toilet> toilets) {
            this.toilets = toilets;
        }

        @Override
        public ToiletHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View toiletView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_toilet, parent, false);
            return new ToiletHolder(toiletView);
        }

        @Override
        public void onBindViewHolder(ToiletHolder holder, int position) {
            Toilet toilet = toilets.get(position);
            holder.toiletName.setText(toilet.getName());
            holder.price.setText(String.format(Locale.getDefault(), "%.2f %s", toilet.getPrice(), getString(R.string.uah)));
            holder.address.setText(toilet.getAddress());
            String startTime = timeFormatter.format(new Date(toilet.getStartTime()));
            String endTime = timeFormatter.format(new Date(toilet.getEndTime()));
            holder.workTime.setText(String.format(Locale.getDefault(), "%s-%s", startTime, endTime));
            holder.distance.setText(getFormattedDistance(toilet.getDistance()));

            holder.itemView.setOnClickListener(v -> {
                toiletListPresenter.toiletChoose(toilet);
            });

            holder.itemView.setOnLongClickListener(view -> {
                toiletListPresenter.toiletContextMenuOpen(toilet);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return toilets.size();
        }

        private String getFormattedDistance(int meters) {
            String formattedDistance;
            if (meters < 1000) {
                formattedDistance = String.format(Locale.getDefault(), "%d%s", meters, getString(R.string.meters));
            } else {
                formattedDistance = String.format(Locale.getDefault(), "%d.2%s", meters / 1000, getString(R.string.km));
            }
            return formattedDistance;
        }
    }
}
