package com.iph.directly.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.gson.annotations.Expose;

/**
 * Created by vanya on 10/8/2016.
 */

public class Toilet implements Parcelable {

    @Expose
    private String id;

    @Expose
    private String name;

    @Expose
    private float price;

    @Expose
    private long startTime;

    @Expose
    private long endTime;

    @Expose
    private String address;

    @Expose
    private boolean is24h;

    @Exclude
    @Expose
    private String placeId;

    @Expose
    private String city;

    @Expose
    private double latitude;

    @Expose
    private double longitude;

    @Expose
    @Exclude
    private int distance;

    @Expose
    private String author = "";

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Toilet() {
    }

    protected Toilet(Parcel in) {
        name = in.readString();
        price = in.readFloat();
        startTime = in.readLong();
        endTime = in.readLong();
        address = in.readString();
        is24h = in.readByte() != 0;
        placeId = in.readString();
        city = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        author = in.readString();
    }

    public static final Creator<Toilet> CREATOR = new Creator<Toilet>() {
        @Override
        public Toilet createFromParcel(Parcel in) {
            return new Toilet(in);
        }

        @Override
        public Toilet[] newArray(int size) {
            return new Toilet[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean is24h() {
        return is24h;
    }

    public void setIs24h(boolean is24h) {
        this.is24h = is24h;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @Exclude
    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Toilet)) return false;

        Toilet toilet = (Toilet) o;

        if (Float.compare(toilet.price, price) != 0) return false;
        if (startTime != toilet.startTime) return false;
        if (endTime != toilet.endTime) return false;
        if (is24h != toilet.is24h) return false;
        if (!name.equals(toilet.name)) return false;
        if (!address.equals(toilet.address)) return false;
        return city.equals(toilet.city);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (price != +0.0f ? Float.floatToIntBits(price) : 0);
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (int) (endTime ^ (endTime >>> 32));
        result = 31 * result + address.hashCode();
        result = 31 * result + (is24h ? 1 : 0);
        result = 31 * result + city.hashCode();
        result = 31 * result + author.hashCode();
        return Math.abs(result);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeFloat(price);
        dest.writeLong(startTime);
        dest.writeLong(endTime);
        dest.writeString(address);
        dest.writeByte((byte) (is24h ? 1 : 0));
        dest.writeString(placeId);
        dest.writeString(city);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(author);
    }

    public boolean hasId() {
        return id != null;
    }

    public void generateId() {
        this.id = String.valueOf(hashCode());
    }

    public String getId() {
        return id;
    }
}
