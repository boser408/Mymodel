package com.myproject.mymodel.domain;

import java.util.List;

public class Dpattern {

    private int startId;
    private int length;
    private float high;
    private float low;
    private int pivotDirection; // none: 0; up: 1; down: -1;
    private List<Pivot> featurePivots; //Pivots that determine the double pivots pattern;
    private List<Pivot> maxPivots; // Pivots that determine the level of trend (polyline);

}
