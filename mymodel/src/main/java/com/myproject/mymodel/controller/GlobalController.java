package com.myproject.mymodel.controller;

import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Pivot;
import com.myproject.mymodel.domain.Scratch;
import service.InAndOutHandle;
import service.PatternStats;
import service.PivotHandle;
import service.impl.InAndOutHandleImpl;
import service.impl.PatternStatsImpl;
import service.impl.PivotHandleImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GlobalController {
    private String downloadDataPath;
    private String eigenScratchPath;
    private String basicScratchAddress;
    private String allCompoundScratchAddress;
    private String contractClass; //Ticker like: ES, NQ, YM, RTY, GC, GLD...
    private String contractSubLabel; //Supplementary description for contract such as "Z0" for ES, then build the full ticker of a contract like "ESZ0";
    private String priceBarType; //weekly,daily,hour,min;

    public GlobalController(String downloadDataPath, String eigenScratchPath, String basicScratchAddress, String allCompoundScratchAddress, String contractClass, String contractSubLabel, String priceBarType) {
        this.downloadDataPath = downloadDataPath;
        this.eigenScratchPath = eigenScratchPath;
        this.basicScratchAddress = basicScratchAddress;
        this.allCompoundScratchAddress = allCompoundScratchAddress;
        this.contractClass = contractClass;
        this.contractSubLabel = contractSubLabel;
        this.priceBarType = priceBarType;
    }

    public void dataHandle(){
        InAndOutHandle inAndOutHandle=new InAndOutHandleImpl();
        PatternStats patternStats=new PatternStatsImpl();
        PivotHandle pivotHandle=new PivotHandleImpl();
        List<HighLowPrice> highLowPrices=new ArrayList<>();
        String priceBarAddress=downloadDataPath+contractClass+"\\"+contractClass+contractSubLabel+priceBarType+".csv";
        String eigenScratchAddress=eigenScratchPath+contractClass+contractSubLabel+priceBarType+".csv";
        if(priceBarType=="m"||priceBarType=="w"||priceBarType=="d"){
            highLowPrices = inAndOutHandle.readBarFromCSV(priceBarAddress);
        }else {
            highLowPrices = inAndOutHandle.readDataFromIBCSV(priceBarAddress);
        }
        List<Scratch> scratchList = pivotHandle.findScratches(highLowPrices, 1,new Scratch(highLowPrices.get(0)),new Scratch(highLowPrices.get(0)),0,0);
        System.out.println("Size of basicScratch for "+contractClass+contractSubLabel+priceBarType+" is: "+scratchList.size());
        inAndOutHandle.saveScratchListToCSV(scratchList,basicScratchAddress);

        List<Pivot> allPivotList=pivotHandle.findAllPivotsByScratch(inAndOutHandle.readScratchFromCSV(basicScratchAddress));
        List<Scratch> allCompoundScratches=new ArrayList<>();
        for(Pivot pivot:allPivotList){
            Scratch scratch=new Scratch(pivot);
            allCompoundScratches.add(scratch);
        }
        allCompoundScratches.addAll(inAndOutHandle.readScratchFromCSV(basicScratchAddress));
        allCompoundScratches.sort(Comparator.comparingInt(Scratch::getStartId).thenComparingInt(Scratch::getLength)); //Sorted by StartId and Length;
        inAndOutHandle.saveScratchListToCSV(allCompoundScratches,allCompoundScratchAddress);
        List<Pivot> keyPivotList=pivotHandle.obtainKeyPivots(allPivotList);
        List<Pivot> pivotsForPatternSearch=pivotHandle.addScratchtoPivot(inAndOutHandle.readScratchFromCSV(basicScratchAddress),keyPivotList);
        pivotsForPatternSearch.sort(Comparator.comparingInt(Pivot::getStartId));

        List<Scratch> eigenScratches=pivotHandle.findEigenScratches(pivotsForPatternSearch);
        System.out.println("Size of eigenScratches "+eigenScratches.size());
        inAndOutHandle.saveScratchListToCSV(eigenScratches,eigenScratchAddress);

        List<Pivot> pivotsof2ndPattern=pivotHandle.find2ndPattern(pivotsForPatternSearch,inAndOutHandle.readScratchFromCSV(allCompoundScratchAddress));
        System.out.println("Size of pivotsof2ndPattern "+pivotsof2ndPattern.size());
        List<Pivot> pivotsof2ndForStats=pivotHandle.findEarningScratch(pivotsof2ndPattern,highLowPrices,inAndOutHandle.readScratchFromCSV(allCompoundScratchAddress));
        System.out.println("Size of pivotsof2ndForStats "+pivotsof2ndForStats.size());
        patternStats.statsofGainExtension(pivotsof2ndForStats);

        List<Pivot> pivotsof3rdPattern=pivotHandle.find3rdPattern(pivotsForPatternSearch,inAndOutHandle.readScratchFromCSV(allCompoundScratchAddress));
        System.out.println("Size of pivotsof3rdPattern is "+pivotsof3rdPattern.size());
        List<Pivot> pivotsof3rdForStats=pivotHandle.findEarningScratch(pivotsof3rdPattern,highLowPrices,inAndOutHandle.readScratchFromCSV(allCompoundScratchAddress));
        System.out.println("Size of pivotsof3rdForStats "+pivotsof3rdForStats.size());
        patternStats.statsofGainExtension(pivotsof3rdForStats);
        /*List<Pivot> pivotsof4thPattern=pivotHandle.find4thPattern(pivotsForPatternSearch,allCompoundScratches);
        System.out.println("Size of pivotsof4thPattern is "+pivotsof4thPattern.size());
        for (Pivot pivot:pivotsof4thPattern){
            System.out.println(pivot.getScratches().toString());
        }*/
    }
}
