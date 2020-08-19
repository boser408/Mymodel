package service;

import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Pivot;
import com.myproject.mymodel.domain.Scratch;

import java.util.List;

public interface PivotHandle {
    int getNumberofLoop();
    List<Scratch> findScratches(List<HighLowPrice> highLowPrices, int startindex, int length, int pivotLength);
    List<Pivot> findPivots(List<Scratch> scratches, int startId);
    List<Pivot> pivotExtension(List<Scratch> scratches,Pivot pivot,int startId);
    List<Pivot> findDoublePivotsPattern(List<Pivot> basicPivotList);
}
