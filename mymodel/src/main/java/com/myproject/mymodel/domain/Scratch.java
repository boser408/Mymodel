package com.myproject.mymodel.domain;

public class Scratch {
    private int length;
    private int startId;
    private float high;
    private float low;
    private int status; // if the scratch fully formed: uptrend:1 ; notrend: 0; downtrend:-1

    public Scratch(int length, int startId, float high, float low, int status) {
        this.length = length;
        this.startId = startId;
        this.high = high;
        this.low = low;
        this.status = status;
    }
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
    public int getStartId() {
        return startId;
    }
    public void setStartId(int startId) {
        this.startId = startId;
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
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    @Override
    public String toString() {
        return "Scratch{" +
                "length=" + length +
                ", startId=" + startId +
                ", high=" + high +
                ", low=" + low +
                ", status=" + status +
                '}';
    }

}
