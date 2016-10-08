package com.iph.directly.presenter;

import com.iph.directly.view.LoadingView;

/**
 * Created by vanya on 10/8/2016.
 */

public class LoadingPresenter {
    private LoadingView loadingView;

    public LoadingPresenter(LoadingView loadingView) {
        this.loadingView = loadingView;
    }

    public void start() {
        loadingView.showProgress(0);
    }

    public void stop() {

    }
}
