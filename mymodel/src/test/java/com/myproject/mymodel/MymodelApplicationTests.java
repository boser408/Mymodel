package com.myproject.mymodel;

import com.myproject.mymodel.controller.GlobalController;
import com.myproject.mymodel.domain.*;
import com.myproject.mymodel.mapper.HighLowPriceMapper;
import com.myproject.mymodel.mapper.ScratchMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import service.PatternStats;
import service.PivotHandle;
import service.impl.PatternStatsImpl;
import service.impl.PivotHandleImpl;

import java.util.*;

@SpringBootTest
class MymodelApplicationTests {
    @Autowired
    private HighLowPriceMapper highLowPriceMapper;
    @Autowired
    private ScratchMapper scratchMapper;
    @Test
    void trySummaryStats(){
        List<HighLowPrice> highLowPrices = highLowPriceMapper.selectHighLow();
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
       // System.out.println(max);
       // System.out.println(min);
       // System.out.println(highLowPrices.get(0).toString());
        highLowPriceMapper.batchinsert(highLowPrices);
    }
    @Test
    void findbasicScratches(){ // Create the table of "findscratch"
        List<HighLowPrice> highLowPrices = highLowPriceMapper.selectHighLow();
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
        scratchMapper.deleteAll("findscratch");
        scratchMapper.batchinsert(scratchList);
    }
    @Test
    void findAllPattern(){
        PivotHandle pivotHandle=new PivotHandleImpl();
        PatternStats patternStats=new PatternStatsImpl();
        List<Pivot> allPivotList=pivotHandle.findAllPivotsByScratch(scratchMapper.selectAllScratches());
        List<Scratch> allCompoundScratches=new ArrayList<>();
        for(Pivot pivot:allPivotList){
            Scratch scratch=new Scratch(pivot);
            allCompoundScratches.add(scratch);
        }
        allCompoundScratches.addAll(scratchMapper.selectAllScratches());
        allCompoundScratches.sort(Comparator.comparingInt(Scratch::getStartId).thenComparingInt(Scratch::getLength)); //Sorted by StartId and Length;
        scratchMapper.deleteAll("tmpscratch");
        scratchMapper.batchtmpinsert(allCompoundScratches);

        List<Pivot> keyPivotList=pivotHandle.obtainKeyPivots(allPivotList);
        List<Pivot> pivotsForPatternSearch=pivotHandle.addScratchtoPivot(scratchMapper.selectAllScratches(),keyPivotList);
        pivotsForPatternSearch.sort(Comparator.comparingInt(Pivot::getStartId));

        List<Pivot> pivotsof2ndPattern=pivotHandle.find2ndPattern(pivotsForPatternSearch,scratchMapper.selectfromTemp());
        System.out.println("Size of pivotsof2ndPattern "+pivotsof2ndPattern.size());
        List<Pivot> pivotsof2ndForStats=pivotHandle.findEarningScratch(pivotsof2ndPattern,highLowPriceMapper.selectHighLow(),scratchMapper.selectfromTemp());
        System.out.println("Size of pivotsof2ndForStats "+pivotsof2ndForStats.size());
        patternStats.statsofGainExtension(pivotsof2ndForStats);

        List<Pivot> pivotsof3rdPattern=pivotHandle.find3rdPattern(pivotsForPatternSearch,scratchMapper.selectfromTemp());
        System.out.println("Size of pivotsof3rdPattern is "+pivotsof3rdPattern.size());
        List<Pivot> pivotsof3rdForStats=pivotHandle.findEarningScratch(pivotsof3rdPattern,highLowPriceMapper.selectHighLow(),scratchMapper.selectfromTemp());
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




