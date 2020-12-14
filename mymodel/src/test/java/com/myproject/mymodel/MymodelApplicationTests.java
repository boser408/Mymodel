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
    void findbasicScratches(){ // Create the table of "findscratch"
        InAndOutHandle inAndOutHandle=new InAndOutHandleImpl();
        List<HighLowPrice> highLowPrices = inAndOutHandle.readBarFromCSV("C:\\Users\\bjsh2\\Documents\\Investment\\Data\\spxw1927.csv");
        PivotHandle pivotHandle=new PivotHandleImpl();
        List<Scratch> scratchList = pivotHandle.findScratches(highLowPrices, 1, highLowPrices.size());
        System.out.println(scratchList.size());
        int endofList=scratchList.size()-1;
        for(int n=0;n<endofList;n++){          // Asign direction to all scratches;
            if(scratchList.get(n).getStatus()==1){
                scratchList.get(n).setStatus(2);
            }else if(scratchList.get(n).getStatus()==-1){
                scratchList.get(n).setStatus(-2);
            }else if(scratchList.get(n).getStatus()==0){
                if(scratchList.get(n).getHigh()==scratchList.get(n+1).getHigh()){
                    scratchList.get(n).setStatus(1);
                }else {
                    scratchList.get(n).setStatus(-1);
                }
            }
        }
        if(scratchList.get(endofList).getHigh()==scratchList.get(endofList-1).getHigh()){ //Asign direction to scratches whose status is 0;
            scratchList.get(endofList).setStatus(-1);
        }else {
            scratchList.get(endofList).setStatus(1);
        }
        for(int n=1;n<scratchList.size()-1;n++){
            boolean crite1=scratchList.get(n).getHigh()==scratchList.get(n+1).getHigh() && scratchList.get(n).getLow()==scratchList.get(n-1).getLow();
            boolean crite2=scratchList.get(n).getHigh()==scratchList.get(n-1).getHigh() && scratchList.get(n).getLow()==scratchList.get(n+1).getLow();
            if(!crite1 && !crite2) {
                System.out.println("Check Data with scratch id ="+scratchList.get(n).toString());
            }
        }
       // System.out.println("Data is Clean!");
        /*for(Scratch scratch:scratchList){
            System.out.println(scratch.toString());
        }*/
        inAndOutHandle.saveScratchListToCSV(scratchList,"E:\\out\\tryWrite\\basicScratch.csv");
    }
    @Test
    void findAllPattern(){
        InAndOutHandle inAndOutHandle=new InAndOutHandleImpl();

        String priceBarAddress="C:\\Users\\bjsh2\\Documents\\Investment\\Data\\spxw1927.csv";
        String basicScratchAddress="E:\\out\\tryWrite\\basicScratch.csv";
        String allCompoundScratchAddress="E:\\out\\tryWrite\\AllCompoundScratch.csv";

        PivotHandle pivotHandle=new PivotHandleImpl();
        PatternStats patternStats=new PatternStatsImpl();
        List<Pivot> allPivotList=pivotHandle.findAllPivotsByScratch(inAndOutHandle.readScratchFromCSV(basicScratchAddress));
        List<Scratch> allCompoundScratches=new ArrayList<>();
        for(Pivot pivot:allPivotList){
            Scratch scratch=new Scratch(pivot);
            allCompoundScratches.add(scratch);
        }
        allCompoundScratches.addAll(inAndOutHandle.readScratchFromCSV(basicScratchAddress));
        allCompoundScratches.sort(Comparator.comparingInt(Scratch::getStartId).thenComparingInt(Scratch::getLength)); //Sorted by StartId and Length;
        inAndOutHandle.saveScratchListToCSV(allCompoundScratches,"E:\\out\\tryWrite\\AllCompoundScratch.csv");
        List<Pivot> keyPivotList=pivotHandle.obtainKeyPivots(allPivotList);
        List<Pivot> pivotsForPatternSearch=pivotHandle.addScratchtoPivot(inAndOutHandle.readScratchFromCSV(basicScratchAddress),keyPivotList);
        pivotsForPatternSearch.sort(Comparator.comparingInt(Pivot::getStartId));

        List<Pivot> pivotsof2ndPattern=pivotHandle.find2ndPattern(pivotsForPatternSearch,inAndOutHandle.readScratchFromCSV(allCompoundScratchAddress));
        System.out.println("Size of pivotsof2ndPattern "+pivotsof2ndPattern.size());
        List<Pivot> pivotsof2ndForStats=pivotHandle.findEarningScratch(pivotsof2ndPattern,inAndOutHandle.readBarFromCSV(priceBarAddress),inAndOutHandle.readScratchFromCSV(allCompoundScratchAddress));
        System.out.println("Size of pivotsof2ndForStats "+pivotsof2ndForStats.size());
        patternStats.statsofGainExtension(pivotsof2ndForStats);

        List<Pivot> pivotsof3rdPattern=pivotHandle.find3rdPattern(pivotsForPatternSearch,inAndOutHandle.readScratchFromCSV(allCompoundScratchAddress));
        System.out.println("Size of pivotsof3rdPattern is "+pivotsof3rdPattern.size());
        List<Pivot> pivotsof3rdForStats=pivotHandle.findEarningScratch(pivotsof3rdPattern,inAndOutHandle.readBarFromCSV(priceBarAddress),inAndOutHandle.readScratchFromCSV(allCompoundScratchAddress));
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




