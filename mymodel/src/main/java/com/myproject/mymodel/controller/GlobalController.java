package com.myproject.mymodel.controller;

import com.myproject.mymodel.domain.HighLowPrice;
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
        List<HighLowPrice> highLowPrices=highLowPriceMapper.selectHighLow();
        List<Scratch> basicScratchList = pivotHandle.findScratches(highLowPrices, 1,new Scratch(highLowPrices.get(0)),new Scratch(highLowPrices.get(0)),0,0);
        System.out.println("scratchList size is: "+basicScratchList.size());
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
        List<Pivot> pivotsof2ndForStats=pivotHandle.findEarningScratch(pivotsof2ndPattern,highLowPrices,scratchMapper.selectfromTemp());
        System.out.println("Size of pivotsof2ndForStats "+pivotsof2ndForStats.size());
        patternStats.statsofGainExtension(pivotsof2ndForStats);

        List<Pivot> pivotsof3rdPattern=pivotHandle.find3rdPattern(pivotsForPatternSearch,scratchMapper.selectfromTemp());
        System.out.println("Size of pivotsof3rdPattern is "+pivotsof3rdPattern.size());
        List<Pivot> pivotsof3rdForStats=pivotHandle.findEarningScratch(pivotsof3rdPattern,highLowPrices,scratchMapper.selectfromTemp());
        System.out.println("Size of pivotsof3rdForStats "+pivotsof3rdForStats.size());
        patternStats.statsofGainExtension(pivotsof3rdForStats);
    }

}
