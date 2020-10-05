package com.myproject.mymodel;

import com.myproject.mymodel.domain.*;
import com.myproject.mymodel.mapper.HighLowPriceMapper;
import com.myproject.mymodel.mapper.ScratchMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import service.PivotHandle;
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
    void findScratches(){ // Create the table of "findscratch"
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
        if(scratchList.get(endofList).getHigh()==scratchList.get(endofList-1).getHigh()){
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
        scratchMapper.batchinsert(scratchList);
    }
    @Test
    void findAllPivotsByScratch(){
        PivotHandle pivotHandle=new PivotHandleImpl();
        List<Pivot> allPivotList=pivotHandle.findAllPivotsByScratch(scratchMapper.selectAllScratches());
        System.out.println("Size of allPivotList 111 is "+allPivotList.size());

        List<Scratch> allCompoundScratches=new ArrayList<>();
        for(Pivot pivot:allPivotList){
            Scratch scratch=new Scratch(pivot);
            allCompoundScratches.add(scratch);
        }
        allCompoundScratches.addAll(scratchMapper.selectAllScratches());
        allCompoundScratches.sort(Comparator.comparingInt(Scratch::getStartId).thenComparingInt(Scratch::getLength));
        System.out.println("Size of allCompoundScratches 111 is "+allCompoundScratches.size());
        //scratchMapper.batchtmpinsert(allCompoundScratches);
        List<Pivot> keyPivotList=pivotHandle.obtainKeyPivots(allPivotList);
        System.out.println("Size of keyPivotList is "+keyPivotList.size());

        System.out.println("Size of allPivotList 222 is "+allPivotList.size());
        List<Pivot> pivotsForPatternSearch=pivotHandle.addScratchtoPivot(scratchMapper.selectAllScratches(),keyPivotList);
        pivotsForPatternSearch.sort(Comparator.comparingInt(Pivot::getStartId));
        System.out.println("Size of pivotsForPatternSearch is "+pivotsForPatternSearch.size());
        for(Pivot pivot:pivotsForPatternSearch){
            System.out.println(pivot.toString());
        }
        //List<Dpattern> dpatternList=pivotHandle.findAllDpattern(pivotsForPatternSearch);

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
        List<Pivot> pivotsof2ndPattern=pivotHandle.find2ndPattern(pivotsForPatternSearch,allCompoundScratches);
        for (Pivot pivot:pivotsof2ndPattern){
            System.out.println(pivot.toString());
        }
       /* List<Pivot> pivotsof3rdPattern=pivotHandle.find3rdPattern(pivotsForPatternSearch,allCompoundScratches);
        System.out.println("Size of pivotsof3rdPattern is "+pivotsof3rdPattern.size());
        for (Pivot pivot:pivotsof3rdPattern){
            System.out.println(pivot.toString());
        }*/

       /* for(Pivot pivot:pivotsForPatternSearch){
            System.out.println(pivot.toString());
        }*/
        /*List<PatternResult> firstResult=new ArrayList<>();
        for(Dpattern dpattern:dpatternList){
            for (Pivot pivot:dpattern.getFeaturePivots()){
                if(pivot.getScratches().size()==4){
                    PatternResult patternResult=new PatternResult(pivot.getScratches().get(3),pivot.getScratches().get(0),pivot.getScratches().get(1),pivot.getScratches().get(2));
                    firstResult.add(patternResult);
                    System.out.println(patternResult.toString());
                    for(Scratch scratch:pivot.getScratches()){
                        System.out.println(scratch.toString());
                    }
                }
            }
        }*/
    }
}




