package service;

import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Pivot;
import com.myproject.mymodel.domain.Scratch;

import java.util.List;

public interface PivotHandle {
    double getControlFactor();
    int getPivotLength();
    Scratch checkHiddenScratch(List<HighLowPrice> highLowPrices);
    List<Scratch> removeRedundentScratch(List<Scratch> scratchList);
    List<Scratch> findScratchtoAdd(List<Scratch> allScratches, int nofStart, int nofEnd);
    List<Pivot> addScratchtoPivot(List<Scratch> allScratches, List<Pivot> keyPivotList);
    Scratch findSubScratch(int startId, int endId, List<HighLowPrice> highLowPrices,int endPatternPivotDirection);
    List<Scratch> findScratches(List<HighLowPrice> highLowPrices, int startindex,Scratch upscratch,Scratch dwscratch,int nofupscratch,int nofdwscratch);
    List<Pivot> findAllPivotsByScratch(List<Scratch> scratchList);
    List<Pivot> obtainKeyPivots(List<Pivot> allPivotList);
    List<Scratch> findEigenScratches(List<Pivot> pivotsForPatternSearch); // EigenScratches are scratches will be paired as the potential first scratch for 2nd or 3rd pattern;
    List<Scratch> mergeEigenScratches(List<Scratch> dailyEigenScratches, List<Scratch> intradayEigenScratches);//Merge daily and intraday (15mins etc) EigenScratches;
    void mergeUpdateData(String currenDataPath, String updateDataPath, String operatDataPath, String cutTime);
    void addPriceRecords(String fromDataPath,String toDataPath);
    List<Pivot> find2ndPattern(List<Pivot> pivotsForPatternSearch, List<Scratch> allCompoundScratches);
    List<Pivot> find3rdPattern(List<Pivot> pivotsForPatternSearch, List<Scratch> allCompoundScratches);
    List<Pivot> find4thPattern(List<Pivot> pivotsForPatternSearch, List<Scratch> allCompoundScratches);
    List<Pivot> findEarningScratch(List<Pivot> pivotsForSearch, List<HighLowPrice> allPrices,List<Scratch> allCompoundScratches);

}
