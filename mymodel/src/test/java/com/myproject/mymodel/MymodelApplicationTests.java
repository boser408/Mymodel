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
        List<Scratch> scratchList = pivotHandle.findScratches(highLowPrices, 1, highLowPrices.size(), 6);
        System.out.println(scratchList.size());
        for(int n=0;n<scratchList.size()-1;n++){          // Asign direction to all scratches;
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
        for(int n=1;n<scratchList.size()-1;n++){
            boolean crite1=scratchList.get(n).getHigh()==scratchList.get(n+1).getHigh() && scratchList.get(n).getLow()==scratchList.get(n-1).getLow();
            boolean crite2=scratchList.get(n).getHigh()==scratchList.get(n-1).getHigh() && scratchList.get(n).getLow()==scratchList.get(n+1).getLow();
            if(!crite1 && !crite2) {
                System.out.println("Check Data with scratch id ="+scratchList.get(n).toString());
            }
        }
       // System.out.println("Data is Clean!");
        for(Scratch scratch:scratchList){
            System.out.println(scratch.toString());
        }
        scratchMapper.batchinsert(scratchList);
    }
    @Test
    void findAllPivotsByScratch(){
        PivotHandle pivotHandle=new PivotHandleImpl();
        List<Scratch> allScratches=scratchMapper.selectAllScratches();
        System.out.println("Size of allScratches "+allScratches.size());
        List<Pivot> allPivotList=pivotHandle.findAllPivotsByScratch(allScratches);
        List<Pivot> keyPivotList=pivotHandle.obtainKeyPivots(allPivotList);
        allScratches=scratchMapper.selectAllScratches();
        System.out.println("Size of allScratches "+allScratches.size());
        List<Pivot> pivotsForPatternSearch=pivotHandle.addScratchtoPivot(allScratches,keyPivotList);
        List<Dpattern> dpatternList=pivotHandle.findAllDpattern(pivotsForPatternSearch);
        List<PatternResult> firstResult=new ArrayList<>();
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
        }
    }
}




