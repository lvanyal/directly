package com.iph.directly.presenter;

import com.iph.directly.view.LoadingView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.runners.JUnit44RunnerImpl;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * Created by vanya on 10/8/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadingPresenterTest {

    private LoadingPresenter loadingPresenter;

    @Before
    public void initMocks() {
        loadingPresenter = new LoadingPresenter(loadingView);
    }

    @Mock
    LoadingView loadingView;

    @Test
    public void start() throws Exception {
        loadingPresenter.start();
        verify(loadingView).showProgress(0);
    }

}