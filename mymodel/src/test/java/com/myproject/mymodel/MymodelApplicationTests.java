package com.myproject.mymodel;

import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Pivot;
import com.myproject.mymodel.domain.Scratch;
import org.junit.jupiter.api.Test;
import service.InAndOutHandle;
import service.PatternStats;
import service.PivotHandle;
import service.impl.InAndOutHandleImpl;
import service.impl.PatternStatsImpl;
import service.impl.PivotHandleImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

class MymodelApplicationTests {
    @Test
    void trySummaryStats(){
        InAndOutHandle inAndOutHandle=new InAndOutHandleImpl();
        List<HighLowPrice> highLowPrices = inAndOutHandle.readBarFromCSV("C:\\Users\\bjsh2\\Documents\\Investment\\Data\\spxw1927.csv");
        /* DoubleSummaryStatistics priceMax= highLowPrices.stream().collect(Collectors.summarizingDouble(HighLowPrice::getHigh));
        Optional<HighLowPrice> optionalHighLowPrice= highLowPrices.stream().min(Comparator.comparingDouble(HighLowPrice::getLow));
        HighLowPrice minItem= optionalHighLowPrice.get();
        System.out.println(priceMax);
        System.out.println(minItem.toString()); */
        int pivotLength=6; // Definition of the length of shortest bar pivot;
        int bottom = highLowPrices.size()-(pivotLength-1);
        for(int n=0;n<bottom;n++){
            int i=n+1;
            Optional<HighLowPrice> max = highLowPrices.stream().filter(highLowPrice -> highLowPrice.getId()>=i && highLowPrice.getId() <=i+(pivotLength-1))
                    .max(Comparator.comparingDouble(HighLowPrice::getHigh));
            Optional<HighLowPrice> min = highLowPrices.stream().filter(highLowPrice -> highLowPrice.getId()>=i && highLowPrice.getId() <=i+(pivotLength-1))
                    .min(Comparator.comparingDouble(HighLowPrice::getLow));
            highLowPrices.get(n).setNdhigh(max.get().getHigh());
            highLowPrices.get(n).setNdlow(min.get().getLow());

        }
    }
    @Test
    void myModel(){ // Create the table of "findscratch
        String addressAdjust="d";
        String priceBarAddress="C:\\Users\\bjsh2\\Documents\\Investment\\Data\\spx"+addressAdjust+"1927.csv";
        String basicScratchAddress="E:\\out\\tryWrite\\basicScratch.csv";
        String allCompoundScratchAddress="E:\\out\\tryWrite\\AllCompoundScratch.csv";
        String eigenScratchAddress="C:\\Users\\bjsh2\\Documents\\Investment\\Data\\EigenScratch\\eigenScrachspx"+addressAdjust+"1927.csv";

        InAndOutHandle inAndOutHandle=new InAndOutHandleImpl();
        PatternStats patternStats=new PatternStatsImpl();
        PivotHandle pivotHandle=new PivotHandleImpl();

        List<HighLowPrice> highLowPrices = inAndOutHandle.readBarFromCSV(priceBarAddress);
        //List<HighLowPrice> highLowPrices = inAndOutHandle.readDataFromIBCSV("C:\\Users\\bjsh2\\Documents\\Investment\\Data\\Futures\\ESZ01min.csv");

        List<Scratch> scratchList = pivotHandle.findScratches(highLowPrices, 1,new Scratch(highLowPrices.get(0)),new Scratch(highLowPrices.get(0)),0,0);
        System.out.println(scratchList.size());
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

        /* System.out.println("Size of allCompoundScratches 222 is "+allCompoundScratches.size());
        List<Scratch> scratchList=new ArrayList<>();
        Scratch scratch000=new Scratch();
        scratch000.setLength(0);
        for(Pivot pivot:pivotsForPatternSearch){
            Scratch scratch=new Scratch(pivot);
            scratchList.add(scratch);
            scratchList.addAll(pivot.getScratches());
            scratchList.add(scratch000);
        }
        scratchMapper.batchsmallinsert(scratchList);*/
    }


}




