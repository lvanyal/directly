package com.iph.directly.domain;

/**
 * Created by Kapitoshka on 23.10.2016.
 */

public interface AuthRepository {
    boolean isSignedIn();

    void signOut();

    String getUserId();
}
