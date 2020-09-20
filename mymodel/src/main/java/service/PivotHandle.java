package service;

import com.myproject.mymodel.domain.Dpattern;
import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Pivot;
import com.myproject.mymodel.domain.Scratch;

import java.util.List;

public interface PivotHandle {
    int getNumberofLoop();
    double getControlFactor();
    Scratch checkHiddenScratch(List<HighLowPrice> highLowPrices,int scratchdirection);
    List<Scratch> removeRedundentScratch(List<Scratch> scratchList);
    Dpattern findDpattern (Pivot pivot);
    Dpattern findTpattern(Pivot pivot);
    List<Scratch> findScratches(List<HighLowPrice> highLowPrices, int startindex, int length, int pivotLength);
    List<Pivot> findAllPivotsByScratch(List<Scratch> scratchList);
    List<Pivot> obtainKeyPivots(List<Pivot> allPivotList);
}
