package com.iph.directly.domain.model;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by vanya on 11/1/2016.
 */

public class Strike {
    @Expose
    private String userId;

    public String getUserId() {
        return userId;
    }

    public Strike(String userId) {
        this.userId = userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
