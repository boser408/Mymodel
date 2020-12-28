package service.impl;

import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Pivot;
import com.myproject.mymodel.domain.Scratch;
import service.InAndOutHandle;
import service.PivotHandle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class PivotHandleImpl implements PivotHandle {
    public static final double controlFactor=0.7;
    public static final int pivotLength=5;
    @Override
    public double getControlFactor() {
        return controlFactor;
    }
    @Override
    public int getPivotLength() {
        return pivotLength;
    }
    @Override
    public Scratch checkHiddenScratch(List<HighLowPrice> highLowPrices) {

        int endindex=highLowPrices.size()-1;
        Scratch scratch=new Scratch(highLowPrices.get(endindex));
        int loopEnd = highLowPrices.size()-(pivotLength-1);
        for(int n=0;n<loopEnd;n++){

            List<HighLowPrice> partialList=highLowPrices.subList(n,endindex+1);
            List<HighLowPrice> partialList2=highLowPrices.subList(n+1,endindex+1);
            double valueofHigh=partialList.stream().mapToDouble(HighLowPrice::getHigh).max().getAsDouble();
            double valueofLow=partialList.stream().mapToDouble(HighLowPrice::getLow).min().getAsDouble();
            double subHigh=partialList2.stream().mapToDouble(HighLowPrice::getHigh).max().getAsDouble();
            double subLow=partialList2.stream().mapToDouble(HighLowPrice::getLow).min().getAsDouble();
            float maxHigh=(float)valueofHigh;
            float minLow=(float)valueofLow;
            float okHigh=(float)subHigh;
            float okLow=(float)subLow;
               boolean crite1= maxHigh== partialList.get(0).getHigh();
                boolean crite2= minLow== highLowPrices.get(endindex).getLow();
                if(crite1 && crite2){
                    scratch.setLength(endindex-n+1);
                    scratch.setStartdate(partialList.get(0).getDate());
                    scratch.setStartId(partialList.get(0).getId());
                    scratch.setHigh(partialList.get(0).getHigh());
                    scratch.setStatus(-1);
                    break;
                }
                boolean crite3= maxHigh==highLowPrices.get(endindex).getHigh();
                boolean crite4= minLow==partialList.get(0).getLow();
                boolean c3=okHigh==highLowPrices.get(endindex).getHigh();
                if(crite3 && crite4){


                    scratch.setLength(endindex-n+1);
                    scratch.setStartdate(partialList.get(0).getDate());
                    scratch.setStartId(partialList.get(0).getId());
                    scratch.setLow(partialList.get(0).getLow());
                    scratch.setStatus(1);
                    break;
                }
        }
        return scratch;
    }
    @Override
    public List<Scratch> removeRedundentScratch(List<Scratch> scratchList) {
        List<Scratch> scratchesforReturn=new ArrayList<>();
        scratchList.sort(Comparator.comparingInt(Scratch::getStartId));
        for(int n=0;n<scratchList.size();n++){
            Scratch maxscratch=new Scratch(scratchList.get(n));
            for(int i=n+1;i<scratchList.size();i++){
                if(scratchList.get(i).getStartId()>=maxscratch.getStartId()+maxscratch.getLength()-1){
                    break;
                }
                boolean c=scratchList.get(i).getLength()>maxscratch.getLength();
                if(c){
                    maxscratch=new Scratch(scratchList.get(i));
                }
                scratchList.remove(i);
                i=i-1;
            }
            scratchesforReturn.add(maxscratch);
        }
        return scratchesforReturn;
    }
    @Override
    public List<Scratch> findScratchtoAdd(List<Scratch> allScratches, int nofStart, int nofEnd) {
        List<Scratch> scratchestoReturn=new ArrayList<>();
        for(int n=0;n<allScratches.size();n++){
            if(allScratches.get(n).getStartId()==nofStart){
                int i=n;
                while (i<allScratches.size() && allScratches.get(i).getStartId()+allScratches.get(i).getLength()<=nofEnd){
                    boolean c1=allScratches.get(i).getStatus()*allScratches.get(n).getStatus()<0;
                    boolean c2=allScratches.get(i).getLength()>=pivotLength;
                    if(c1 && c2){
                        scratchestoReturn.add(allScratches.get(i));
                    }
                    i++;
                }
                break;
            }
        }
        return scratchestoReturn;
    }
    @Override
    public List<Pivot> addScratchtoPivot(List<Scratch> allScratches, List<Pivot> keyPivotList) {
        List<Pivot> pivotsForPatternSearch=new ArrayList<>();
        for(Pivot pivot:keyPivotList){
            List<Scratch> scratchestoAdd=new ArrayList<>();
            int nofStart,nofEnd;
            if(pivot.getScratches().size()==1 && pivot.getScratches().get(0).getStatus()==pivot.getPivotType()){//No Feature Scratch in the Pivot;
                pivot.getScratches().clear();
                nofStart=pivot.getStartId();
                nofEnd=pivot.getStartId()+pivot.getLength();
                pivot.getScratches().addAll(findScratchtoAdd(allScratches,nofStart,nofEnd));
            }else { // At least one Feature Scratch in the Pivot;
                nofStart=pivot.getStartId();
                for(int t=0;t<pivot.getScratches().size();t++){
                    Scratch scratch=new Scratch(pivot.getScratches().get(t));
                    nofEnd=scratch.getStartId()+1;
                    scratchestoAdd.addAll(findScratchtoAdd(allScratches,nofStart,nofEnd));
                    nofStart=scratch.getStartId()+scratch.getLength()-1;
                }
                Scratch scratch=new Scratch(pivot.getScratches().get(pivot.getScratches().size()-1));
                nofStart=scratch.getStartId()+scratch.getLength()-1;
                nofEnd=pivot.getStartId()+pivot.getLength();
                scratchestoAdd.addAll(findScratchtoAdd(allScratches,nofStart,nofEnd));
                pivot.getScratches().addAll(scratchestoAdd);
            }
            pivot.getScratches().sort(Comparator.comparingInt(Scratch::getStartId));
            pivotsForPatternSearch.add(pivot);
        }
        for (Pivot pivot:pivotsForPatternSearch){
            for(int n=0;n<pivot.getScratches().size();n++){
                if(pivot.getScratches().get(n).getStatus()*pivot.getPivotType()>0){
                    pivot.getScratches().remove(n);
                    n=n-1;
                }
            }
        }
        return pivotsForPatternSearch;
    }
    @Override
    public Scratch findSubScratch(int startId, int endId, List<HighLowPrice> highLowPrices, int endPatternPivotDirection) {
        Scratch returnScratch=new Scratch(highLowPrices.get(startId));
            for(int n=startId;n<=endId;n++){
                Scratch subScratch=new Scratch(highLowPrices.get(n));
                if(endPatternPivotDirection>0){     //Looking for a downtrend subscratch;
                    if(highLowPrices.get(n+1).getHigh()>highLowPrices.get(n).getHigh()){continue;}
                }else {                            //Looking for an uptrend subscratch;
                    if(highLowPrices.get(n+1).getLow()<highLowPrices.get(n).getLow()){continue;}
                }
                for(int t=2;t<pivotLength;t++){
                    List<HighLowPrice> partialList=highLowPrices.subList(n,n+t);
                    List<HighLowPrice> partialList2=highLowPrices.subList(n+1,n+t);
                    float valueofHigh=(float)partialList.stream().mapToDouble(HighLowPrice::getHigh).max().getAsDouble();
                    float valueofLow=(float)partialList.stream().mapToDouble(HighLowPrice::getLow).min().getAsDouble();
                    float subHigh=(float)partialList2.stream().mapToDouble(HighLowPrice::getHigh).max().getAsDouble();
                    float subLow=(float)partialList2.stream().mapToDouble(HighLowPrice::getLow).min().getAsDouble();
                    if(endPatternPivotDirection>0){     //Looking for a downtrend subscratch;
                        boolean c1=valueofHigh==highLowPrices.get(n).getHigh();
                        boolean c2=subLow==highLowPrices.get(n+t-1).getLow();
                       if(highLowPrices.get(n).getClose()>highLowPrices.get(n).getOpen()){ //The starting bar is an uptrend bar;
                           if(c1&&c2){
                               subScratch.setLength(t);
                               subScratch.setStatus(-1);
                               subScratch.setLow(subLow);
                           }
                       }else {                          //The starting bar is a downtrend bar;
                           if(valueofLow==highLowPrices.get(n).getLow()){
                               if(subHigh==highLowPrices.get(n+1).getHigh() && subLow==highLowPrices.get(n+t-1).getLow()){
                                   subScratch.setLength(t-1);
                                   subScratch.setStatus(-1);
                                   subScratch.setLow(subLow);
                               }
                           }else {
                               boolean c3=valueofLow==highLowPrices.get(n+t-1).getLow();
                               if(c1&&c3){
                                   subScratch.setLength(t);
                                   subScratch.setStatus(-1);
                                   subScratch.setLow(valueofLow);
                               }
                           }
                       }
                    }else {                            //Looking for an uptrend subscratch;
                       boolean c1=valueofLow==highLowPrices.get(n).getLow();
                       boolean c2=subHigh==highLowPrices.get(n+t-1).getHigh();
                       if(highLowPrices.get(n+1).getClose()>highLowPrices.get(n).getOpen()){ //The starting bar is an uptrend bar;
                           if(valueofHigh==highLowPrices.get(n).getHigh()){
                               if(subLow==highLowPrices.get(n+1).getLow() && subHigh==highLowPrices.get(n+t-1).getHigh()){
                                   subScratch.setLength(t-1);
                                   subScratch.setStatus(1);
                                   subScratch.setHigh(subHigh);
                               }
                           }else {
                             boolean c3=valueofHigh==highLowPrices.get(n+t-1).getHigh();
                             if(c1 && c3){
                                 subScratch.setLength(t);
                                 subScratch.setStatus(1);
                                 subScratch.setHigh(subHigh);
                             }
                           }
                       }else {                                                      //The starting bar is a downtrend bar;
                           if(c1&&c2){
                               subScratch.setLength(t);
                               subScratch.setStatus(1);
                               subScratch.setHigh(subHigh);
                           }
                       }
                    }
                }
                boolean b1=subScratch.getLength()>returnScratch.getLength();
                boolean b2=subScratch.getHigh()-subScratch.getLow()>returnScratch.getHigh()-returnScratch.getLow();
                if(b1){
                    returnScratch=new Scratch(subScratch);
                }else if(subScratch.getLength()==returnScratch.getLength() && b2){
                    returnScratch=new Scratch(subScratch);
                }
            }
        return returnScratch;
    }
    @Override
    public List<Scratch> findScratches(List<HighLowPrice> highLowPrices, int startindex,Scratch upscratch,Scratch dwscratch,int nofupscratch,int nofdwscratch) {

        List<Scratch> scratches = new ArrayList<>();

        for(int n=startindex;n<highLowPrices.size();n++){
            if (upscratch.getStatus()==1) { //Scenario 1: A formed up trend scratch exists;

                if(highLowPrices.get(n).getHigh()>=upscratch.getHigh()     // 1.1: uptrend creates a new high;
                        && highLowPrices.get(n).getLow()>=upscratch.getLow() ){

                    upscratch.setLength(highLowPrices.get(n).getId()-upscratch.getStartId()+1);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    nofupscratch=n;
                    dwscratch=new Scratch(highLowPrices.get(n));
                    nofdwscratch=n;

                }else if(highLowPrices.get(n).getHigh()>upscratch.getHigh()  //1.2: a bar breaks both high and low of this uptrend scrach;
                        && highLowPrices.get(n).getLow()<upscratch.getLow()){

                    if(highLowPrices.get(n).getOpen()>highLowPrices.get(n).getClose()){ //n is considered as a negative bar: which means the intraday highest point(top) appeared earlier than the intraday lowest point(bottom)
                        upscratch.setLength(highLowPrices.get(n).getId()-upscratch.getStartId()+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        Scratch scratch=new Scratch(upscratch);
                        scratches.add(scratch);

                        upscratch=new Scratch(highLowPrices.get(n));
                        nofupscratch=n;

                        dwscratch=new Scratch(highLowPrices.get(n));
                        nofdwscratch=n;

                    }else {  //n is considered as a positive bar: which means the intraday lowest point(bottom) appeared earlier than the intraday highest point(top)
                        dwscratch.setLength(highLowPrices.get(n).getId()-dwscratch.getStartId()+1);
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        if(n-dwscratch.getStartId()>=pivotLength-2){ // A down trend scratch formed
                            dwscratch.setStatus(-1);
                        }

                        Scratch dscratch=new Scratch(dwscratch); // amended because new constructor is added;
                        scratches.add(dscratch);

                        dwscratch=new Scratch(highLowPrices.get(n));
                        nofdwscratch=n;
                        Scratch scratch=new Scratch(upscratch);// amended because new constructor is added;
                        scratches.add(scratch);

                        upscratch=new Scratch(highLowPrices.get(n));
                        nofupscratch=n;
                    }

                }else if(highLowPrices.get(n).getHigh()<=dwscratch.getHigh()  // 1.3: a downtrend scratch formed in a uptrend;
                        && highLowPrices.get(n).getLow()<=dwscratch.getLow()
                        && n-dwscratch.getStartId()>=pivotLength-2){

                    dwscratch.setLength(highLowPrices.get(n).getId()-dwscratch.getStartId()+1);
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    dwscratch.setStatus(-1);
                    nofdwscratch=n;
                    if(upscratch.getStartId()<dwscratch.getStartId()){
                        upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);

                        Scratch scratch=new Scratch(upscratch);
                        scratches.add(scratch);

                    }
                    upscratch=new Scratch(highLowPrices.get(n));
                    nofupscratch=n;
                }else {                                                            // 1.4
                    if(highLowPrices.get(n).getLow()<upscratch.getLow()){          // 1.4.1: uptrend scratch ended by a broken of low;
                        upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);
                        Scratch scratch=new Scratch(upscratch);
                        scratches.add(scratch);

                        upscratch=new Scratch(highLowPrices.get(n));
                        nofupscratch=n;
                        dwscratch.setLength(highLowPrices.get(n).getId()-dwscratch.getStartId()+1);
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        nofdwscratch=n;
                    }else if(highLowPrices.get(n).getLow()<dwscratch.getLow()){    //1.4.2: dwscratch extended by a new low;
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        dwscratch.setLength(highLowPrices.get(n).getId()-dwscratch.getStartId()+1);
                        nofdwscratch=n;

                    }else if(n-nofupscratch>=pivotLength && dwscratch.getStatus()==0
                            && (highLowPrices.get(n).getLow()==
                            ((float)highLowPrices.subList(nofupscratch+1,n+1).stream().mapToDouble(HighLowPrice::getLow).min().getAsDouble())) ){ // find hidden downtrend scratch;

                            upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);
                            Scratch scratch=new Scratch(upscratch);
                            scratches.add(scratch);
                            upscratch=new Scratch(highLowPrices.get(n));
                            nofupscratch=n;
                            dwscratch.setLow(highLowPrices.get(n).getLow());
                            dwscratch.setLength(highLowPrices.get(n).getId()-dwscratch.getStartId()+1);
                            dwscratch.setStatus(-1);
                            nofdwscratch=n;

                    }else if(dwscratch.getLength()==1 && n-nofdwscratch==1
                            && highLowPrices.get(n-1).getOpen()<highLowPrices.get(n-1).getClose()
                            && highLowPrices.get(n).getHigh()<=highLowPrices.get(n-1).getHigh()){
                            dwscratch.setLength(2);
                            dwscratch.setLow(highLowPrices.get(n).getLow());
                            nofdwscratch=n;
                    }
                    else {
                        if(n-nofupscratch>=pivotLength){
                            List<HighLowPrice> partialHighLowPrice=highLowPrices.subList(nofupscratch,n+1);

                            Scratch tempscratch=checkHiddenScratch(partialHighLowPrice);
                            if(tempscratch.getStatus()==-1){
                                upscratch.setLength(highLowPrices.get(nofupscratch).getId()-upscratch.getStartId()+1);
                                Scratch scratch=new Scratch(upscratch);
                                scratches.add(scratch);
                                dwscratch.setLength(highLowPrices.get(nofdwscratch).getId()-dwscratch.getStartId()+1);
                                Scratch dscratch=new Scratch(dwscratch);
                                scratches.add(dscratch);
                                if(tempscratch.getStartId()-1-nofdwscratch>0){
                                    Scratch scratch2=new Scratch(highLowPrices.get(nofdwscratch));
                                    scratch2.setLength(tempscratch.getStartId()-nofdwscratch);
                                    scratch2.setHigh(tempscratch.getHigh());
                                    scratches.add(scratch2);
                                }
                                dwscratch=new Scratch(tempscratch);
                                nofdwscratch=n;
                                upscratch=new Scratch(highLowPrices.get(n));
                                nofupscratch=n;
                            }else if(tempscratch.getStatus()==1){
                                upscratch.setLength(highLowPrices.get(nofupscratch).getId()-upscratch.getStartId()+1);
                                Scratch scratch=new Scratch(upscratch);
                                scratches.add(scratch);
                                boolean c=highLowPrices.get(nofdwscratch).getLow()<tempscratch.getLow(); //Insert lines to fix the bug on 1987/11/1;
                                if(c){
                                    dwscratch.setLength(highLowPrices.get(nofdwscratch).getId()-dwscratch.getStartId()+1);
                                    dwscratch.setLow(highLowPrices.get(nofdwscratch).getLow());
                                    dwscratch.setStatus(-1);
                                    tempscratch.setStartdate(highLowPrices.get(nofdwscratch).getDate());
                                    tempscratch.setStartId(highLowPrices.get(nofdwscratch).getId());
                                    tempscratch.setLength(tempscratch.getLength()+tempscratch.getStartId()-nofdwscratch);
                                    tempscratch.setLow(highLowPrices.get(nofdwscratch).getLow());         //Insert lines to fix the bug on 1987/11/1;
                                }else {
                                    dwscratch.setLength(tempscratch.getStartId()-dwscratch.getStartId()+1);
                                    dwscratch.setLow(tempscratch.getLow());
                                }

                                Scratch dscratch=new Scratch(dwscratch);
                                scratches.add(dscratch);
                                upscratch=new Scratch(tempscratch);
                                nofupscratch=n;
                                dwscratch=new Scratch(highLowPrices.get(n));
                                nofdwscratch=n;
                            }
                        }
                    }
                }
            }else if (dwscratch.getStatus()==-1){ //Scenario 2: A formed down trend scratch exists;

                if(highLowPrices.get(n).getLow()<=dwscratch.getLow()&&highLowPrices.get(n).getHigh()<=dwscratch.getHigh()){
                    dwscratch.setLength(highLowPrices.get(n).getId()-dwscratch.getStartId()+1);
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    nofdwscratch=n;
                    upscratch=new Scratch(highLowPrices.get(n));
                    nofupscratch=n;
                }else if(highLowPrices.get(n).getLow()<dwscratch.getLow()&&highLowPrices.get(n).getHigh()>dwscratch.getHigh()){

                    if(highLowPrices.get(n).getOpen()>highLowPrices.get(n).getClose()){
                        upscratch.setLength(highLowPrices.get(n).getId()-upscratch.getStartId()+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        if(n-upscratch.getStartId()>=pivotLength-2){ // A up trend scratch formed
                            upscratch.setStatus(1);
                        }

                        Scratch scratch=new Scratch(upscratch);
                        scratches.add(scratch);

                        upscratch=new Scratch(highLowPrices.get(n));
                        nofupscratch=n;
                        Scratch dscratch=new Scratch(dwscratch);
                        scratches.add(dscratch);

                        dwscratch=new Scratch(highLowPrices.get(n));
                        nofdwscratch=n;
                    }else {
                        dwscratch.setLength(highLowPrices.get(n).getId()-dwscratch.getStartId()+1);
                        dwscratch.setLow(highLowPrices.get(n).getLow());

                        Scratch dscratch=new Scratch(dwscratch);
                        scratches.add(dscratch);

                        dwscratch=new Scratch(highLowPrices.get(n));
                        nofdwscratch=n;

                        upscratch=new Scratch(highLowPrices.get(n));
                        nofupscratch=n;
                    }

                }else if(highLowPrices.get(n).getHigh()>=upscratch.getHigh()
                        && highLowPrices.get(n).getLow()>=upscratch.getLow()
                        && n-upscratch.getStartId()>=pivotLength-2){

                    upscratch.setLength(highLowPrices.get(n).getId()-upscratch.getStartId()+1);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    upscratch.setStatus(1);
                    nofupscratch=n;
                    if(dwscratch.getStartId()<upscratch.getStartId()){
                        dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);
                        Scratch dscratch=new Scratch(dwscratch);
                        scratches.add(dscratch);
                    }
                    dwscratch=new Scratch(highLowPrices.get(n));
                    nofdwscratch=n;
                }else if(upscratch.getLength()==1 && n-nofupscratch==1
                        && highLowPrices.get(n-1).getOpen()>highLowPrices.get(n-1).getClose()
                        && highLowPrices.get(n).getLow()>=highLowPrices.get(n-1).getLow()){

                    upscratch.setLength(2);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    nofupscratch=n;

                }else if(n-nofdwscratch>=pivotLength && upscratch.getStatus()==0
                        &&(highLowPrices.get(n).getHigh()==
                        ((float)highLowPrices.subList(nofdwscratch+1,n+1).stream().mapToDouble(HighLowPrice::getHigh).max().getAsDouble())) ){ // find hidden uptrend scratch;

                        dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);
                        Scratch dscratch=new Scratch(dwscratch);
                        scratches.add(dscratch);
                        dwscratch=new Scratch(highLowPrices.get(n));
                        nofdwscratch=n;
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLength(highLowPrices.get(n).getId()-upscratch.getStartId()+1);
                        upscratch.setStatus(1);
                        nofupscratch=n;

                }else {

                    if(highLowPrices.get(n).getHigh()>dwscratch.getHigh()){

                        dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);
                        Scratch dscratch=new Scratch(dwscratch);
                        scratches.add(dscratch);

                        dwscratch=new Scratch(highLowPrices.get(n));
                        nofdwscratch=n;
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLength(highLowPrices.get(n).getId()-upscratch.getStartId()+1);
                        nofupscratch=n;
                    }else if(highLowPrices.get(n).getHigh()>upscratch.getHigh()){

                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLength(highLowPrices.get(n).getId()-upscratch.getStartId()+1);
                        nofupscratch=n;
                    }else {

                        if(n-nofdwscratch>=pivotLength){
                            List<HighLowPrice> partialHighLowPrice=highLowPrices.subList(nofdwscratch,n+1);

                            Scratch tempscratch=checkHiddenScratch(partialHighLowPrice);
                            if(tempscratch.getStatus()==1){
                                dwscratch.setLength(highLowPrices.get(nofdwscratch).getId()-dwscratch.getStartId()+1);
                                Scratch dscratch=new Scratch(dwscratch);
                                scratches.add(dscratch);
                                upscratch.setLength(highLowPrices.get(nofupscratch).getId()-upscratch.getStartId()+1);
                                Scratch scratch=new Scratch(upscratch);
                                scratches.add(scratch);

                                if(tempscratch.getStartId()-1-nofupscratch>0){
                                    Scratch scratch2=new Scratch(highLowPrices.get(nofupscratch));
                                    scratch2.setLength(tempscratch.getStartId()-nofupscratch);
                                    scratch2.setLow(tempscratch.getLow());
                                    scratches.add(scratch2);
                                }
                                upscratch=new Scratch(tempscratch);
                                nofupscratch=n;
                                dwscratch=new Scratch(highLowPrices.get(n));
                                nofdwscratch=n;
                            }else if(tempscratch.getStatus()==-1){
                                dwscratch.setLength(highLowPrices.get(nofdwscratch).getId()-dwscratch.getStartId()+1);
                                Scratch dscratch=new Scratch(dwscratch);
                                scratches.add(dscratch);
                                boolean c=highLowPrices.get(nofupscratch).getHigh()>tempscratch.getHigh();
                                if(c){
                                    upscratch.setLength(highLowPrices.get(nofupscratch).getId()-upscratch.getStartId()+1);
                                    upscratch.setHigh(highLowPrices.get(nofupscratch).getHigh());
                                    upscratch.setStatus(1);
                                    tempscratch.setStartdate(highLowPrices.get(nofupscratch).getDate());
                                    tempscratch.setStartId(highLowPrices.get(nofupscratch).getId());
                                    tempscratch.setLength(tempscratch.getLength()+tempscratch.getStartId()-nofupscratch);
                                    tempscratch.setHigh(highLowPrices.get(nofupscratch).getHigh());
                                }else {
                                    upscratch.setLength(tempscratch.getStartId()-upscratch.getStartId()+1);
                                    upscratch.setHigh(tempscratch.getHigh());
                                }

                                Scratch scratch=new Scratch(upscratch);
                                scratches.add(scratch);
                                upscratch=new Scratch(highLowPrices.get(n));
                                nofupscratch=n;
                                dwscratch=new Scratch(tempscratch);
                                nofdwscratch=n;
                            }
                        }
                    }
                }
            }else {     // Scenario 3:  no direction

                if(highLowPrices.get(n).getHigh()>=upscratch.getHigh() && highLowPrices.get(n).getHigh()>=dwscratch.getHigh()
                        && highLowPrices.get(n).getLow()>=upscratch.getLow()){
                    upscratch.setLength(highLowPrices.get(n).getId()-upscratch.getStartId()+1);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    nofupscratch=n;
                    if(dwscratch.getStartId()<upscratch.getStartId()){
                        dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);
                        Scratch dscratch=new Scratch(dwscratch);//amended because new constructor is added;
                        scratches.add(dscratch);
                    }else if(upscratch.getStartId()==dwscratch.getStartId()&&highLowPrices.get(n).getLow()==dwscratch.getLow()){
                        if(highLowPrices.get(upscratch.getStartId()-1).getOpen()>highLowPrices.get(upscratch.getStartId()-1).getClose()){
                            dwscratch.setLength(n-dwscratch.getStartId()+2);
                            Scratch dscratch=new Scratch(dwscratch);//amended because new constructor is added;
                            scratches.add(dscratch);
                        }
                    }else if(upscratch.getStartId()==dwscratch.getStartId()&&highLowPrices.get(n).getLow()>dwscratch.getLow()){
                        if(highLowPrices.get(upscratch.getStartId()-1).getOpen()>highLowPrices.get(upscratch.getStartId()-1).getClose()){
                            dwscratch.setLength(nofdwscratch-dwscratch.getStartId()+2);
                            Scratch dscratch=new Scratch(dwscratch);//amended because new constructor is added;
                            scratches.add(dscratch);
                        }
                    }
                    dwscratch=new Scratch(highLowPrices.get(n));
                    nofdwscratch=n;
                    if(n-upscratch.getStartId()>=pivotLength-2){ // An up trend scratch formed
                        upscratch.setStatus(1);
                    }
                }else if(highLowPrices.get(n).getLow()<=dwscratch.getLow() && highLowPrices.get(n).getLow()<=upscratch.getLow()
                        && highLowPrices.get(n).getHigh()<=dwscratch.getHigh()){
                    dwscratch.setLength(highLowPrices.get(n).getId()-dwscratch.getStartId()+1);
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    nofdwscratch=n;
                    if(upscratch.getStartId()<dwscratch.getStartId()){
                        upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);
                        Scratch scratch=new Scratch(upscratch);//amended because new constructor is added;
                        scratches.add(scratch);
                    }else if(upscratch.getStartId()==dwscratch.getStartId()&&highLowPrices.get(n).getHigh()==upscratch.getHigh()){
                        if(highLowPrices.get(upscratch.getStartId()-1).getOpen()<highLowPrices.get(upscratch.getStartId()-1).getClose()){
                            upscratch.setLength(n-upscratch.getStartId()+2);
                            Scratch scratch=new Scratch(upscratch);//amended because new constructor is added;
                            scratches.add(scratch);
                        }
                    }else if(upscratch.getStartId()==dwscratch.getStartId()&&highLowPrices.get(n).getHigh()<upscratch.getHigh()){
                        if(highLowPrices.get(upscratch.getStartId()-1).getOpen()<highLowPrices.get(upscratch.getStartId()-1).getClose()){
                            upscratch.setLength(nofupscratch-upscratch.getStartId()+2);
                            Scratch scratch=new Scratch(upscratch);//amended because new constructor is added;
                            scratches.add(scratch);
                        }
                    }
                    upscratch=new Scratch(highLowPrices.get(n));
                    nofupscratch=n;
                    if(n-dwscratch.getStartId()>=pivotLength-2){ // A down trend scratch formed
                        dwscratch.setStatus(-1);
                    }

                }else if(highLowPrices.get(n).getHigh()>=upscratch.getHigh() && n-upscratch.getStartId()>=pivotLength-2){
                    upscratch.setLength(highLowPrices.get(n).getId()-upscratch.getStartId()+1);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    upscratch.setStatus(1);
                    nofupscratch=n;
                    if(dwscratch.getStartId()<upscratch.getStartId()){
                        dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);

                        Scratch dscratch=new Scratch(dwscratch);
                        scratches.add(dscratch);

                    }

                    dwscratch=new Scratch(highLowPrices.get(n));
                    nofdwscratch=n;
                }else if(highLowPrices.get(n).getLow()<dwscratch.getLow() && n-dwscratch.getStartId()>=pivotLength-2){
                    dwscratch.setLength(highLowPrices.get(n).getId()-dwscratch.getStartId()+1);
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    dwscratch.setStatus(-1);
                    nofdwscratch=n;
                    if(upscratch.getStartId()<dwscratch.getStartId()){
                        upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);

                        Scratch scratch=new Scratch(upscratch);
                        scratches.add(scratch);

                    }
                    upscratch=new Scratch(highLowPrices.get(n));
                    nofupscratch=n;
                }else if(highLowPrices.get(n).getHigh()>upscratch.getHigh() && highLowPrices.get(n).getHigh()>dwscratch.getHigh()
                        && highLowPrices.get(n).getLow()<upscratch.getLow() && highLowPrices.get(n).getLow()<dwscratch.getLow()){
                    if(highLowPrices.get(n).getOpen()>highLowPrices.get(n).getClose()){
                        upscratch.setLength(highLowPrices.get(n).getId()-upscratch.getStartId()+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        if(n-upscratch.getStartId()>=pivotLength-2){ // An up trend scratch formed
                            upscratch.setStatus(1);
                        }
                        Scratch scratch=new Scratch(upscratch);
                        scratches.add(scratch);

                        if(dwscratch.getStartId()<upscratch.getStartId()){
                            dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);
                            Scratch dscratch=new Scratch(dwscratch);
                            scratches.add(dscratch);
                        }
                        upscratch=new Scratch(highLowPrices.get(n));
                        nofupscratch=n;
                        dwscratch=new Scratch(highLowPrices.get(n));
                        nofdwscratch=n;

                    }else {
                        dwscratch.setLength(highLowPrices.get(n).getId()-dwscratch.getStartId()+1);
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        if(n-dwscratch.getStartId()>=pivotLength-2){ // A down trend scratch formed
                            dwscratch.setStatus(-1);
                        }

                        Scratch dscratch=new Scratch(dwscratch);
                        scratches.add(dscratch);


                        if(upscratch.getStartId()<dwscratch.getStartId()){
                            upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);

                            Scratch scratch=new Scratch(upscratch);
                            scratches.add(scratch);

                        }
                        upscratch=new Scratch(highLowPrices.get(n));
                        nofupscratch=n;
                        dwscratch=new Scratch(highLowPrices.get(n));
                        nofdwscratch=n;
                    }

                }else if(highLowPrices.get(n).getHigh()>upscratch.getHigh()){
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    upscratch.setLength(highLowPrices.get(n).getId()-upscratch.getStartId()+1);
                    nofupscratch=n;
                }else if(highLowPrices.get(n).getLow()<dwscratch.getLow()){
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    dwscratch.setLength(highLowPrices.get(n).getId()-dwscratch.getStartId()+1);
                    nofdwscratch=n;
                }else {
                    if(upscratch.getStartId()<dwscratch.getStartId()){
                        if(n-nofupscratch>=pivotLength && dwscratch.getStatus()==0
                                && (highLowPrices.get(n).getLow()==
                                ((float)highLowPrices.subList(nofupscratch+1,n+1).stream().mapToDouble(HighLowPrice::getLow).min().getAsDouble()))){// find hidden downtrend scratch;

                            upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);
                            Scratch scratch=new Scratch(upscratch);
                            scratches.add(scratch);
                            upscratch=new Scratch(highLowPrices.get(n));
                            nofupscratch=n;
                            dwscratch.setLow(highLowPrices.get(n).getLow());
                            dwscratch.setLength(highLowPrices.get(n).getId()-dwscratch.getStartId()+1);
                            dwscratch.setStatus(-1);
                            nofdwscratch=n;

                        }else if(n-nofupscratch>=pivotLength){
                            List<HighLowPrice> partialHighLowPrice=highLowPrices.subList(nofupscratch,n+1);
                            Scratch tempscratch=checkHiddenScratch(partialHighLowPrice);
                            if(tempscratch.getStatus()==-1){
                                upscratch.setLength(highLowPrices.get(nofupscratch).getId()-upscratch.getStartId()+1);
                                Scratch scratch=new Scratch(upscratch);
                                scratches.add(scratch);
                                dwscratch.setLength(highLowPrices.get(nofdwscratch).getId()-dwscratch.getStartId()+1);
                                Scratch dscratch=new Scratch(dwscratch);
                                scratches.add(dscratch);
                                if(tempscratch.getStartId()-1-nofdwscratch>0){
                                    Scratch scratch2=new Scratch(highLowPrices.get(nofdwscratch));
                                    scratch2.setLength(tempscratch.getStartId()-nofdwscratch);
                                    scratch2.setHigh(tempscratch.getHigh());
                                    scratches.add(scratch2);
                                }
                                dwscratch=new Scratch(tempscratch);
                                nofdwscratch=n;
                                upscratch=new Scratch(highLowPrices.get(n));
                                nofupscratch=n;
                            }else if(tempscratch.getStatus()==1){
                                upscratch.setLength(highLowPrices.get(nofupscratch).getId()-upscratch.getStartId()+1);
                                Scratch scratch=new Scratch(upscratch);
                                scratches.add(scratch);
                                boolean c=highLowPrices.get(nofdwscratch).getLow()<tempscratch.getLow(); //Insert lines to fix the bug on 1987/11/1;
                                if(c){
                                    dwscratch.setLength(highLowPrices.get(nofdwscratch).getId()-dwscratch.getStartId()+1);
                                    dwscratch.setLow(highLowPrices.get(nofdwscratch).getLow());
                                    dwscratch.setStatus(-1);
                                    tempscratch.setStartdate(highLowPrices.get(nofdwscratch).getDate());
                                    tempscratch.setStartId(highLowPrices.get(nofdwscratch).getId());
                                    tempscratch.setLength(tempscratch.getLength()+tempscratch.getStartId()-nofdwscratch);
                                    tempscratch.setLow(highLowPrices.get(nofdwscratch).getLow());         //Insert lines to fix the bug on 1987/11/1;
                                }else {
                                    dwscratch.setLength(tempscratch.getStartId()-dwscratch.getStartId()+1);
                                    dwscratch.setLow(tempscratch.getLow());
                                }

                                Scratch dscratch=new Scratch(dwscratch);
                                scratches.add(dscratch);
                                upscratch=new Scratch(tempscratch);
                                nofupscratch=n;
                                dwscratch=new Scratch(highLowPrices.get(n));
                                nofdwscratch=n;
                            }

                        }
                    }else {
                        if(n-nofdwscratch>=pivotLength && upscratch.getStatus()==0
                                &&(highLowPrices.get(n).getHigh()==
                                ((float)highLowPrices.subList(nofdwscratch+1,n+1).stream().mapToDouble(HighLowPrice::getHigh).max().getAsDouble()))){// find hidden uptrend scratch;

                            dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);
                            Scratch dscratch=new Scratch(dwscratch);
                            scratches.add(dscratch);
                            dwscratch=new Scratch(highLowPrices.get(n));
                            nofdwscratch=n;
                            upscratch.setHigh(highLowPrices.get(n).getHigh());
                            upscratch.setLength(highLowPrices.get(n).getId()-upscratch.getStartId()+1);
                            upscratch.setStatus(1);
                            nofupscratch=n;

                        }else if(n-nofdwscratch>=pivotLength){
                            List<HighLowPrice> partialHighLowPrice=highLowPrices.subList(nofdwscratch,n+1);

                            Scratch tempscratch=checkHiddenScratch(partialHighLowPrice);
                            if(tempscratch.getStatus()==1){
                                dwscratch.setLength(highLowPrices.get(nofdwscratch).getId()-dwscratch.getStartId()+1);
                                Scratch dscratch=new Scratch(dwscratch);
                                scratches.add(dscratch);
                                upscratch.setLength(highLowPrices.get(nofupscratch).getId()-upscratch.getStartId()+1);
                                Scratch scratch=new Scratch(upscratch);
                                scratches.add(scratch);

                                if(tempscratch.getStartId()-1-nofupscratch>0){
                                    Scratch scratch2=new Scratch(highLowPrices.get(nofupscratch));
                                    scratch2.setLength(tempscratch.getStartId()-nofupscratch);
                                    scratch2.setLow(tempscratch.getLow());
                                    scratches.add(scratch2);
                                }
                                upscratch=new Scratch(tempscratch);
                                nofupscratch=n;
                                dwscratch=new Scratch(highLowPrices.get(n));
                                nofdwscratch=n;
                            }else if(tempscratch.getStatus()==-1){
                                dwscratch.setLength(highLowPrices.get(nofdwscratch).getId()-dwscratch.getStartId()+1);
                                Scratch dscratch=new Scratch(dwscratch);
                                scratches.add(dscratch);
                                boolean c=highLowPrices.get(nofupscratch).getHigh()>tempscratch.getHigh();
                                if(c){
                                    upscratch.setLength(highLowPrices.get(nofupscratch).getId()-upscratch.getStartId()+1);
                                    upscratch.setHigh(highLowPrices.get(nofupscratch).getHigh());
                                    upscratch.setStatus(1);
                                    tempscratch.setStartdate(highLowPrices.get(nofupscratch).getDate());
                                    tempscratch.setStartId(highLowPrices.get(nofupscratch).getId());
                                    tempscratch.setLength(tempscratch.getLength()+tempscratch.getStartId()-nofupscratch);
                                    tempscratch.setHigh(highLowPrices.get(nofupscratch).getHigh());
                                }else {
                                    upscratch.setLength(tempscratch.getStartId()-upscratch.getStartId()+1);
                                    upscratch.setHigh(tempscratch.getHigh());
                                }

                                Scratch scratch=new Scratch(upscratch);
                                scratches.add(scratch);
                                upscratch=new Scratch(highLowPrices.get(n));
                                nofupscratch=n;
                                dwscratch=new Scratch(tempscratch);
                                nofdwscratch=n;
                            }

                        }
                    }
                }
            }
        }
        scratches.add(upscratch);
        scratches.add(dwscratch);
        scratches.sort(Comparator.comparingInt(Scratch::getStartId));
        int endofList=scratches.size()-1;
        for(int n=0;n<endofList;n++){          // Asign direction to all scratches;
            if(scratches.get(n).getStatus()==1){
                scratches.get(n).setStatus(2);
            }else if(scratches.get(n).getStatus()==-1){
                scratches.get(n).setStatus(-2);
            }else if(scratches.get(n).getStatus()==0){
                if(scratches.get(n).getHigh()==scratches.get(n+1).getHigh()){
                    scratches.get(n).setStatus(1);
                }else {
                    scratches.get(n).setStatus(-1);
                }
            }
        }
        if(scratches.get(endofList).getHigh()==scratches.get(endofList-1).getHigh()){ //Asign direction to scratches whose status is 0;
            scratches.get(endofList).setStatus(-1);
        }else {
            scratches.get(endofList).setStatus(1);
        }
        for(int n=1;n<scratches.size()-1;n++){
            boolean crite1=scratches.get(n).getHigh()==scratches.get(n+1).getHigh() && scratches.get(n).getLow()==scratches.get(n-1).getLow();
            boolean crite2=scratches.get(n).getHigh()==scratches.get(n-1).getHigh() && scratches.get(n).getLow()==scratches.get(n+1).getLow();
            if(!crite1 && !crite2) {
                System.out.println("Check Data with scratch id ="+scratches.get(n).toString());
            }
        }
        return scratches;
    }
    @Override
    public List<Pivot> findAllPivotsByScratch(List<Scratch> scratchList) {
        List<Pivot> allPivotList=new ArrayList<>();
        int sizeoftable=300;
        int loopcontroll=500;
        while (sizeoftable!=loopcontroll){ // Start Line of Complex method;
            loopcontroll=sizeoftable;
            List<Pivot> pivotList=new ArrayList<>();     // Start Line of original simple method;
            List<Scratch> scratchesforLoop=new ArrayList<>();
            int n=0;
            while (n<scratchList.size()-2){
                boolean crite1=scratchList.get(n).getHigh()>=scratchList.get(n+2).getHigh() &&
                        scratchList.get(n+2).getLow()<=scratchList.get(n).getLow() &&
                        scratchList.get(n).getStatus()<0;
                boolean crite2=scratchList.get(n+2).getHigh()>=scratchList.get(n).getHigh() &&
                        scratchList.get(n).getLow()<=scratchList.get(n+2).getLow() &&
                        scratchList.get(n).getStatus()>0;
                if(crite1){          // Scenarial #1: scratch n is a start scratch of downtrend pivot
                    int endofpivot=n+2;
                    float lowerLow=scratchList.get(n+2).getLow();

                    Pivot pivot= new Pivot(scratchList.get(n));
                    pivot.setLength(scratchList.get(endofpivot).getStartId()-scratchList.get(n).getStartId()+scratchList.get(endofpivot).getLength());
                    pivot.setLow(lowerLow);
                    int maxlevel=scratchList.subList(n,endofpivot).stream().mapToInt(Scratch::getStatus).max().getAsInt();
                    pivot.setPivotType(-(maxlevel+1));
                    for (int i=n+1;i<=endofpivot;i++){
                        pivot.getScratches().add(scratchList.get(i));
                    }
                    pivotList.add(pivot);
                    Scratch scratch=new Scratch(pivot);
                    scratchesforLoop.add(scratch);
                    n=endofpivot+1;
                }else if(crite2){    // Scenarial #2: scratch n is a start scratch of uptrend pivot
                    int endofpivot=n+2;
                    float higherHigh=scratchList.get(n+2).getHigh();

                    Pivot pivot= new Pivot(scratchList.get(n));
                    pivot.setLength(scratchList.get(endofpivot).getStartId()-scratchList.get(n).getStartId()+scratchList.get(endofpivot).getLength());
                    pivot.setHigh(higherHigh);
                    int maxlevel=scratchList.subList(n,endofpivot).stream().mapToInt(Scratch::getStatus).min().getAsInt();
                    pivot.setPivotType(-(maxlevel-1));
                    for (int i=n+1;i<=endofpivot;i++){
                        pivot.getScratches().add(scratchList.get(i));
                    }
                    pivotList.add(pivot);
                    Scratch scratch=new Scratch(pivot);
                    scratchesforLoop.add(scratch);
                    n=endofpivot+1;
                }else {              // Scenarial #3: scratch n is not a start scratch of any pivot
                    Scratch scratch=new Scratch(scratchList.get(n));
                    scratchesforLoop.add(scratch);
                    n=n+1;
                }
            }
            for(int t=n;t<scratchList.size();t++){
                scratchesforLoop.add(scratchList.get(t));
            }
             // End Line of original simple method;
            allPivotList.addAll(pivotList);
            sizeoftable=scratchesforLoop.size();
            scratchList.clear();
            scratchList.addAll(scratchesforLoop);
        }                                  // End Line of Complex method;
        allPivotList.sort(Comparator.comparingInt(Pivot::getStartId));
        return allPivotList;
    }
    @Override
    public List<Pivot> obtainKeyPivots(List<Pivot> allPivotList) {
        List<Pivot> keyPivotList=new ArrayList<>();
        List<Scratch> allScratchList=new ArrayList<>();
        for(Pivot pivot:allPivotList){
            Scratch scratch=new Scratch(pivot);
            allScratchList.add(scratch);
        }
        List<Scratch> scratchList=new ArrayList<>();
        scratchList.addAll(allScratchList);
        List<Scratch> scratchesforLoop=new ArrayList<>();
        for(int n=0;n<scratchList.size();n++){
            Scratch maxscratch=new Scratch(scratchList.get(n));
            for(int i=n+1;i<scratchList.size();i++){
                if(scratchList.get(i).getStartId()>=maxscratch.getStartId()+maxscratch.getLength()-1){
                    break;
                }
                boolean c=scratchList.get(i).getLength()>maxscratch.getLength();
                if(c){
                    maxscratch=new Scratch(scratchList.get(i));
                }
                scratchList.remove(i);
                i=i-1;
            }
            scratchesforLoop.add(maxscratch);
        }
        while (!scratchesforLoop.isEmpty()){
            List<Scratch> scratchListforcontrol=new ArrayList<>();
            for(Scratch basicscratch:scratchesforLoop){
                List<Scratch> scratchListforHandle=new ArrayList<>();
                Pivot pivot=new Pivot(basicscratch);
                for(Scratch scratch:allScratchList){

                    boolean c1=basicscratch.getStatus()*scratch.getStatus()<0;
                    boolean c2=basicscratch.getStartId()+basicscratch.getLength()>scratch.getStartId()+scratch.getLength();
                    boolean c3=basicscratch.getStartId()<scratch.getStartId();
                    if(c1 && c2 && c3){
                        scratchListforHandle.add(scratch);
                    }
                }
                if(scratchListforHandle.size()>0){
                    List<Scratch> cleanedscratchList=removeRedundentScratch(scratchListforHandle);
                    scratchListforcontrol.addAll(cleanedscratchList);
                    pivot.getScratches().clear();
                    pivot.getScratches().addAll(cleanedscratchList);
                }
                keyPivotList.add(pivot);
            }
            scratchesforLoop.clear();
            if(!scratchListforcontrol.isEmpty()){
                scratchesforLoop.addAll(scratchListforcontrol);
            }
            //System.out.println("Size of Loop List "+ scratchesforLoop.size());
        }
        return keyPivotList;
    }
    @Override
    public List<Scratch> findEigenScratches(List<Pivot> pivotsForPatternSearch) {
        List<Scratch> eigenscratches=new ArrayList<>();
        float highPoint=pivotsForPatternSearch.get(0).getHigh();
        float lowPoint=pivotsForPatternSearch.get(0).getLow();
        int indexofeigenPivot=0;
        for(int n=0;n<pivotsForPatternSearch.size();n++){
            boolean b1=pivotsForPatternSearch.get(n).getHigh()>=highPoint;
            boolean b2=pivotsForPatternSearch.get(n).getLow()<=lowPoint;
            if(b1 && b2){
                indexofeigenPivot=n;
                highPoint=pivotsForPatternSearch.get(n).getHigh();
                lowPoint=pivotsForPatternSearch.get(n).getLow();
            }
        }
        System.out.println("Eigen Pivot is "+pivotsForPatternSearch.get(indexofeigenPivot).toString());
        eigenscratches.addAll(pivotsForPatternSearch.get(indexofeigenPivot).getScratches());
        if(eigenscratches.size()>1){
            eigenscratches.sort(Comparator.comparingInt(Scratch::getLength).reversed());
            int n=0;
            while (n<eigenscratches.size()){
                Scratch maxScratch=new Scratch(eigenscratches.get(n));
                for(int t=n+1;t<eigenscratches.size();t++){
                    boolean b=maxScratch.getStartId()>eigenscratches.get(t).getStartId();
                    boolean c=(float)eigenscratches.get(t).getLength()/maxScratch.getLength()<controlFactor;
                    if(b && c){
                       eigenscratches.remove(t);
                       t--;
                    }else if(b && !c){
                        boolean a=false;
                        if(maxScratch.getStatus()>0){
                            if(eigenscratches.get(t).getLow()>maxScratch.getHigh()){
                                a=true;
                            }
                        }else {
                            if(maxScratch.getLow()>eigenscratches.get(t).getHigh()){
                                a=true;
                            }
                        }
                        if(!a){
                            eigenscratches.remove(t);
                            t--;
                        }
                    }
                }
                n++;
            }
            eigenscratches.sort(Comparator.comparingInt(Scratch::getStartId));
        }
        return eigenscratches;
    }

    @Override
    public List<Scratch> mergeEigenScratches(List<Scratch> dailyEigenScratches, List<Scratch> intradayEigenScratches) {
        List<Scratch> mergedScratches=new ArrayList<>();
        SimpleDateFormat intradayTime=new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        SimpleDateFormat dailyTime=new SimpleDateFormat("yyyy/MM/dd");
        for(int n=0;n<dailyEigenScratches.size();n++){
            try {
                Date mergedate=dailyTime.parse(dailyTime.format(intradayTime.parse(intradayEigenScratches.get(0).getStartdate())));
                Date date=dailyTime.parse(dailyEigenScratches.get(n).getStartdate());
                if(date.getTime()==mergedate.getTime()){
                    mergedScratches.addAll(dailyEigenScratches.subList(0,n));
                    mergedScratches.addAll(intradayEigenScratches);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return mergedScratches;
    }

    @Override
    public List<Pivot> find2ndPattern(List<Pivot> pivotsForPatternSearch, List<Scratch> scratches) {
        List<Scratch> allCompoundScratches=new ArrayList<>();
        allCompoundScratches.addAll(scratches);
        List<Pivot> pivotsof2ndPattern=new ArrayList<>();
        List<Scratch> scratchesofNomatch=new ArrayList<>();
        int case0=0;
        int case1=0;
        int case2=0;
        int case3=0;
        int case4=0;
        int case5=0;
        for(Pivot pivot:pivotsForPatternSearch){
            // System.out.println("Current Pivot is -------------------- "+pivot.toString());
            for(Scratch scratch:pivot.getScratches()){

                int flag=0;
                int startSearch=0;
                int endofList=allCompoundScratches.size()-1;
                int endSearch=allCompoundScratches.get(endofList).getStartId()+allCompoundScratches.get(endofList).getLength()-1;
                if(pivot.getScratches().size()>1){
                    for(int e=1;e<pivot.getScratches().size();e++){
                        float compare=(float)pivot.getScratches().get(e).getLength()/scratch.getLength();
                        boolean b1=compare>1/controlFactor;
                        boolean b2=pivot.getScratches().get(e).getStartId()>scratch.getStartId();

                        if(b1&&b2&&pivot.getPivotType()>0){
                            flag=1;
                            if(e==pivot.getScratches().size()-1){
                                endSearch=pivot.getStartId()+pivot.getLength()-1;
                                break;
                            }
                            for(int d=e+1;d<=pivot.getScratches().size()-1;d++){
                                if(pivot.getScratches().get(d).getHigh()>pivot.getScratches().get(e).getHigh()){
                                    endSearch=pivot.getScratches().get(d).getStartId();
                                    break;
                                }
                            }
                        }else if(b1&&b2&&pivot.getPivotType()<0){
                            flag=1;
                            if(e==pivot.getScratches().size()-1){
                                endSearch=pivot.getStartId()+pivot.getLength()-1;
                                break;
                            }
                            for(int d=e+1;d<=pivot.getScratches().size()-1;d++){
                                if(pivot.getScratches().get(d).getLow()<pivot.getScratches().get(e).getLow()){
                                    endSearch=pivot.getScratches().get(d).getStartId();
                                    break;
                                }
                            }
                        }
                        if(endSearch<allCompoundScratches.get(endofList).getStartId()+allCompoundScratches.get(endofList).getLength()-1){
                            break;
                        }
                    }
                }

                for(int n=0;n<=allCompoundScratches.size()-1;n++){ // Start Line of 2nd Pattern Searching;
                    if(allCompoundScratches.get(n).getStartId()>endSearch){
                        break;
                    }
                    startSearch=scratch.getStartId()+scratch.getLength()-1;
                    boolean c1=allCompoundScratches.get(n).getStartId()>startSearch;
                    boolean c2=allCompoundScratches.get(n).getStatus()*scratch.getStatus()<0;
                    float a=(float)allCompoundScratches.get(n).getLength()/scratch.getLength();
                    boolean c3=a>controlFactor;
                    boolean c4=true;
                    if(pivot.getPivotType()>0){
                        if(allCompoundScratches.get(n).getHigh()>pivot.getHigh()){
                            c4=false;
                        }
                    }else {
                        if(allCompoundScratches.get(n).getLow()<pivot.getLow()){
                            c4=false;
                        }
                    }
                    boolean c5=allCompoundScratches.get(n).getStatus()>=100 || allCompoundScratches.get(n).getStatus()<=-100;
                    if(c1 && c2 && c3 && c4 &&!c5){
                        for(int i=n-1;allCompoundScratches.get(i).getStartId()>=startSearch;i--){
                            boolean c6=allCompoundScratches.get(n).getStartId()>allCompoundScratches.get(i).getStartId()+allCompoundScratches.get(i).getLength();
                            if(scratch.getStatus()>0){
                                if(c6 && allCompoundScratches.get(i).getLow()<scratch.getLow()
                                        && allCompoundScratches.get(i).getLow()<allCompoundScratches.get(n).getLow()){
                                    Pivot pivot1=new Pivot(scratch);
                                    pivot1.getScratches().add(allCompoundScratches.get(n));
                                    pivotsof2ndPattern.add(pivot1);
                                    allCompoundScratches.get(n).setStatus(allCompoundScratches.get(n).getStatus()*100);
                                    if(flag<0){
                                        flag=3;
                                    }else {
                                        flag=2;
                                    }
                                    for(int t=n+1;t<allCompoundScratches.size();t++){
                                        boolean a1=(float)allCompoundScratches.get(t).getLength()/scratch.getLength()<1/controlFactor;
                                        boolean a2=allCompoundScratches.get(i).getLow()<allCompoundScratches.get(t).getLow();
                                        boolean a3=allCompoundScratches.get(n).getStartId()==allCompoundScratches.get(t).getStartId();
                                        if(!a1||!a2||!a3){
                                            break;
                                        }else {
                                            Pivot pivot2=new Pivot(scratch);
                                            pivot2.getScratches().add(allCompoundScratches.get(t));
                                            pivotsof2ndPattern.add(pivot2);
                                            allCompoundScratches.get(t).setStatus(allCompoundScratches.get(t).getStatus()*100);
                                            if(flag<0){
                                                flag=3;
                                            }else {
                                                flag=2;
                                            }
                                        }
                                    }
                                    break;
                                }else if(c6 && allCompoundScratches.get(i).getLow()<scratch.getLow()
                                        && allCompoundScratches.get(i).getLow()>allCompoundScratches.get(n).getLow()){
                                    flag=-2;
                                }
                            }else {
                                if(c6 && allCompoundScratches.get(i).getHigh()>scratch.getHigh()
                                        && allCompoundScratches.get(i).getHigh()>allCompoundScratches.get(n).getHigh()){
                                    Pivot pivot1=new Pivot(scratch);
                                    pivot1.getScratches().add(allCompoundScratches.get(n));
                                    pivotsof2ndPattern.add(pivot1);
                                    allCompoundScratches.get(n).setStatus(allCompoundScratches.get(n).getStatus()*100);
                                    if(flag<0){
                                        flag=3;
                                       // pivot1.setPivotType(pivot1.getPivotType()*100);
                                    }else {
                                        flag=2;
                                    }
                                    for(int t=n+1;t<allCompoundScratches.size();t++){
                                        boolean a1=(float)allCompoundScratches.get(t).getLength()/scratch.getLength()<1/controlFactor;
                                        boolean a2=allCompoundScratches.get(i).getHigh()>allCompoundScratches.get(t).getHigh();
                                        boolean a3=allCompoundScratches.get(n).getStartId()==allCompoundScratches.get(t).getStartId();
                                        if(!a1||!a2||!a3){
                                            break;
                                        }else {
                                            Pivot pivot2=new Pivot(scratch);
                                            pivot2.getScratches().add(allCompoundScratches.get(t));
                                            pivotsof2ndPattern.add(pivot2);
                                            allCompoundScratches.get(t).setStatus(allCompoundScratches.get(t).getStatus()*100);
                                            if(flag<0){
                                                flag=3;
                                            }else {
                                                flag=2;
                                            }
                                        }
                                    }
                                    break;
                                }else if(c6 && allCompoundScratches.get(i).getHigh()>scratch.getHigh()
                                        && allCompoundScratches.get(i).getHigh()<allCompoundScratches.get(n).getHigh()){
                                    flag=-2;
                                }
                            }
                        }
                        if(flag>=2){
                            if(flag==2){
                                case2++;
                            }else {
                                case3++;
                            }
                            break;
                        }
                    }else if(c1 && c2 && c3){
                        flag=-1;
                    }
                }                                                        // End Line of 2nd Pattern Searching;
                if(flag<2){
                    if(flag==1){
                        //System.out.println("NonMatch Case--111111111 "+scratch.toString());
                        case1++;
                    }else if(flag==0) {
                        //System.out.println("NonMatch Case--000000000 "+scratch.toString());
                        case0++;
                    }else if(flag==-1){
                        case4++;
                        //System.out.println("NonMatch Case flag=-1 "+scratch.toString());
                    }else if(flag==-2){
                        case5++;
                        //System.out.println("NonMatch Case flag=-2 "+scratch.toString());
                    }
                    scratchesofNomatch.add(scratch);
                }
            }
        }
        System.out.println("Size of scratchesofNomatch is "+scratchesofNomatch.size());
        System.out.println("case0 = "+case0 +" case1 = "+case1+" case2 = "+case2+" case3 = "+case3+" case4 = "+case4+" case5 = "+case5);
        //System.out.println("Size of pivotsof2ndPattern is "+pivotsof2ndPattern.size());
        scratchesofNomatch.sort(Comparator.comparingInt(Scratch::getStartId));

        List<Pivot> pivotList=new ArrayList<>();
        List<Pivot> pivots=new ArrayList<>();
        pivotList.addAll(pivotsof2ndPattern);
        for(int n=0;n<pivotList.size()-1;n++){
            Pivot maxpivot=new Pivot(pivotList.get(n));
            for(int i=n+1;i<pivotList.size();i++){
                if(pivotList.get(n).getScratches().get(1).equals(pivotList.get(i).getScratches().get(1))){
                        /*System.out.println("pivot n is "+pivotList.get(n).toString());
                        System.out.println("pivot i is "+pivotList.get(i).toString());*/
                    if(pivotList.get(n).getLength()<pivotList.get(i).getLength()){
                        maxpivot=new Pivot(pivotList.get(i));
                    }
                    pivotList.remove(i);
                    i=i-1;
                }
            }
            pivots.add(maxpivot);
            //System.out.println("pivot max is "+maxpivot.toString());
        }
        //System.out.println("Size of pivots is "+pivotList.size());
        pivots.sort(Comparator.comparingInt(Pivot::getStartId));
        int nofbroken=0;
        for(Pivot pivot:pivots){
            if(pivot.getPivotType()>=100 || pivot.getPivotType()<=-100){
                // System.out.println("Noticable Pivot --- "+pivot.toString());
                nofbroken++;
            }
        }
        System.out.println("nofbroker = "+nofbroken);
        return pivots;
    }
    @Override
    public List<Pivot> find3rdPattern(List<Pivot> pivotsForPatternSearch, List<Scratch> scratches) {
        List<Scratch> allCompoundScratches=new ArrayList<>();
        allCompoundScratches.addAll(scratches);
        List<Pivot> pivotsof3rdPattern=new ArrayList<>();
        int nofmatch=0; // Number of found 3rd pattern pair;
        int nofreuse=0; // Number of found 3rd pattern that was paired by previous scratch;
        int nofoutlier=0; // Number of found 3rd pattern bigger than expected length range, potential lose when trading;
        int nofoutlier2=0;// Number of found 3rd pattern bigger than twice of expected length range, potential big lose when trading;
        int nofnonematch=0; // Number of scratches that have no 3rd pattern match;

        for(Pivot pivot:pivotsForPatternSearch){
            for(Scratch scratch:pivot.getScratches()){
                int nofStart=0;
                for(int n=0;n<allCompoundScratches.size();n++){
                    if(scratch.getStartId()==allCompoundScratches.get(n).getStartId()){
                        nofStart=n;
                        break;
                    }
                }
                for(int n=nofStart;n<allCompoundScratches.size();n++){ //Start line of pattern search;
                    boolean c1=allCompoundScratches.get(n).getStatus()*scratch.getStatus()>0;
                    boolean c2=(float)allCompoundScratches.get(n).getLength()/scratch.getLength()>controlFactor;
                    boolean c3=allCompoundScratches.get(n).getStartId()>scratch.getStartId();
                    boolean c4=false;
                    if(scratch.getStatus()>0){
                        if(scratch.getLow()>allCompoundScratches.get(n).getHigh()){
                            c4=true;
                        }
                    }else {
                        if(scratch.getHigh()<allCompoundScratches.get(n).getLow()){
                            c4=true;
                        }
                    }
                    boolean c5=allCompoundScratches.get(n).getStatus()>=100 || allCompoundScratches.get(n).getStatus()<=-100;
                    boolean c6=(double)allCompoundScratches.get(n).getLength()/scratch.getLength()>=1/controlFactor;
                    if(c1 && c3 && !c4 && c6){
                        nofnonematch++;
                        break;
                    }
                    if(c1 && c2 && c3 && c4 && c5){
                        nofreuse++;
                        break;
                    }
                    if(c1 && c2 && c3 && c4 && !c5){
                        nofmatch++;
                        Pivot pivot1=new Pivot(scratch);
                        Scratch scratch1=new Scratch(allCompoundScratches.get(n));
                        pivot1.getScratches().add(scratch1);
                        pivotsof3rdPattern.add(pivot1);
                        allCompoundScratches.get(n).setStatus(allCompoundScratches.get(n).getStatus()*100);
                        if(c6){
                            if((float)allCompoundScratches.get(n).getLength()/scratch.getLength()>2){
                                nofoutlier2++;
                            }else {nofoutlier++;}

                        }else{
                           for(int t=n+1;t<allCompoundScratches.size();t++){ // Find all the 3rd Pattern ending pivots with same startId;
                               boolean c7=(double)allCompoundScratches.get(t).getLength()/scratch.getLength()>=1/controlFactor;
                               if(allCompoundScratches.get(t).getStartId()!=allCompoundScratches.get(n).getStartId() || c7){
                                   break;
                               }else {
                                   boolean c8=allCompoundScratches.get(t).getStatus()>=100 || allCompoundScratches.get(t).getStatus()<=-100;
                                   boolean c9=false;
                                   if(scratch.getStatus()>0){
                                       if(scratch.getLow()>allCompoundScratches.get(t).getHigh()){
                                           c9=true;
                                       }
                                   }else {
                                       if(scratch.getHigh()<allCompoundScratches.get(t).getLow()){
                                           c9=true;
                                       }
                                   }
                                   if(!c8 && c9){
                                       nofmatch++;
                                       Pivot pivot2=new Pivot(scratch);
                                       Scratch scratch2=new Scratch(allCompoundScratches.get(t));
                                       pivot2.getScratches().add(scratch2);
                                       pivotsof3rdPattern.add(pivot2);
                                       allCompoundScratches.get(t).setStatus(allCompoundScratches.get(t).getStatus()*100);
                                   }
                               }
                           }                                                // Find all the 3rd Pattern ending pivots with same startId;
                        }
                        break;
                    }
                }    // End line of pattern search;
            }
        }
        System.out.println("nofmatch= "+nofmatch+" nofreuse= "+nofreuse+" nofoutlier= "+nofoutlier+" nofoutlier2= "+nofoutlier2+" nofnonematch "+nofnonematch);
        return pivotsof3rdPattern;
    }
    @Override
    public List<Pivot> find4thPattern(List<Pivot> pivotsForPatternSearch, List<Scratch> scratches) {
        List<Scratch> allCompoundScratches=new ArrayList<>();
        allCompoundScratches.addAll(scratches);
        List<Pivot> pivotsof4thPattern=new ArrayList<>();
        int nofmatch=0; // Number of found 4th pattern pair;
        int nofreuse=0; // Number of found 4th pattern that was paired by previous scratch;
        int nofoutlier=0; // Number of found 4th pattern bigger than expected length range, potential lose when trading;
        int nofoutlier2=0;// Number of found 4th pattern bigger than twice expected length range, potential serious lose when trading;
        int nofnonematch=0; // Number of scratches that have no 4th pattern match;

        int r25=0;
        int r70=0;
        int r140=0;
        int rmax=0;
        for(Pivot pivot:pivotsForPatternSearch){
            for(Scratch scratch:pivot.getScratches()){
                int nofStart=0;
                for(int n=0;n<allCompoundScratches.size();n++){
                    if(scratch.getStartId()==allCompoundScratches.get(n).getStartId()){
                        nofStart=n;
                        break;
                    }
                }
                for(int n=nofStart;n<allCompoundScratches.size();n++){ //Start line of pattern search;
                    boolean c1=allCompoundScratches.get(n).getStatus()*scratch.getStatus()>0;
                    boolean c2=(float)allCompoundScratches.get(n).getLength()/scratch.getLength()>controlFactor;
                    boolean c3=allCompoundScratches.get(n).getStartId()>scratch.getStartId();
                    boolean c4=false;
                    boolean c7=false;
                    if(pivot.getPivotType()>0){
                       if(allCompoundScratches.get(n).getHigh()>scratch.getHigh()||allCompoundScratches.get(n).getLow()>scratch.getLow()){
                           c4=true;
                       }
                       if(allCompoundScratches.get(n).getLow()<scratch.getHigh()){
                           c7=true;
                       }
                    }else {
                       if(allCompoundScratches.get(n).getLow()<scratch.getLow()||allCompoundScratches.get(n).getHigh()<scratch.getHigh()){
                           c4=true;
                       }
                       if(allCompoundScratches.get(n).getHigh()>scratch.getLow()){
                           c7=true;
                       }
                    }
                    boolean c5=allCompoundScratches.get(n).getStatus()>=100 || allCompoundScratches.get(n).getStatus()<=-100;
                    boolean c6=(float)allCompoundScratches.get(n).getLength()/scratch.getLength()>1/controlFactor;

                    if(c1 && c3 && !(c4&&c7) && c6){
                        nofnonematch++;
                        break;
                    }
                    if(c1 && c2 && c3 && c4 && c5 && c7){
                        nofreuse++;
                        break;
                    }
                    if(c1 && c2 && c3 && c4 && !c5 && c7){
                        nofmatch++;
                        Pivot pivot1=new Pivot(scratch);
                        Scratch scratch1=new Scratch(allCompoundScratches.get(n));
                        pivot1.getScratches().add(scratch1);
                        pivotsof4thPattern.add(pivot1);
                        allCompoundScratches.get(n).setStatus(allCompoundScratches.get(n).getStatus()*100);
                        if(c6){
                            if((float)allCompoundScratches.get(n).getLength()/scratch.getLength()>2){
                                nofoutlier2++;
                            }else {nofoutlier++;}
                        }else {
                            for(int t=n+1;t<allCompoundScratches.size()-1;t++){
                                boolean b0=allCompoundScratches.get(t).getStartId()>allCompoundScratches.get(n).getStartId()+allCompoundScratches.get(n).getLength()-1;
                                if(b0){
                                    break;
                                }
                                boolean b1=allCompoundScratches.get(t).getStatus()*scratch.getStatus()<0;
                                boolean b2=allCompoundScratches.get(t).getStartId()==allCompoundScratches.get(n).getStartId()+allCompoundScratches.get(n).getLength()-1;
                                if(b1 && b2){
                                    pivot1.getScratches().add(allCompoundScratches.get(t));
                                }
                            }
                            float ratio=(float)pivot1.getScratches().get(pivot1.getScratches().size()-1).getLength()/pivot1.getLength();
                            if(ratio<0.25){
                                r25++;
                            }else if(ratio>=0.25 && ratio<0.7){
                                r70++;
                            }else if(ratio>=0.7 && ratio<1.4){
                                r140++;
                            }else if(ratio>=1.4){
                                rmax++;
                            }
                        }
                        break;
                    }
                }    // End line of pattern search;
            }
        }
        System.out.println("nofmatch= "+nofmatch+" nofreuse= "+nofreuse+" nofoutlier= "+nofoutlier+" nofoutlier2= "+nofoutlier2+" nofnonematch "+nofnonematch);
        System.out.println("ratio<0.25: "+r25+" ratio>=0.25 && ratio<0.7: "+r70+" ratio>=0.7 && ratio<1.4: "+r140+" ratio>=1.4 "+rmax);

        return pivotsof4thPattern;
    }
    @Override
    public List<Pivot> findEarningScratch(List<Pivot> pivotsForSearch, List<HighLowPrice> allPrices,List<Scratch> allCompoundScratches) {
        List<Pivot> returnPivotList=new ArrayList<>();
        for(Pivot pivot:pivotsForSearch){
            double ratio=(double)pivot.getScratches().get(1).getLength()/pivot.getScratches().get(0).getLength();
            if(ratio>1/controlFactor){                  // The pattern-ending pivot is an outlier;
                int flag=0;
                int startId=pivot.getScratches().get(1).getStartId()+(int)(pivot.getScratches().get(0).getLength()*controlFactor);
                int endId=pivot.getScratches().get(1).getStartId()+(int)(pivot.getScratches().get(0).getLength()/controlFactor)-1;
               if(pivot.getScratches().get(1).getStatus()>0){   // If the pattern-ending pivot is an uptrend pivot;
                   int cutpoint=startId;
                   for(int n=startId;n<=endId;n++){
                       List<HighLowPrice> partialList=allPrices.subList(pivot.getScratches().get(1).getStartId()-1,n);
                       float valueofHigh=(float)partialList.stream().mapToDouble(HighLowPrice::getHigh).max().getAsDouble();
                       if(valueofHigh==allPrices.get(n-1).getHigh()){
                           flag=1;
                           cutpoint=n;
                           break;
                       }
                   }
                   if(flag>0){
                       Scratch scratch=findSubScratch(cutpoint-1,endId-1,allPrices,1);
                       Pivot pivot1=new Pivot(pivot);
                       pivot1.getScratches().get(1).setLength(scratch.getStartId()-pivot1.getScratches().get(1).getStartId()-1);
                       pivot1.getScratches().get(1).setHigh(scratch.getHigh());
                       pivot1.getScratches().add(scratch);
                       returnPivotList.add(pivot1);
                   }
               }else {                                              // If the pattern-ending pivots is a downtrend pivot;
                   int cutpoint=startId;
                   for(int n=startId;n<=endId;n++){
                       List<HighLowPrice> partialList=allPrices.subList(pivot.getScratches().get(1).getStartId()-1,n);
                       float valueofLow=(float)partialList.stream().mapToDouble(HighLowPrice::getLow).min().getAsDouble();
                       if(valueofLow==allPrices.get(n-1).getLow()){
                           flag=1;
                           cutpoint=n;
                           break;
                       }
                   }
                   if(flag>0){
                       Scratch scratch=findSubScratch(cutpoint-1,endId-1,allPrices,-1);
                       Pivot pivot1=new Pivot(pivot);
                       pivot1.getScratches().get(1).setLength(scratch.getStartId()-pivot1.getScratches().get(1).getStartId()-1);
                       pivot1.getScratches().get(1).setLow(scratch.getLow());
                       pivot1.getScratches().add(scratch);
                       returnPivotList.add(pivot1);
                   }
               }
            }else {                                    // The pattern-ending pivot is normal;
                Pivot pivot1=new Pivot(pivot);
                int startId=pivot.getScratches().get(1).getStartId()+pivot.getScratches().get(1).getLength()-1;
                int n=0;
                while (allCompoundScratches.get(n).getStartId()<startId){n++;}
                for(int t=n;t<allCompoundScratches.size();t++){
                    if(pivot.getScratches().get(1).getStatus()*allCompoundScratches.get(t).getStatus()<0
                            &&allCompoundScratches.get(t).getStartId()==startId){
                        Scratch scratch=new Scratch(allCompoundScratches.get(t));
                        pivot1.getScratches().add(scratch);
                    }
                }
                returnPivotList.add(pivot1);
            }
        }
        return returnPivotList;
    }
}
