package com.myproject.mymodel.domain;

public class HighLowPrice {
    private String date;
    private int id;
    private float open;
    private float high;
    private float low;
    private float close;
    private float ndhigh;//Highest price in next n days
    private float ndlow;//Lowest price in next n days

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getOpen() {
        return open;
    }

    public void setOpen(float open) {
        this.open = open;
    }

    public float getHigh() {
        return high;
    }

    public void setHigh(float high) {
        this.high = high;
    }

    public float getLow() {
        return low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    public float getClose() {
        return close;
    }

    public void setClose(float close) {
        this.close = close;
    }

    public float getNdhigh() {
        return ndhigh;
    }

    public void setNdhigh(float ndhigh) {
        this.ndhigh = ndhigh;
    }

    public float getNdlow() {
        return ndlow;
    }

    public void setNdlow(float ndlow) {
        this.ndlow = ndlow;
    }

    @Override
    public String toString() {
        return "HighLowPrice{" +
                "date='" + date + '\'' +
                ", id=" + id +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", ndhigh=" + ndhigh +
                ", ndlow=" + ndlow +
                '}';
    }
}
