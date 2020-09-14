package com.myproject.mymodel.domain;

import java.util.ArrayList;
import java.util.List;

public class Pivot {
    private int length;
    private int startId;
    private float high;
    private float low;
    private int pivotType;// If not a pivot: 0; is a consolidation up: 1; down: -1; trend up: 2; trend down: -2;
    private List<Scratch> scratches;

    public Pivot() {
    }

    public Pivot(Pivot pivot) {
        this.length = pivot.getLength();
        this.startId = pivot.getStartId();
        this.high = pivot.getHigh();
        this.low = pivot.getLow();
        this.pivotType = pivot.getPivotType();
        this.scratches = pivot.getScratches();
    }

    public Pivot(Scratch scratch) {
        this.length = scratch.getLength();
        this.startId = scratch.getStartId();
        this.high = scratch.getHigh();
        this.low = scratch.getLow();
        this.pivotType = scratch.getStatus();
        this.scratches = new ArrayList<>();
        this.getScratches().add(scratch);
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

    public int getPivotType() {
        return pivotType;
    }

    public void setPivotType(int pivotType) {
        this.pivotType = pivotType;
    }

    public List<Scratch> getScratches() {
        return scratches;
    }

    public void setScratches(List<Scratch> scratches) {
        this.scratches = scratches;
    }

    @Override
    public String toString() {
        return "Pivot{" +
                "length=" + length +
                ", startId=" + startId +
                ", high=" + high +
                ", low=" + low +
                ", pivotType=" + pivotType +
                ", scratches=" + scratches +
                '}';
    }
}
