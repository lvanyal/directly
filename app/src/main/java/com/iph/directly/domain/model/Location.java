package com.iph.directly.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by vanya on 10/8/2016.
 */

public class Location implements Parcelable{
    private LatLng latLng;

    private String city;
    private String country;
    private String street;
    private String buildingNumber;

    public Location(double latitude, double longitude, String city, String country) {
        this.latLng = new LatLng(latitude, longitude);
        this.city = city;
        this.country = country;
    }

    public Location(double latitude, double longitude, String city, String country, String street, String buildingNumber) {
        this.latLng = new LatLng(latitude, longitude);
        this.city = city;
        this.country = country;
        this.street = street;
        this.buildingNumber = buildingNumber;
    }

    protected Location(Parcel in) {
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        city = in.readString();
        country = in.readString();
        this.street = in.readString();
        this.buildingNumber = in.readString();
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(latLng, flags);
        dest.writeString(city);
        dest.writeString(country);
        dest.writeString(street);
        dest.writeString(buildingNumber);
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStreet() {
        return street;
    }

    public String getBuildingNumber() {
        return buildingNumber;
    }
}
