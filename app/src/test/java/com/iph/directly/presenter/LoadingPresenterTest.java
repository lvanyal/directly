package com.iph.directly.presenter;

import android.content.Context;

import com.iph.directly.domain.LocationRepository;
import com.iph.directly.domain.LocationRepositoryImpl;
import com.iph.directly.domain.model.Location;
import com.iph.directly.view.LoadingView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * Created by vanya on 10/8/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadingPresenterTest {

    private static final String CITY = "Lviv";
    private static final String COUNTRY = "Ukraine";
    private static final Location LOCATION = new Location(49.844063, 24.025633, CITY, COUNTRY);

    private LoadingPresenter loadingPresenter;

    @Before
    public void initMocks() {
        LocationRepository locationRepository = new LocationRepositoryImpl(LOCATION);
        loadingPresenter = new LoadingPresenter(loadingView, locationRepository);
    }

    @Mock
    private LoadingView loadingView;

    @Test
    public void start() throws Exception {
        loadingPresenter.start();
        verify(loadingView).showProgress();
        verify(loadingView).navigateToToilets(LOCATION);
    }

}