package com.myproject.mymodel.domain;

import java.util.ArrayList;
import java.util.List;

public class Dpattern {

    private int startId;
    private int length;
    private float high;
    private float low;
    private int pivotDirection; // none: 0; up: 1; down: -1;
    private List<Scratch> featureScratches; ////Scratches that created the double pivots pattern;
    private List<Pivot> featurePivots; //Pivots that created the double pivots pattern;

    public Dpattern() {
    }

    public Dpattern(Pivot pivot) {
        this.startId = pivot.getStartId();
        this.length = pivot.getLength();
        this.high = pivot.getHigh();
        this.low = pivot.getLow();
        this.pivotDirection = pivot.getPivotType();
        this.featureScratches = pivot.getScratches();
        this.featurePivots = new ArrayList<>();
    }

    public int getStartId() {
        return startId;
    }

    public void setStartId(int startId) {
        this.startId = startId;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
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

    public int getPivotDirection() {
        return pivotDirection;
    }

    public void setPivotDirection(int pivotDirection) {
        this.pivotDirection = pivotDirection;
    }

    public List<Scratch> getFeatureScratches() {
        return featureScratches;
    }

    public void setFeatureScratches(List<Scratch> featureScratches) {
        this.featureScratches = featureScratches;
    }

    public List<Pivot> getFeaturePivots() {
        return featurePivots;
    }

    public void setFeaturePivots(List<Pivot> featurePivots) {
        this.featurePivots = featurePivots;
    }

    @Override
    public String toString() {
        return "Dpattern{" +
                "startId=" + startId +
                ", length=" + length +
                ", high=" + high +
                ", low=" + low +
                ", pivotDirection=" + pivotDirection +
                ", featureScratches=" + featureScratches +
                ", featurePivots=" + featurePivots +
                '}';
    }
}
