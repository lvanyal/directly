package com.iph.directly.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.directly.iph.directly.R;
import com.google.android.gms.maps.SupportMapFragment;
import com.iph.directly.domain.Injector;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;
import com.iph.directly.presenter.ToiletListPresenter;
import com.iph.directly.view.ToiletListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class ToiletListFragment extends Fragment implements ToiletListView {

    public static final String EXTRA_LOCATION = "extra_location";
    public static final String MAP_TAG = "mapTag";

    private ToiletListPresenter toiletListPresenter;

    private RecyclerView toiletListView;
    private ToiletsAdapter toiletsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_toilet_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        toiletListView = (RecyclerView) view.findViewById(R.id.toilet_list);
        toiletListView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        toiletsAdapter = new ToiletsAdapter();
        toiletListView.setAdapter(toiletsAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Location location = getArguments().getParcelable(EXTRA_LOCATION);
        toiletListPresenter = new ToiletListPresenter(this, Injector.provideToiletRepository(), Injector.provideLocationRepository(getActivity()), Injector.provideDirectionRepository(getActivity()), location);
    }

    @Override
    public void onStart() {
        super.onStart();
        toiletListPresenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        toiletListPresenter.stop();
    }

    @Override
    public void showToiletList(List<Toilet> toilets) {
        toiletsAdapter.setToilets(toilets);
        toiletsAdapter.notifyDataSetChanged();
    }

    @Override
    public void showProgress() {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void showEmptyView() {

    }

    @Override
    public void hideEmptyView() {

    }

    @Override
    public void navigateToDirection(Toilet toilet, Location location) {
        SupportMapFragment fragment;
        if (getFragmentManager().findFragmentByTag(MAP_TAG) == null) {
            fragment = new DirectionFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, MAP_TAG)
                    .hide(this)
                    .addToBackStack(MAP_TAG)
                    .commit();
        } else {
            fragment = (SupportMapFragment) getFragmentManager().findFragmentByTag(MAP_TAG);
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
    public void updateToiletPositionInList(Toilet toilet) {
        toiletsAdapter.notifyDataSetChanged();
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
            holder.workTime.setText(String.format(Locale.getDefault(), "%s-%s", toilet.getStartTime(), toilet.getEndTime()));
            holder.distance.setText(toilet.getDistance() + "m");

            holder.itemView.setOnClickListener(v -> {
                toiletListPresenter.toiletChoose(toilet);
            });
        }

        @Override
        public int getItemCount() {
            return toilets.size();
        }
    }
}
