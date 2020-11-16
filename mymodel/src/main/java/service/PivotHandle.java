package service;

import com.myproject.mymodel.domain.Dpattern;
import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Pivot;
import com.myproject.mymodel.domain.Scratch;

import java.util.List;

public interface PivotHandle {
    double getControlFactor();
    Scratch checkHiddenScratch(List<HighLowPrice> highLowPrices);
    List<Scratch> removeRedundentScratch(List<Scratch> scratchList);
    List<Scratch> findScratchtoAdd(List<Scratch> allScratches, int nofStart, int nofEnd);
    List<Pivot> addScratchtoPivot(List<Scratch> allScratches, List<Pivot> keyPivotList);
    Dpattern findDpattern (Pivot pivot);
    Dpattern findTpattern(Pivot pivot);
    List<Dpattern> findAllDpattern(List<Pivot> pivotList);
    List<Scratch> findScratches(List<HighLowPrice> highLowPrices, int startindex, int length);
    List<Pivot> findAllPivotsByScratch(List<Scratch> scratchList);
    List<Pivot> obtainKeyPivots(List<Pivot> allPivotList);
    List<Pivot> find3rdPattern(List<Pivot> pivotsForPatternSearch, List<Scratch> allCompoundScratches);
    List<Pivot> find2ndPattern(List<Pivot> pivotsForPatternSearch, List<Scratch> allCompoundScratches);
    List<Pivot> find4thPattern(List<Pivot> pivotsForPatternSearch, List<Scratch> allCompoundScratches);
    List<Pivot> findSubScratch(List<Pivot> pivotsof2ndPattern, List<HighLowPrice> allPrices,List<Scratch> allCompoundScratches);

}
