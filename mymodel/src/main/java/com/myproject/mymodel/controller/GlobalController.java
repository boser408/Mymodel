package com.myproject.mymodel.controller;

import com.myproject.mymodel.domain.Pivot;
import com.myproject.mymodel.domain.Scratch;
import com.myproject.mymodel.mapper.HighLowPriceMapper;
import com.myproject.mymodel.mapper.ScratchMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;
import service.PatternStats;
import service.PivotHandle;
import service.impl.PatternStatsImpl;
import service.impl.PivotHandleImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Repository
public class GlobalController {
    @Autowired
    private HighLowPriceMapper highLowPriceMapper;
    @Autowired
    private ScratchMapper scratchMapper;
    @Bean
    public void findBasicScratches(){ // Create the table of "findscratch"

        PivotHandle pivotHandle=new PivotHandleImpl();
        PatternStats patternStats=new PatternStatsImpl();
        List<Scratch> basicScratchList = pivotHandle.findScratches(highLowPriceMapper.selectHighLow(), 1, highLowPriceMapper.selectHighLow().size());
        System.out.println("scratchList size is: "+basicScratchList.size());
        int endofList=basicScratchList.size()-1;
        for(int n=0;n<endofList;n++){          // Asign direction to all scratches;
            if(basicScratchList.get(n).getStatus()==1){
                basicScratchList.get(n).setStatus(2);
            }else if(basicScratchList.get(n).getStatus()==-1){
                basicScratchList.get(n).setStatus(-2);
            }else if(basicScratchList.get(n).getStatus()==0){
                if(basicScratchList.get(n).getHigh()==basicScratchList.get(n+1).getHigh()){
                    basicScratchList.get(n).setStatus(1);
                }else {
                    basicScratchList.get(n).setStatus(-1);
                }
            }
        }
        if(basicScratchList.get(endofList).getHigh()==basicScratchList.get(endofList-1).getHigh()){ //Asign direction to scratches whose status is 0;
            basicScratchList.get(endofList).setStatus(-1);
        }else {
            basicScratchList.get(endofList).setStatus(1);
        }
        for(int n=1;n<basicScratchList.size()-1;n++){
            boolean crite1=basicScratchList.get(n).getHigh()==basicScratchList.get(n+1).getHigh() && basicScratchList.get(n).getLow()==basicScratchList.get(n-1).getLow();
            boolean crite2=basicScratchList.get(n).getHigh()==basicScratchList.get(n-1).getHigh() && basicScratchList.get(n).getLow()==basicScratchList.get(n+1).getLow();
            if(!crite1 && !crite2) {
                System.out.println("Check Data with scratch id ="+basicScratchList.get(n).toString());
            }
        }

        scratchMapper.deleteAll("findscratch");
        scratchMapper.batchinsert(basicScratchList);

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
    }

}
