package com.iph.directly.domain;

import com.facebook.AccessToken;

/**
 * Created by Kapitoshka on 23.10.2016.
 */

public class FacebookRepositoryImpl implements FacebookRepository {
    @Override
    public boolean isSignedIn() {
        return AccessToken.getCurrentAccessToken() != null && !AccessToken.getCurrentAccessToken().isExpired();
    }
}
