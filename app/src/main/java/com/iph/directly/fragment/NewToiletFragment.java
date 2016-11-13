package com.iph.directly.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.directly.iph.directly.R;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.data.DataBufferObserver;
import com.google.android.gms.internal.zzc;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.iph.directly.domain.Injector;
import com.iph.directly.domain.model.Toilet;
import com.iph.directly.presenter.NewToiletPresenter;
import com.iph.directly.view.NewToiletView;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Retrofit;
import rx.Observable;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

/**
 * Created by vanya on 10/26/2016.
 */

public class NewToiletFragment extends Fragment implements NewToiletView {

    private static final int DEFAULT_START_TIME_HOUR = 9;
    private static final int DEFAULT_END_TIME_HOUR = 21;

    public static final String EXTRA_CURRENT_USER_ID = "extra_current_user_id";
    public static final String EXTRA_TOILET = "extra_toilet";

    private Calendar startTimeCal = Calendar.getInstance();
    private Calendar endTimeCal = Calendar.getInstance();

    {
        startTimeCal.set(Calendar.HOUR_OF_DAY, DEFAULT_START_TIME_HOUR);
        startTimeCal.set(Calendar.MINUTE, 0);

        endTimeCal.set(Calendar.HOUR_OF_DAY, DEFAULT_END_TIME_HOUR);
        endTimeCal.set(Calendar.MINUTE, 0);
    }

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1232;
    private static final int LOCATION_ENABLE_REQUEST_CODE = 32;
    private EditText address;
    private CheckBox isFullDay;
    private TextView startTime;
    private TextView endTime;
    private Spinner price;
    private TextView organization;

    private NewToiletPresenter newToiletPresenter;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    Calendar calendar = Calendar.getInstance();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        String currentUserId = getArguments().getString(EXTRA_CURRENT_USER_ID);
        Toilet toilet = getArguments().getParcelable(EXTRA_TOILET);
        newToiletPresenter = new NewToiletPresenter(this, Injector.provideToiletRepository(getActivity()), Injector.provideLocationRepository(this, getActivity(), LOCATION_ENABLE_REQUEST_CODE)
                , currentUserId, toilet);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_toilet, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        address = (EditText) view.findViewById(R.id.address);
        address.setOnClickListener(v -> showLocationChooser());
        isFullDay = (CheckBox) view.findViewById(R.id.isFullDay);
        isFullDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            startTime.setEnabled(!isChecked);
            endTime.setEnabled(!isChecked);
        });

        startTime = (TextView) view.findViewById(R.id.start_time);
        startTime.setText(timeFormat.format(startTimeCal.getTime()));
        startTime.setOnClickListener(v -> TimePickerDialog.newInstance((viewha, hourOfDay, minute, seconds) -> {
                    startTimeCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    startTimeCal.set(Calendar.MINUTE, seconds);
                    startTime.setText(getFormattedTime(hourOfDay, minute));
                }, DEFAULT_START_TIME_HOUR, 0, true).show(getActivity().getFragmentManager(), "startTime")
        );

        endTime = (TextView) view.findViewById(R.id.end_time);
        endTime.setText(timeFormat.format(endTimeCal.getTime()));
        endTime.setOnClickListener(v -> TimePickerDialog.newInstance((viewha, hourOfDay, minute, seconds) -> {
                    endTimeCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    endTimeCal.set(Calendar.MINUTE, minute);
                    endTime.setText(getFormattedTime(hourOfDay, minute));
                }, DEFAULT_END_TIME_HOUR, 0, true).show(getActivity().getFragmentManager(), "endTime")
        );
        price = (Spinner) view.findViewById(R.id.prices);
        organization = (TextView) view.findViewById(R.id.organization);
        view.findViewById(R.id.add_toilet).setOnClickListener(view1 -> newToiletPresenter.createButtonClicked()
        );

        view.findViewById(R.id.my_location).setOnClickListener(v -> newToiletPresenter.currentLocationButtonClicked());
    }

    private String getFormattedTime(int hour, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return timeFormat.format(new Date(calendar.getTimeInMillis()));
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
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), intent);
                newToiletPresenter.placeSelected(place);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        newToiletPresenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        newToiletPresenter.stop();
    }

    @Override
    public void showAddress(String address) {
        this.address.setText(address);
    }

    @Override
    public String getAddress() {
        return this.address.getText().toString();
    }

    @Override
    public long getStartTime() {
        return startTimeCal.getTimeInMillis();
    }

    @Override
    public long getEndTime() {
        return endTimeCal.getTimeInMillis();
    }

    @Override
    public String getName() {
        return organization.getText().toString();
    }

    @Override
    public boolean isFullDay() {
        return isFullDay.isChecked();
    }

    @Override
    public int getPrice() {
        return Integer.valueOf(((TextView) price.getSelectedView()).getText().toString());
    }

    @Override
    public void navigateToToiletList(Toilet toilet) {
        getActivity().getSupportFragmentManager().popBackStackImmediate();
    }

    @Override
    public void showError(int resourceId) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.error)
                .setMessage(getString(resourceId))
                .show();
    }

    @Override
    public void showToilet(Toilet toilet) {
        address.setText(toilet.getAddress());
        isFullDay.setChecked(toilet.is24h());
        startTime.setText(timeFormat.format(new Date(toilet.getStartTime())));
        endTime.setText(timeFormat.format(new Date(toilet.getEndTime())));
        price.setSelection((int) toilet.getPrice());
        organization.setText(toilet.getName());
    }
}
