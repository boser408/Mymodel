package com.myproject.mymodel.domain;

public class Dpattern {
    private int length;
    private int startId;
    private float high;
    private float low;
    private int directionType; // none: 0; up: 1; down: -1;
    private String polyline;

    public Dpattern(Pivot pivot ) {
        this.length = pivot.getLength();
        this.startId = pivot.getStartId();
        this.high = pivot.getHigh();
        this.low = pivot.getLow();
        this.directionType = pivot.getPivotType();
        this.polyline = pivot.getScratches().toString();
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

    public int getDirectionType() {
        return directionType;
    }

    public void setDirectionType(int directionType) {
        this.directionType = directionType;
    }

    public String getPolyline() {
        return polyline;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }

    @Override
    public String toString() {
        return "Dpattern{" +
                "length=" + length +
                ", startId=" + startId +
                ", high=" + high +
                ", low=" + low +
                ", directionType=" + directionType +
                ", polyline='" + polyline + '\'' +
                '}';
    }
}
