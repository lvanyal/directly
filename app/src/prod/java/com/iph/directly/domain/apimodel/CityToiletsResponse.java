package com.iph.directly.domain.apimodel;

import java.util.List;

/**
 * Created by vanya on 10/15/2016.
 */

public class CityToiletsResponse {

    public static final CityToiletsResponse EMPTY = new CityToiletsResponse();

    private boolean success;

    private Result result;

    private class Result {
        List<InnerResult> results;
    }

    private class InnerResult {
        private List<Resources> resources;
    }

    private class Resources {
        String format;
        String url;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getResourceUrl() {
        return result.results.get(0).resources.get(0).url;
    }

    public String getResourceFormat() {
        return result.results.get(0).resources.get(0).format;
    }
}
