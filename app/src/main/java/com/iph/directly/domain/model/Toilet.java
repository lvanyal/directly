package com.iph.directly.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by vanya on 10/8/2016.
 */

public class Toilet implements Parcelable{
    private String name;
    private float price;
    private String startTime;
    private String endTime;
    private String address;
    private boolean is24h;

    public Toilet() {}

    protected Toilet(Parcel in) {
        name = in.readString();
        price = in.readFloat();
        startTime = in.readString();
        endTime = in.readString();
        address = in.readString();
        is24h = in.readByte() != 0;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Toilet)) return false;

        Toilet toilet = (Toilet) o;

        return new EqualsBuilder()
                .append(price, toilet.price)
                .append(is24h, toilet.is24h)
                .append(name, toilet.name)
                .append(startTime, toilet.startTime)
                .append(endTime, toilet.endTime)
                .append(address, toilet.address)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(price)
                .append(startTime)
                .append(endTime)
                .append(address)
                .append(is24h)
                .toHashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeFloat(price);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(address);
        dest.writeByte((byte) (is24h ? 1 : 0));
    }
}
