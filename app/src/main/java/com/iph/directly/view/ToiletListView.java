package com.iph.directly.view;

import com.iph.directly.domain.model.Toilet;

import java.util.List;

/**
 * Created by vanya on 10/5/2016.
 */

public interface ToiletListView {
    void showToiletList(List<Toilet> toilets);
    void showProgress();
    void hideProgress();
    void showEmptyView();
    void hideEmptyView();
}
