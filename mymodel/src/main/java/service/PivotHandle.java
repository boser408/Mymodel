package service;

import com.myproject.mymodel.domain.Dpattern;
import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Pivot;
import com.myproject.mymodel.domain.Scratch;

import java.util.List;

public interface PivotHandle {
    int getNumberofLoop();
    Pivot cleanPivot (Pivot pivot);
    Dpattern findDpattern (Pivot pivot);
    Pivot dwsubpivotHandle(List<Pivot> cleanedPivotList, Pivot subpivot, int nloop,int endNumberofsubpivot);
    Pivot subpivotHandle(List<Pivot> cleanedPivotList, Pivot subpivot, int nloop,int endNumberofsubpivot);
    List<Scratch> findScratches(List<HighLowPrice> highLowPrices, int startindex, int length, int pivotLength);
    List<Pivot> findPivots(List<Scratch> scratches, int startId);
    List<Pivot> pivotExtension(List<Scratch> scratches,Pivot pivot,int startId);
    List<Pivot> scratchClean(List<Pivot> basicPivotList);// Create cleaned pivot lists that only contain effective scratches, which could potentially produce D-pattern;
    List<Dpattern> findDPatterninPivots(List<Pivot> basicPivotList);//Find small D-patterns within basic Pivots;
    List<Dpattern> findallDpattern(List<Pivot> cleanedPivotList);
}
