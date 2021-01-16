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
    private String operatDataPath;
    private String eigenScratchPath;
    private String basicScratchAddress;
    private String allScratchAddress;
    private String contractClass; //Ticker like: ES, NQ, YM, RTY, GC, GLD...
    private String priceBar; //weekly,daily,hour,min;

    public GlobalController(String operatDataPath, String eigenScratchPath, String basicScratchAddress, String contractClass, String priceBar) {
        this.operatDataPath = operatDataPath;
        this.eigenScratchPath = eigenScratchPath;
        this.basicScratchAddress = basicScratchAddress+"basic"+contractClass+priceBar+".csv";
        this.allScratchAddress = basicScratchAddress+"all"+contractClass+priceBar+".csv";
        this.contractClass = contractClass;
        this.priceBar = priceBar;
    }

    public void createDailyEigenScratch(){
        InAndOutHandle inAndOutHandle=new InAndOutHandleImpl();
        PivotHandle pivotHandle=new PivotHandleImpl();
        List<HighLowPrice> highLowPrices=new ArrayList<>();
        String priceBarAddress=operatDataPath+contractClass+priceBar+".csv";
        String eigenScratchAddress=eigenScratchPath+contractClass+priceBar+".csv";
        if(priceBar=="m"||priceBar=="w"||priceBar=="d"){
            highLowPrices = inAndOutHandle.readBarFromCSV(priceBarAddress);
        }else {
            highLowPrices = inAndOutHandle.readDataFromIBCSV(priceBarAddress);
        }
        List<Scratch> scratchList = pivotHandle.findScratches(highLowPrices, 1,new Scratch(highLowPrices.get(0)),new Scratch(highLowPrices.get(0)),0,0);
        System.out.println("Size of basicScratch for "+contractClass+priceBar+" is: "+scratchList.size());
        inAndOutHandle.saveScratchListToCSV(scratchList,basicScratchAddress);

        List<Pivot> allPivotList=pivotHandle.findAllPivotsByScratch(inAndOutHandle.readScratchFromCSV(basicScratchAddress));
        List<Scratch> allCompoundScratches=new ArrayList<>();
        for(Pivot pivot:allPivotList){
            Scratch scratch=new Scratch(pivot);
            allCompoundScratches.add(scratch);
        }
        allCompoundScratches.addAll(inAndOutHandle.readScratchFromCSV(basicScratchAddress));
        allCompoundScratches.sort(Comparator.comparingInt(Scratch::getStartId).thenComparingInt(Scratch::getLength)); //Sorted by StartId and Length;
        inAndOutHandle.saveScratchListToCSV(allCompoundScratches,allScratchAddress);
        List<Pivot> keyPivotList=pivotHandle.obtainKeyPivots(allPivotList);
        List<Pivot> pivotsForPatternSearch=pivotHandle.addScratchtoPivot(inAndOutHandle.readScratchFromCSV(basicScratchAddress),keyPivotList);
        pivotsForPatternSearch.sort(Comparator.comparingInt(Pivot::getStartId));

        List<Scratch> eigenScratches=pivotHandle.findEigenScratches(pivotsForPatternSearch);
        System.out.println("Size of eigenScratches "+eigenScratches.size());
        inAndOutHandle.saveScratchListToCSV(eigenScratches,eigenScratchAddress);
    }

    public void dataHandle(){
        InAndOutHandle inAndOutHandle=new InAndOutHandleImpl();
        PatternStats patternStats=new PatternStatsImpl();
        PivotHandle pivotHandle=new PivotHandleImpl();
        List<HighLowPrice> highLowPrices=new ArrayList<>();
        String priceBarAddress=operatDataPath+contractClass+priceBar+".csv";
        String eigenScratchAddress=eigenScratchPath+contractClass+priceBar+".csv";
        if(priceBar=="m"||priceBar=="w"||priceBar=="d"){
            highLowPrices = inAndOutHandle.readBarFromCSV(priceBarAddress);
        }else {
            highLowPrices = inAndOutHandle.readDataFromIBCSV(priceBarAddress);
        }
        List<Scratch> scratchList = pivotHandle.findScratches(highLowPrices, 1,new Scratch(highLowPrices.get(0)),new Scratch(highLowPrices.get(0)),0,0);
        System.out.println("Size of basicScratch for "+contractClass+priceBar+" is: "+scratchList.size());
        inAndOutHandle.saveScratchListToCSV(scratchList,basicScratchAddress);

        List<Pivot> allPivotList=pivotHandle.findAllPivotsByScratch(inAndOutHandle.readScratchFromCSV(basicScratchAddress));
        List<Scratch> allCompoundScratches=new ArrayList<>();
        for(Pivot pivot:allPivotList){
            Scratch scratch=new Scratch(pivot);
            allCompoundScratches.add(scratch);
        }
        allCompoundScratches.addAll(inAndOutHandle.readScratchFromCSV(basicScratchAddress));
        allCompoundScratches.sort(Comparator.comparingInt(Scratch::getStartId).thenComparingInt(Scratch::getLength)); //Sorted by StartId and Length;
        inAndOutHandle.saveScratchListToCSV(allCompoundScratches,allScratchAddress);
        List<Pivot> keyPivotList=pivotHandle.obtainKeyPivots(allPivotList);
        List<Pivot> pivotsForPatternSearch=pivotHandle.addScratchtoPivot(inAndOutHandle.readScratchFromCSV(basicScratchAddress),keyPivotList);
        pivotsForPatternSearch.sort(Comparator.comparingInt(Pivot::getStartId));

        List<Scratch> eigenScratches=pivotHandle.findEigenScratches(pivotsForPatternSearch);
        System.out.println("Size of eigenScratches "+eigenScratches.size());
        inAndOutHandle.saveScratchListToCSV(eigenScratches,eigenScratchAddress);

        List<Pivot> pivotsof2ndPattern=pivotHandle.find2ndPattern(pivotsForPatternSearch,inAndOutHandle.readScratchFromCSV(allScratchAddress));
        System.out.println("Size of pivotsof2ndPattern "+pivotsof2ndPattern.size());
        List<Pivot> pivotsof2ndForStats=pivotHandle.findEarningScratch(pivotsof2ndPattern,highLowPrices,inAndOutHandle.readScratchFromCSV(allScratchAddress));
        System.out.println("Size of pivotsof2ndForStats "+pivotsof2ndForStats.size());
        patternStats.statsofGainExtension(pivotsof2ndForStats);

        List<Pivot> pivotsof3rdPattern=pivotHandle.find3rdPattern(pivotsForPatternSearch,inAndOutHandle.readScratchFromCSV(allScratchAddress));
        System.out.println("Size of pivotsof3rdPattern is "+pivotsof3rdPattern.size());
        List<Pivot> pivotsof3rdForStats=pivotHandle.findEarningScratch(pivotsof3rdPattern,highLowPrices,inAndOutHandle.readScratchFromCSV(allScratchAddress));
        System.out.println("Size of pivotsof3rdForStats "+pivotsof3rdForStats.size());
        patternStats.statsofGainExtension(pivotsof3rdForStats);
        /*List<Pivot> pivotsof4thPattern=pivotHandle.find4thPattern(pivotsForPatternSearch,allCompoundScratches);
        System.out.println("Size of pivotsof4thPattern is "+pivotsof4thPattern.size());
        for (Pivot pivot:pivotsof4thPattern){
            System.out.println(pivot.getScratches().toString());
        }*/
    }
}
