package com.iph.directly.domain.auth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.iph.directly.domain.AuthRepository;
import com.iph.directly.fragment.ToiletListFragment;

import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by vanya on 10/25/2016.
 */

public class Authorizer implements AuthRepository {
    private static final String TAG = Authorizer.class.getName();
    private final AuthListener authListener;
    private final android.support.v4.app.Fragment fragment;

    private CallbackManager callbackManager;
    private FirebaseAuth.AuthStateListener authStateListener;

    private FirebaseAuth firebaseAuth;

    public Authorizer(android.support.v4.app.Fragment fragment, AuthListener authListener) {
        this.fragment = fragment;
        this.authListener = authListener;
        init();
    }

    private void init() {
        FacebookSdk.sdkInitialize(fragment.getActivity());
        firebaseAuth = FirebaseAuth.getInstance();
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

    @Override
    public boolean isSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    @Override
    public void signOut() {
        firebaseAuth.signOut();
    }

    @Override
    public String getUserId() {
        return firebaseAuth.getCurrentUser().getUid();
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Timber.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            authListener.onSignIn();
        });
    }

    public void start() {
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    public void stop() {
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void signIn() {
        LoginManager.getInstance().logInWithReadPermissions(fragment, Arrays.asList("public_profile", "email"));
    }

    public interface AuthListener {
        public void onSignIn();
    }
}
