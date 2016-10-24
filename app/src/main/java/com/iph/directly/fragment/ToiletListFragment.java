package com.iph.directly.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.directly.iph.directly.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.iph.directly.domain.FacebookRepository;
import com.iph.directly.domain.Injector;
import com.iph.directly.domain.model.Location;
import com.iph.directly.domain.model.Toilet;
import com.iph.directly.presenter.ToiletListPresenter;
import com.iph.directly.view.ToiletListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

import timber.log.Timber;

/**
 * A placeholder fragment containing a simple view.
 */
public class ToiletListFragment extends Fragment implements ToiletListView {

    public static final String EXTRA_LOCATION = "extra_location";
    public static final String MAP_TAG = "mapTag";
    private static final String TAG = ToiletListFragment.class.getName();

    private FirebaseAuth firebaseAuth;

    private ToiletListPresenter toiletListPresenter;

    private RecyclerView toiletListView;
    private ToiletsAdapter toiletsAdapter;
    private FloatingActionButton addToiletButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_toilet_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        addToiletButton = (FloatingActionButton) view.findViewById(R.id.add_toilet_button);
        addToiletButton.setOnClickListener(addToiletClickListener);
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

    private CallbackManager callbackManager;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        firebaseAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        Location location = getArguments().getParcelable(EXTRA_LOCATION);
        toiletListPresenter = new ToiletListPresenter(this
                , Injector.provideToiletRepository()
                , Injector.provideLocationRepository(getActivity())
                , Injector.provideDirectionRepository(getActivity())
                , Injector.provideFacebookRepository(), location);

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Timber.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Timber.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                // User is signed out
                Timber.d(TAG, "onAuthStateChanged:signed_out");
            }
        };
    }



    private void handleFacebookAccessToken(AccessToken token) {
        Timber.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    Timber.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                    if (!task.isSuccessful()) {
                        Timber.e(TAG, "signInWithCredential", task.getException());
                        Toast.makeText(getActivity(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        FirebaseUser user = task.getResult().getUser();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

    @Override
    public void navigateToToiletCreation() {

    }

    @Override
    public void navigateToFbAuth() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
    }

    @Override
    public void onStart() {
        super.onStart();
        toiletListPresenter.start();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        toiletListPresenter.stop();
        firebaseAuth.removeAuthStateListener(authStateListener);
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
        Uri gmmIntentUri = Uri.parse(String.format(Locale.getDefault(), "geo:0,0?q=%s+%s", toilet.getCity(), toilet.getAddress()));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
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
            holder.distance.setText(getFormattedDistance(toilet.getDistance()));

            holder.itemView.setOnClickListener(v -> {
                toiletListPresenter.toiletChoose(toilet);
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
