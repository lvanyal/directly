package com.iph.directly.domain.apimodel;

import java.util.List;

/**
 * Created by vanya on 18.04.16.
 */
public class RouteResponse {

    private List<Route> routes;

    public int getDistance() {
        return this.routes.get(0).legs.get(0).distance.value;
    }

    public int getDuration() {
        return this.routes.get(0).legs.get(0).duration.value;
    }

    public String getPoints() {
        return this.routes.get(0).overview_polyline.points;
    }

    public boolean hasPoints() {
        return !routes.isEmpty();
    }

    private class Route {
        private OverviewPolyline overview_polyline;
        private List<Leg> legs;
    }

    private class OverviewPolyline {
        String points;
    }

    private class Leg {
        private Distance distance;
        private Duration duration;
    }

    private class Distance {
        private int value;
    }

    private class Duration {
        private int value;
    }
}