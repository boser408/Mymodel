package com.myproject.mymodel;

import com.myproject.mymodel.domain.Dpattern;
import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Pivot;
import com.myproject.mymodel.domain.Scratch;
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
        List<Scratch> listofsmall=scratchMapper.selectAllScratches();
        List<Pivot> allPivotList=pivotHandle.findAllPivotsByScratch(listofsmall);
        System.out.println("size of table "+allPivotList.size());
        allPivotList.sort(Comparator.comparingInt(Pivot::getStartId));
        /*for(Pivot pivot: allPivotList){
            pivot.getScratches().sort(Comparator.comparingInt(Scratch::getStartId));
        }*/
        List<Pivot> keyPivotList=pivotHandle.obtainKeyPivots(allPivotList);
        for(Pivot pivot:keyPivotList){
            System.out.println(pivot.toString());
           /*for(Scratch scratch:pivot.getScratches()){
               System.out.println(scratch.toString());
           }*/
        }

    }
    @Test
    void produceScratchTable(){ // find all the basic pivots combined by scratches
        List<Scratch> listofsmall=scratchMapper.selectAllScratches();
        PivotHandle pivotHandle=new PivotHandleImpl();
        List<Pivot> pivotList=new ArrayList<>();
        Pivot currentpivot=new Pivot(listofsmall.get(0));

        int n=1;
        while (n<listofsmall.size()-2){
            if(currentpivot.getPivotType()!=0){
                List<Pivot> extendedpivots=pivotHandle.pivotExtension(listofsmall,currentpivot,n);
                Pivot savepivot=new Pivot(extendedpivots.get(0));
                pivotList.add(savepivot);
                currentpivot=new Pivot(extendedpivots.get(1));
                n=pivotHandle.getNumberofLoop()+1;
                while (n<listofsmall.size()-2 && currentpivot.getScratches().size()==0){
                    if((listofsmall.get(n).getStatus()==1 && listofsmall.get(n).getHigh()>currentpivot.getHigh()) ||
                            (listofsmall.get(n).getStatus()==-1 && listofsmall.get(n).getLow()<currentpivot.getLow())||
                            listofsmall.get(n).getStatus()==0){
                        Pivot tmppivot=new Pivot(currentpivot);
                        pivotList.add(tmppivot);
                        System.out.println("pExtension stage tmppivot added:"+tmppivot.toString());
                        currentpivot.setLength(listofsmall.get(n).getLength());
                        currentpivot.setStartId(listofsmall.get(n).getStartId());
                        currentpivot.setHigh(listofsmall.get(n).getHigh());
                        currentpivot.setLow(listofsmall.get(n).getLow());
                        n=n+1;
                    }else {
                        break;
                    }
                }
                if(n==listofsmall.size()){
                    break;
                }
                System.out.println("pExtension: savpivot="+savepivot.toString());
                System.out.println("pExtension: currentpivot="+currentpivot.toString());
                System.out.println("pExtension: pivot of n="+listofsmall.get(n).toString());
            }else {
                List<Pivot> simplepivots = pivotHandle.findPivots(listofsmall,n);
                Pivot savepivot=new Pivot();
                Pivot tmppivot=new Pivot();
                int i=0;
                if(simplepivots.size()==2){

                    if(simplepivots.get(0).getStartId()<simplepivots.get(1).getStartId()){
                        savepivot=new Pivot(simplepivots.get(i));
                        pivotList.add(savepivot);
                        i=1;
                    }else {
                        i=1;
                        savepivot=new Pivot(simplepivots.get(i));
                        pivotList.add(savepivot);
                        i=0;
                    }

                }
                n=pivotHandle.getNumberofLoop()+1;
                currentpivot=new Pivot(simplepivots.get(i));

                while (n<listofsmall.size()-2 && currentpivot.getScratches().size()==0){
                    if((listofsmall.get(n).getStatus()==1 && listofsmall.get(n).getHigh()>currentpivot.getHigh()) ||
                            (listofsmall.get(n).getStatus()==-1 && listofsmall.get(n).getLow()<currentpivot.getLow())||
                            listofsmall.get(n).getStatus()==0){
                        tmppivot=new Pivot(currentpivot);
                        pivotList.add(tmppivot);
                        System.out.println("pFinding stage tmppivot added:"+tmppivot.toString());
                        currentpivot.setLength(listofsmall.get(n).getLength());
                        currentpivot.setStartId(listofsmall.get(n).getStartId());
                        currentpivot.setHigh(listofsmall.get(n).getHigh());
                        currentpivot.setLow(listofsmall.get(n).getLow());
                        n=n+1;
                    }else {
                        break;
                    }
                }
                if(n==listofsmall.size()){
                    break;
                }
                System.out.println("pFinding: savpivot="+savepivot.toString());
                System.out.println("pFinding: tmppivot="+tmppivot.toString());
                System.out.println("pFinding: currentpivot="+currentpivot.toString());
            }
        }
        for(Pivot pivot:pivotList){
            if(pivot.getPivotType()==0 && pivot.getScratches().size()>0){
                pivot.getScratches().clear();
            }
            //System.out.println(pivot.toString());
        }
        System.out.println("Size of pivotList is:"+pivotList.size());

        for( n=1;n<pivotList.size()-1;n++){
            boolean crite1=pivotList.get(n).getHigh()==pivotList.get(n+1).getHigh() && pivotList.get(n).getLow()==pivotList.get(n-1).getLow();
            boolean crite2=pivotList.get(n).getHigh()==pivotList.get(n-1).getHigh() && pivotList.get(n).getLow()==pivotList.get(n+1).getLow();
            if(!crite1 && !crite2) {
                System.out.println("Check Data of this pivot : "+pivotList.get(n).toString());
            }
        }
        /*for( n=0;n<pivotList.size()-1;n++){          // Asign direction of pivots whose pivotType=0;
            if(pivotList.get(n).getPivotType()==0){
                if(pivotList.get(n).getHigh()==pivotList.get(n+1).getHigh()){
                    pivotList.get(n).setPivotType(1);
                }else {
                    pivotList.get(n).setPivotType(-1);
                }
            }
        }*/
        List<Scratch> scratchList=new ArrayList<>();
        for(Pivot pivot:pivotList){
            Scratch scratch=new Scratch(pivot);
            scratchList.add(scratch);
        }
        scratchMapper.batchsmallinsert(scratchList);
        /*for(Pivot pivot:pivotList){
            if(!pivot.getScratches().isEmpty()){
                pivot.getScratches().sort(Comparator.comparingInt(Scratch::getStartId));
            }

            System.out.println(pivot.toString());
        }*/
       // List<Pivot> magaPivotList = pivotHandle.findMagaPivotList(pivotList);
        /*List<Scratch> allExtendScratch=new ArrayList<>();
        for(Pivot pivot:pivotList){
            Scratch scratch=new Scratch(pivot);
            if(pivot.getPivotType()==0){

            }
            allExtendScratch.add(scratch);
        }*/

        /*List<Dpattern> finalDpatternList=new ArrayList<>();
        for(Pivot pivot:magaPivotList){
            if(pivot.getScratches().size()>1){
                Dpattern returndpattern=pivotHandle.findDpattern(pivot);
                if(returndpattern.getFeaturePivots().size()>=1){
                    finalDpatternList.add(returndpattern);
                }
            }
        }*/
      //  System.out.println("Size of finalDpatternList="+finalDpatternList.size());
        /*List<Dpattern> finalTpatternList=new ArrayList<>();
        for(Pivot pivot:magaPivotList){
            if (pivot.getScratches().size()>2){
                Dpattern tripattern=pivotHandle.findTpattern(pivot);
                if(tripattern.getPivotDirection()>30 || tripattern.getPivotDirection()<-30){
                    finalTpatternList.add(tripattern);
                }
            }
        }
        nn=1;
        for(Dpattern tpattern:finalTpatternList){
            System.out.println("Pivots in "+nn+"th tpattern is");
            for(Pivot pivot:tpattern.getFeaturePivots()){
                System.out.println("Pivot="+pivot.toString());
            }
            nn=nn+1;
        }*/
        /*List<Pivot> dpatternPivotList=new ArrayList<>();
        for(Dpattern dpattern:finalDpatternList){
            for(Pivot pivot:dpattern.getFeaturePivots()){
                dpatternPivotList.add(pivot);
            }
        }*/
        /*for(Pivot pivot:dpatternPivotList){
            int nloop=0;
            while (nloop<listofsmall.size()){
                boolean criteria=listofsmall.get(nloop).getStatus()*pivot.getScratches().get(1).getStatus()>0;
                double factor=(double) listofsmall.get(nloop).getLength()/pivot.getScratches().get(1).getLength();
                if(listofsmall.get(nloop).getStartId()>pivot.getScratches().get(1).getStartId()
                        && factor>=pivotHandle.getControlFactor()&&criteria){
                    pivot.getScratches().add(2,listofsmall.get(nloop));
                    break;
                }else {n++;}
            }

        }*/
    }
}




