package com.myproject.mymodel.domain;

public class PatternResult {
    int length1;
    int length2;
    int length3;
    int length4;
    int length5;
    int length6;
    float diff1;
    float diff2;
    float diff3;
    float diff4;
    float diff5;
    float diff6;

    public PatternResult() {
    }

    public PatternResult(Scratch scratch1,Scratch scratch2,Scratch scratch3,Scratch scratch4) {
        this.length1 = scratch2.getStartId()-scratch1.getStartId()-scratch1.getLength()+2;
        this.length2 = scratch2.getLength();
        this.length3 = scratch3.getStartId()-scratch2.getStartId()-scratch2.getLength()+2;
        this.length4 = scratch3.getLength();
        this.length5 = scratch4.getStartId()-scratch3.getStartId()-scratch3.getLength()+2;
        this.length6 = scratch4.getLength();
        if(scratch2.getStatus()>0){
            this.diff1 = scratch1.getHigh()-scratch2.getLow();
            this.diff2 = scratch2.getHigh()-scratch2.getLow();
            this.diff3 = scratch2.getHigh()-scratch3.getLow();
            this.diff4 = scratch3.getHigh()-scratch3.getLow();
            this.diff5 = scratch3.getHigh()-scratch4.getLow();
            this.diff6 = scratch4.getHigh()-scratch4.getLow();
        }else {
            this.diff1 = scratch2.getHigh()-scratch1.getLow();
            this.diff2 = scratch2.getHigh()-scratch2.getLow();
            this.diff3 = scratch3.getHigh()-scratch2.getLow();
            this.diff4 = scratch3.getHigh()-scratch3.getLow();
            this.diff5 = scratch4.getHigh()-scratch3.getLow();
            this.diff6 = scratch4.getHigh()-scratch4.getLow();
        }

    }

    public int getLength1() {
        return length1;
    }

    public void setLength1(int length1) {
        this.length1 = length1;
    }

    public int getLength2() {
        return length2;
    }

    public void setLength2(int length2) {
        this.length2 = length2;
    }

    public int getLength3() {
        return length3;
    }

    public void setLength3(int length3) {
        this.length3 = length3;
    }

    public int getLength4() {
        return length4;
    }

    public void setLength4(int length4) {
        this.length4 = length4;
    }

    public int getLength5() {
        return length5;
    }

    public void setLength5(int length5) {
        this.length5 = length5;
    }

    public int getLength6() {
        return length6;
    }

    public void setLength6(int length6) {
        this.length6 = length6;
    }

    public float getDiff1() {
        return diff1;
    }

    public void setDiff1(float diff1) {
        this.diff1 = diff1;
    }

    public float getDiff2() {
        return diff2;
    }

    public void setDiff2(float diff2) {
        this.diff2 = diff2;
    }

    public float getDiff3() {
        return diff3;
    }

    public void setDiff3(float diff3) {
        this.diff3 = diff3;
    }

    public float getDiff4() {
        return diff4;
    }

    public void setDiff4(float diff4) {
        this.diff4 = diff4;
    }

    public float getDiff5() {
        return diff5;
    }

    public void setDiff5(float diff5) {
        this.diff5 = diff5;
    }

    public float getDiff6() {
        return diff6;
    }

    public void setDiff6(float diff6) {
        this.diff6 = diff6;
    }

    @Override
    public String toString() {
        return "PatternResult{" +
                "length1=" + length1 +
                ", length2=" + length2 +
                ", length3=" + length3 +
                ", length4=" + length4 +
                ", length5=" + length5 +
                ", length6=" + length6 +
                ", diff1=" + diff1 +
                ", diff2=" + diff2 +
                ", diff3=" + diff3 +
                ", diff4=" + diff4 +
                ", diff5=" + diff5 +
                ", diff6=" + diff6 +
                '}';
    }
}
