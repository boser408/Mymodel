package service.impl;

import com.myproject.mymodel.domain.Dpattern;
import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Pivot;
import com.myproject.mymodel.domain.Scratch;
import service.PivotHandle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PivotHandleImpl implements PivotHandle {
    public static final double controlFactor=0.7;
    public static final int pivotLength=6;
    public int numberofLoop;
    public int getNumberofLoop() {
        return numberofLoop;
    }
    public double getControlFactor(){return controlFactor;}

    @Override
    public Scratch checkHiddenScratch(List<HighLowPrice> highLowPrices,int scratchdirection) {
        //System.out.println("Enter checkHiddenScratch--Start with "+ highLowPrices.get(0).toString()+"Size of List"+highLowPrices.get(highLowPrices.size()-1));
        int endindex=highLowPrices.size()-1;
        Scratch scratch=new Scratch(highLowPrices.get(endindex));
        int loopEnd = highLowPrices.size()-(pivotLength-1);
        for(int n=0;n<loopEnd;n++){

            List<HighLowPrice> partialList=highLowPrices.subList(n,endindex+1);
            double valueofHigh=partialList.stream().mapToDouble(HighLowPrice::getHigh).max().getAsDouble();
            double valueofLow=partialList.stream().mapToDouble(HighLowPrice::getLow).min().getAsDouble();
            float maxHigh=(float) valueofHigh;
            float minLow=(float)valueofLow;

            if(scratchdirection==-1){
                boolean crite1= maxHigh== partialList.get(0).getHigh();
                boolean crite2= minLow== highLowPrices.get(endindex).getLow();

                if(crite1 && crite2){
                    //System.out.println("Enter Stage--111111");
                    scratch.setLength(endindex-n+1);
                    scratch.setStartId(partialList.get(0).getId());
                    scratch.setHigh(partialList.get(0).getHigh());
                    scratch.setStatus(-1);
                    break;
                }
            }else {
                boolean crite1=(float)valueofHigh==highLowPrices.get(endindex).getHigh();
                boolean crite2=(float)valueofLow==partialList.get(0).getLow();
                if(crite1 && crite2){
                    //System.out.println("Enter Stage--222222222");
                    scratch.setLength(endindex-n+1);
                    scratch.setStartId(partialList.get(0).getId());
                    scratch.setLow(partialList.get(0).getLow());
                    scratch.setStatus(1);
                    break;
                }
            }
        }
        //System.out.println("Returned Scratch is "+scratch.toString());
        return scratch;
    }
    @Override
    public Dpattern findDpattern(Pivot pivot) {
        Dpattern foundDpattern=new Dpattern(pivot);
        Scratch scratch=new Scratch(pivot);

        //System.out.println("pivot in findDpattern is XXXXXXXX:"+pivot.toString());
        for(int n=0;n<pivot.getScratches().size()-1;n++){
            for(int i=n+1;i<pivot.getScratches().size();i++){
                double factor=(double) pivot.getScratches().get(i).getLength()/pivot.getScratches().get(n).getLength();
                if(factor>=1/controlFactor){break;}
                //System.out.println("i="+i+","+"n="+n+","+"factor="+factor);
                if(pivot.getPivotType()>=1){

                    if((factor>controlFactor && factor<1/controlFactor) && pivot.getScratches().get(i).getLow()>pivot.getScratches().get(n).getHigh()){
                        /*System.out.println("Dpattern Found in"+pivot.toString());
                        System.out.println("the 1st Scratch is"+pivot.getScratches().get(n).toString());
                        System.out.println("the 2nd Scratch is"+pivot.getScratches().get(i).toString());*/
                        Pivot tmppivot=new Pivot(scratch);
                        tmppivot.setPivotType(pivot.getPivotType());
                        tmppivot.getScratches().add(pivot.getScratches().get(n));
                        tmppivot.getScratches().add(pivot.getScratches().get(i));
                        foundDpattern.getFeaturePivots().add(tmppivot);

                    }
                }else if(pivot.getPivotType()<=-1){
                  if((factor>controlFactor && factor<1/controlFactor) && pivot.getScratches().get(n).getLow()>pivot.getScratches().get(i).getHigh()){
                        /*System.out.println("Dpattern Found in"+pivot.toString());
                        System.out.println("the 1st Scratch is"+pivot.getScratches().get(n).toString());
                        System.out.println("the 2nd Scratch is"+pivot.getScratches().get(i).toString());*/
                      Pivot tmppivot=new Pivot(scratch);
                      tmppivot.setPivotType(pivot.getPivotType());
                      tmppivot.getScratches().add(pivot.getScratches().get(n));
                      tmppivot.getScratches().add(pivot.getScratches().get(i));
                      //System.out.println("tmppivot is "+tmppivot.toString());

                      foundDpattern.getFeaturePivots().add(tmppivot);
                      //System.out.println("foundDpattern is "+foundDpattern.toString());

                    }
                }
            }

        }
            //System.out.println(" returning foundDpattern is :"+foundDpattern.toString());
            return foundDpattern;
    }
    @Override
    public Dpattern findTpattern(Pivot pivot) {
        Dpattern returnTpattern=new Dpattern(pivot);
        Scratch scratch=new Scratch(pivot);

        for(int n=0;n<pivot.getScratches().size()-1;n++){
            for(int i=n+1;i<pivot.getScratches().size();i++){
                double factor=(double) pivot.getScratches().get(i).getLength()/pivot.getScratches().get(n).getLength();
                if(factor>=1/controlFactor){break;}
                if(pivot.getPivotType()>=1){
                    if((factor>controlFactor && factor<1/controlFactor) && pivot.getScratches().get(i).getLow()<=pivot.getScratches().get(n).getHigh()){
                        //System.out.println("i="+i+","+"n="+n+","+"factor="+factor);
                        for(int t=i+1;t<pivot.getScratches().size();t++){
                            double factor2=(double) pivot.getScratches().get(t).getLength()/pivot.getScratches().get(n).getLength();
                            double factor3=(double) pivot.getScratches().get(t).getLength()/pivot.getScratches().get(i).getLength();

                            if(factor2>=1/controlFactor||factor3>=1/controlFactor){break;}
                            //System.out.println("factor2= "+factor2+" factor3="+factor3);
                            boolean criteria=(factor2>controlFactor && factor2<1/controlFactor)&&(factor3>controlFactor && factor3<1/controlFactor);
                            if(criteria && pivot.getScratches().get(t).getLow()<=pivot.getScratches().get(n).getHigh() ){
                                //System.out.println("found-------------------111111");
                                Pivot tmppivot=new Pivot(scratch);
                                tmppivot.setPivotType(pivot.getPivotType());
                                tmppivot.getScratches().add(pivot.getScratches().get(n));
                                tmppivot.getScratches().add(pivot.getScratches().get(i));
                                tmppivot.getScratches().add(pivot.getScratches().get(t));
                                returnTpattern.getFeaturePivots().add(tmppivot);
                                //System.out.println("found Tpattern ="+returnTpattern.toString());
                            }
                        }
                    }
                }else if(pivot.getPivotType()<=-1){
                    if((factor>controlFactor && factor<1/controlFactor) && pivot.getScratches().get(n).getLow()<=pivot.getScratches().get(i).getHigh()){
                        //System.out.println("i="+i+","+"n="+n+","+"factor="+factor);
                        for(int t=i+1;t<pivot.getScratches().size();t++){
                            double factor2=(double) pivot.getScratches().get(t).getLength()/pivot.getScratches().get(n).getLength();
                            double factor3=(double) pivot.getScratches().get(t).getLength()/pivot.getScratches().get(i).getLength();

                            if(factor2>=1/controlFactor||factor3>=1/controlFactor){break;}
                            //System.out.println("factor2="+factor2+" factor3="+factor3);
                            boolean criteria=(factor2>controlFactor && factor2<1/controlFactor)&&(factor3>controlFactor && factor3<1/controlFactor);
                            if(criteria && pivot.getScratches().get(n).getLow()<=pivot.getScratches().get(t).getHigh() ){
                                //System.out.println("found-------------------222222222");
                                Pivot tmppivot=new Pivot(scratch);
                                tmppivot.setPivotType(pivot.getPivotType());
                                tmppivot.getScratches().add(pivot.getScratches().get(n));
                                tmppivot.getScratches().add(pivot.getScratches().get(i));
                                tmppivot.getScratches().add(pivot.getScratches().get(t));
                                returnTpattern.getFeaturePivots().add(tmppivot);
                            }
                        }

                    }
                }
            }
        }
        if(returnTpattern.getFeaturePivots().size()>=1){ // Mark the pivot as Tripple-Pivots pattern;

            returnTpattern.setPivotDirection(returnTpattern.getPivotDirection()*33);
           // System.out.println("Final--------------------Returned Tpattern ="+returnTpattern.toString());
        }
        return returnTpattern;
    }
    @Override
    public List<Pivot> dwsubpivotHandle(List<Pivot> cleanedPivotList, Pivot subpivot, int n, int endNumberofsubpivot) {
        List<Pivot> returnPivotList=new ArrayList<>();
        if(n+1-endNumberofsubpivot>=4){//Potentiall exists a subpivotlist of current subpivot;
                        int i=endNumberofsubpivot+1;
                        int numberofTmpLow=i;
                        Pivot tmpPivot=new Pivot(cleanedPivotList.get(i));
                        List<Scratch> scratchesForInsert=new ArrayList<>();
                        i=i+2;
                        while (i<=n){
                            Scratch scratch=new Scratch(cleanedPivotList.get(i-1));
                            scratchesForInsert.add(scratch);
                            if(cleanedPivotList.get(i).getHigh()>=tmpPivot.getHigh()){
                                numberofTmpLow=i;
                                tmpPivot.setLength(cleanedPivotList.get(i).getStartId()-tmpPivot.getStartId()
                                        +cleanedPivotList.get(i).getLength());
                                tmpPivot.setHigh(cleanedPivotList.get(i).getHigh());
                                for(Scratch scratch1:scratchesForInsert){
                                    tmpPivot.getScratches().add(scratch1);
                                }
                            }
                            i=i+2;
                        }
                        returnPivotList.add(0,tmpPivot);
                        Scratch scratch=new Scratch(tmpPivot); // 2. Merge the tmpPivot into subpivot;
                        subpivot.getScratches().add(scratch);
                        if(numberofTmpLow<n){ // 3. Handle the low point of tmpPivot and merge the left pivots into subpivot
                            for(int t=numberofTmpLow+2;t<=n;t=t+2){
                                Scratch scratch2=new Scratch(cleanedPivotList.get(t));
                                subpivot.getScratches().add(scratch2);
                            }
                        }
        }else { // Only 1 pivot between the beginning subpivot and ending subpivot;
                    returnPivotList.add(0,cleanedPivotList.get(n));
                    Scratch scratch=new Scratch(cleanedPivotList.get(n));
                    subpivot.getScratches().add(scratch);
        }
                    subpivot.setLength(cleanedPivotList.get(n+1).getStartId()-subpivot.getStartId()
                            +cleanedPivotList.get(n+1).getLength());
                    subpivot.setLow(cleanedPivotList.get(n+1).getLow());

                    for(Scratch scratch:cleanedPivotList.get(n+1).getScratches()){
                        subpivot.getScratches().add(scratch);
                    }

                    if(subpivot.getScratches().size()>1){
                        subpivot.getScratches().sort(Comparator.comparingInt(Scratch::getStartId));
                    }
        returnPivotList.add(1,subpivot);
        return returnPivotList;
    }
    @Override
    public List<Pivot> subpivotHandle(List<Pivot> cleanedPivotList, Pivot subpivot, int n, int endNumberofsubpivot) {
        List<Pivot> returnPivotList=new ArrayList<>();
        if(n+1-endNumberofsubpivot>=4){//Potentiall exists a subpivotlist of current subpivot;
            int i=endNumberofsubpivot+1;
            int numberofTmpLow=i;
            Pivot tmpPivot=new Pivot(cleanedPivotList.get(i));
            List<Scratch> scratchesForInsert=new ArrayList<>();
            i=i+2;
            while (i<=n){
                Scratch scratch=new Scratch(cleanedPivotList.get(i-1));
                scratchesForInsert.add(scratch);
                if(cleanedPivotList.get(i).getLow()<=tmpPivot.getLow()){
                    numberofTmpLow=i;
                    tmpPivot.setLength(cleanedPivotList.get(i).getStartId()-tmpPivot.getStartId()
                            +cleanedPivotList.get(i).getLength());
                    tmpPivot.setLow(cleanedPivotList.get(i).getLow());
                    for(Scratch scratch1:scratchesForInsert){
                        tmpPivot.getScratches().add(scratch1);
                    }
                }
                i=i+2;
            }
            returnPivotList.add(0,tmpPivot);
            Scratch scratch=new Scratch(tmpPivot); // 2. Merge the tmpPivot into subpivot;
            subpivot.getScratches().add(scratch);
            if(numberofTmpLow<n){ // 3. Handle the low point of tmpPivot and merge the left pivots into subpivot
                for(int t=numberofTmpLow+2;t<=n;t=t+2){
                    Scratch scratch2=new Scratch(cleanedPivotList.get(t));
                    subpivot.getScratches().add(scratch2);
                }
            }
        }else { // Only 1 pivot between the beginning subpivot and ending subpivot;
            returnPivotList.add(0,cleanedPivotList.get(n));
            Scratch scratch=new Scratch(cleanedPivotList.get(n));
            subpivot.getScratches().add(scratch);
        }
        subpivot.setLength(cleanedPivotList.get(n+1).getStartId()-subpivot.getStartId()
                +cleanedPivotList.get(n+1).getLength());
        subpivot.setHigh(cleanedPivotList.get(n+1).getHigh());
        for(Scratch scratch:cleanedPivotList.get(n+1).getScratches()){
            subpivot.getScratches().add(scratch);
        }
        if(subpivot.getScratches().size()>1){
            subpivot.getScratches().sort(Comparator.comparingInt(Scratch::getStartId));
        }
        returnPivotList.add(1,subpivot);
        return returnPivotList;
    }
    @Override
    public List<Scratch> findScratches(List<HighLowPrice> highLowPrices, int startindex, int length, int pivotLength) {

        List<Scratch> scratches = new ArrayList<>();
        Scratch upscratch=new Scratch(highLowPrices.get(startindex-1));
        Scratch dwscratch=new Scratch(highLowPrices.get(startindex-1));
        int nofupscratch=0; //number when upscratch ended;
        int nofdwscratch=0; //number when dwscratch ended;
        for(int n=startindex;n<startindex+length-1;n++){
            if (upscratch.getStatus()==1) { //Scenario 1: A formed up trend scratch exists;

                if(highLowPrices.get(n).getHigh()>=upscratch.getHigh()     // 1.1: uptrend creates a new high;
                        && highLowPrices.get(n).getLow()>=upscratch.getLow() ){

                    upscratch.setLength(upscratch.getLength()+1);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    nofupscratch=n;
                    dwscratch=new Scratch(highLowPrices.get(n));
                    nofdwscratch=n;

                }else if(highLowPrices.get(n).getHigh() >upscratch.getHigh()  //1.2: a bar breaks both high and low of this uptrend scrach;
                        && highLowPrices.get(n).getLow()<upscratch.getLow()){

                    if(highLowPrices.get(n).getOpen()>highLowPrices.get(n).getClose()){
                        upscratch.setLength(upscratch.getLength()+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        Scratch scratch=new Scratch(upscratch);
                        scratches.add(scratch);

                        upscratch=new Scratch(highLowPrices.get(n));
                        nofupscratch=n;

                        dwscratch=new Scratch(highLowPrices.get(n));
                        nofdwscratch=n;

                    }else {
                        dwscratch.setLength(dwscratch.getLength()+1);
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

                    dwscratch.setLength(dwscratch.getLength()+1);
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
                        dwscratch.setLength(dwscratch.getLength()+1);
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        nofdwscratch=n;
                    }else if(highLowPrices.get(n).getLow()<dwscratch.getLow()){    //1.4.2: dwscratch extended by a new low;
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        dwscratch.setLength(dwscratch.getLength()+1);
                        nofdwscratch=n;
                        upscratch.setLength(upscratch.getLength()+1);

                    }else if(n-nofupscratch>=pivotLength && dwscratch.getStatus()==0
                            && (highLowPrices.get(n).getLow()==
                            ((float)highLowPrices.subList(nofupscratch+1,n+1).stream().mapToDouble(HighLowPrice::getLow).min().getAsDouble())) ){ // find hidden uptrend scratch;
                        /*float localHigh=(float)highLowPrices.subList(nofupscratch+1,n+1).stream().mapToDouble(HighLowPrice::getHigh).max().getAsDouble();
                        boolean crite1=highLowPrices.get(n).getHigh()==localHigh;*/

                            upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);
                            Scratch scratch=new Scratch(upscratch);
                            scratches.add(scratch);
                            upscratch=new Scratch(highLowPrices.get(n));
                            nofupscratch=n;
                            dwscratch.setLow(highLowPrices.get(n).getLow());
                            dwscratch.setLength(highLowPrices.get(n).getId()-dwscratch.getStartId()+1);
                            dwscratch.setStatus(-1);
                            nofdwscratch=n;

                    }else {
                        if(n-nofdwscratch>=pivotLength){
                            List<HighLowPrice> partialHighLowPrice=highLowPrices.subList(nofdwscratch+1,n+1);

                            Scratch tempscratch=checkHiddenScratch(partialHighLowPrice,-1);
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
                            }

                        }else {
                            upscratch.setLength(upscratch.getLength()+1);
                            dwscratch.setLength(dwscratch.getLength()+1);
                        }
                    }
                }

            }else if (dwscratch.getStatus()==-1){ //Scenario 2: A formed down trend scratch exists;
                if(highLowPrices.get(n).getLow()<=dwscratch.getLow()&&highLowPrices.get(n).getHigh()<=dwscratch.getHigh()){
                    dwscratch.setLength(dwscratch.getLength()+1);
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    nofdwscratch=n;
                    upscratch=new Scratch(highLowPrices.get(n));
                    nofupscratch=n;

                }else if(highLowPrices.get(n).getLow()<dwscratch.getLow()&&highLowPrices.get(n).getHigh()>dwscratch.getHigh()){

                    if(highLowPrices.get(n).getOpen()>highLowPrices.get(n).getClose()){
                        upscratch.setLength(upscratch.getLength()+1);
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
                        dwscratch.setLength(dwscratch.getLength()+1);
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

                    upscratch.setLength(upscratch.getLength()+1);
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
                }else if(n-nofdwscratch>=pivotLength && upscratch.getStatus()==0
                        &&(highLowPrices.get(n).getHigh()==
                        ((float)highLowPrices.subList(nofupscratch+1,n+1).stream().mapToDouble(HighLowPrice::getHigh).max().getAsDouble())) ){ // find hidden uptrend scratch;

                        dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);
                        Scratch dscratch=new Scratch(dwscratch);
                        scratches.add(dscratch);
                        dwscratch=new Scratch(highLowPrices.get(n));
                        nofdwscratch=n;
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLength(highLowPrices.get(n).getId()-upscratch.getStartId()+1);
                        upscratch.setStatus(1);
                        nofupscratch=n;

                } else {
                    if(highLowPrices.get(n).getHigh()>dwscratch.getHigh()){
                        dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);
                        Scratch dscratch=new Scratch(dwscratch);
                        scratches.add(dscratch);

                        dwscratch=new Scratch(highLowPrices.get(n));
                        nofdwscratch=n;
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLength(upscratch.getLength()+1);
                        nofupscratch=n;
                    }else if(highLowPrices.get(n).getHigh()>upscratch.getHigh()){
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLength(upscratch.getLength()+1);
                        nofupscratch=n;
                        dwscratch.setLength(dwscratch.getLength()+1);

                    }else {
                        if(n-nofupscratch>=pivotLength){
                            List<HighLowPrice> partialHighLowPrice=highLowPrices.subList(nofupscratch+1,n+1);

                            Scratch tempscratch=checkHiddenScratch(partialHighLowPrice,1);
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
                            }

                        }else {
                            upscratch.setLength(upscratch.getLength()+1);
                            dwscratch.setLength(dwscratch.getLength()+1);
                        }

                    }
                }

            }else {     // Scenario 3:  no direction
                if(highLowPrices.get(n).getHigh()>upscratch.getHigh() && highLowPrices.get(n).getHigh()>dwscratch.getHigh()
                        && highLowPrices.get(n).getLow()>=upscratch.getLow()){
                    upscratch.setLength(upscratch.getLength()+1);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    nofupscratch=n;
                    if(dwscratch.getStartId()<upscratch.getStartId()){
                        dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);

                        Scratch dscratch=new Scratch(dwscratch);//amended because new constructor is added;
                        scratches.add(dscratch);

                    }
                    dwscratch=new Scratch(highLowPrices.get(n));
                    nofdwscratch=n;
                    if(n-upscratch.getStartId()>=pivotLength-2){ // An up trend scratch formed
                        upscratch.setStatus(1);
                    }
                }else if(highLowPrices.get(n).getLow()<dwscratch.getLow() && highLowPrices.get(n).getLow()< upscratch.getLow()
                        && highLowPrices.get(n).getHigh()<=dwscratch.getHigh()){
                    dwscratch.setLength(dwscratch.getLength()+1);
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    nofdwscratch=n;
                    if(upscratch.getStartId()<dwscratch.getStartId()){
                        upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);

                        Scratch scratch=new Scratch(upscratch);//amended because new constructor is added;
                        scratches.add(scratch);

                    }
                    upscratch=new Scratch(highLowPrices.get(n));
                    nofupscratch=n;
                    if(n-dwscratch.getStartId()>=pivotLength-2){ // A down trend scratch formed
                        dwscratch.setStatus(-1);
                    }

                }else if(highLowPrices.get(n).getHigh()>=upscratch.getHigh() && n-upscratch.getStartId()>=pivotLength-2){
                    upscratch.setLength(upscratch.getLength()+1);
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
                    dwscratch.setLength(dwscratch.getLength()+1);
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
                        upscratch.setLength(upscratch.getLength()+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        if(n-upscratch.getStartId()>=pivotLength-2){ // An up trend scratch formed
                            upscratch.setStatus(1);
                        }
                        Scratch scratch=new Scratch(upscratch);
                        scratches.add(scratch);
                        System.out.println("scratch added"+scratch.toString());

                        if(dwscratch.getStartId()<upscratch.getStartId()){
                            dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);
                            Scratch dscratch=new Scratch(dwscratch);
                            scratches.add(dscratch);
                            System.out.println("dscratch added"+dscratch.toString());
                        }
                        upscratch=new Scratch(highLowPrices.get(n));
                        nofupscratch=n;
                        dwscratch=new Scratch(highLowPrices.get(n));
                        nofdwscratch=n;

                    }else {
                        dwscratch.setLength(dwscratch.getLength()+1);
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
                    upscratch.setLength(upscratch.getLength()+1);
                    nofupscratch=n;
                    dwscratch.setLength(dwscratch.getLength()+1);

                }else if(highLowPrices.get(n).getLow()<dwscratch.getLow()){
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    dwscratch.setLength(dwscratch.getLength()+1);
                    nofdwscratch=n;
                    upscratch.setLength(upscratch.getLength()+1);

                }else {
                    if(upscratch.getStartId()<dwscratch.getStartId()){
                        if(n-nofupscratch>=pivotLength && dwscratch.getStatus()==0
                                && (highLowPrices.get(n).getLow()==
                                ((float)highLowPrices.subList(nofupscratch+1,n+1).stream().mapToDouble(HighLowPrice::getLow).min().getAsDouble()))){// find hidden uptrend scratch;

                            upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);
                            Scratch scratch=new Scratch(upscratch);
                            scratches.add(scratch);
                            upscratch=new Scratch(highLowPrices.get(n));
                            nofupscratch=n;
                            dwscratch.setLow(highLowPrices.get(n).getLow());
                            dwscratch.setLength(highLowPrices.get(n).getId()-dwscratch.getStartId()+1);
                            dwscratch.setStatus(-1);
                            nofdwscratch=n;

                        }else if(n-nofdwscratch>=pivotLength){
                            List<HighLowPrice> partialHighLowPrice=highLowPrices.subList(nofdwscratch+1,n+1);
                            Scratch tempscratch=checkHiddenScratch(partialHighLowPrice,-1);
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
                            }

                        }else {
                            upscratch.setLength(upscratch.getLength()+1);
                            dwscratch.setLength(dwscratch.getLength()+1);
                        }
                    }else {
                        if(n-nofdwscratch>=pivotLength && upscratch.getStatus()==0
                                &&(highLowPrices.get(n).getHigh()==
                                ((float)highLowPrices.subList(nofupscratch+1,n+1).stream().mapToDouble(HighLowPrice::getHigh).max().getAsDouble()))){// find hidden uptrend scratch;

                            dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);
                            Scratch dscratch=new Scratch(dwscratch);
                            scratches.add(dscratch);
                            dwscratch=new Scratch(highLowPrices.get(n));
                            nofdwscratch=n;
                            upscratch.setHigh(highLowPrices.get(n).getHigh());
                            upscratch.setLength(highLowPrices.get(n).getId()-upscratch.getStartId()+1);
                            upscratch.setStatus(1);
                            nofupscratch=n;

                        }else if(n-nofupscratch>=pivotLength){
                            List<HighLowPrice> partialHighLowPrice=highLowPrices.subList(nofupscratch+1,n+1);

                            Scratch tempscratch=checkHiddenScratch(partialHighLowPrice,1);
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
                            }

                        }else {
                            upscratch.setLength(upscratch.getLength()+1);
                            dwscratch.setLength(dwscratch.getLength()+1);
                        }
                    }
                }
            }
        }
        scratches.add(upscratch);
        scratches.add(dwscratch);
        scratches.sort(Comparator.comparingInt(Scratch::getStartId));
        return scratches;
    }
    @Override
    public List<Pivot> findPivots(List<Scratch> list, int n) {
        List<Pivot> pivots=new ArrayList<>();
        List<Scratch> upscratches=new ArrayList<>();
        List<Scratch> dwscratches=new ArrayList<>();
        Pivot uppivot=new Pivot();
        Pivot dwpivot=new Pivot();
        uppivot.setPivotType(0);
        uppivot.setScratches(upscratches);
        dwpivot.setPivotType(0);
        dwpivot.setScratches(dwscratches);

        while (n<list.size()-1 && uppivot.getScratches().isEmpty() && dwpivot.getScratches().isEmpty()){

            if (list.get(n).getStatus()==1 && list.get(n-1).getHigh()>list.get(n).getHigh()){
                dwpivot.setLength(list.get(n-1).getLength()+list.get(n).getLength()-1);
                dwpivot.setStartId(list.get(n-1).getStartId());
                dwpivot.setHigh(list.get(n-1).getHigh());
                dwpivot.setLow(list.get(n).getLow());
                dwpivot.setPivotType(0);
                dwpivot.getScratches().add(list.get(n));

                uppivot.setLength(list.get(n).getLength());
                uppivot.setStartId(list.get(n).getStartId());
                uppivot.setHigh(list.get(n).getHigh());
                uppivot.setLow(list.get(n).getLow());

            }else if(list.get(n).getStatus()==-1 && list.get(n-1).getLow()<list.get(n).getLow()){
                uppivot.setLength(list.get(n-1).getLength()+list.get(n).getLength()-1);
                uppivot.setStartId(list.get(n-1).getStartId());
                uppivot.setHigh(list.get(n).getHigh());
                uppivot.setLow(list.get(n-1).getLow());
                uppivot.setPivotType(0);
                uppivot.getScratches().add(list.get(n));

                dwpivot.setLength(list.get(n).getLength());
                dwpivot.setStartId(list.get(n).getStartId());
                dwpivot.setHigh(list.get(n).getHigh());
                dwpivot.setLow(list.get(n).getLow());

            }
            n++;
        }
        while (n<list.size()-1){
            if(dwpivot.getScratches().size()>0 && dwpivot.getStartId()<uppivot.getStartId()){

                if(list.get(n).getLow()<=dwpivot.getLow()){
                    dwpivot.setLength(dwpivot.getLength()+list.get(n).getLength()-1);
                    dwpivot.setLow(list.get(n).getLow());
                    dwpivot.setPivotType(-1);
                    pivots.add(dwpivot);
                    break;
                }else if(uppivot.getScratches().size()>0 && list.get(n).getHigh()>=uppivot.getHigh()){

                    uppivot.setLength(list.get(n).getStartId()-uppivot.getStartId()+list.get(n).getLength());
                    uppivot.setHigh(list.get(n).getHigh());
                    uppivot.setPivotType(1);
                    pivots.add(uppivot);

                    dwpivot.setLength(uppivot.getStartId()-dwpivot.getStartId()+1);
                    pivots.add(dwpivot);
                    break;
                }else{

                    if(list.get(n).getStatus()==-1) {

                        uppivot.setLength(uppivot.getLength()+list.get(n).getLength()-1);
                        dwpivot.setLength(dwpivot.getLength()+list.get(n).getLength()-1);
                        uppivot.getScratches().add(list.get(n));
                    }else if(list.get(n).getStatus()==1){

                        uppivot.setLength(uppivot.getLength()+list.get(n).getLength()-1);
                        dwpivot.setLength(dwpivot.getLength()+list.get(n).getLength()-1);
                        dwpivot.getScratches().add(list.get(n));

                    }else {

                        dwpivot.setLength(dwpivot.getLength()+list.get(n).getLength()-1);
                        uppivot.setLength(uppivot.getLength()+list.get(n).getLength()-1);

                    }
                    n++;
                }

            }else if(uppivot.getScratches().size()>0 && uppivot.getLength()>dwpivot.getLength()){

                if(list.get(n).getHigh()>=uppivot.getHigh()){
                    uppivot.setLength(uppivot.getLength()+list.get(n).getLength()-1);
                    uppivot.setHigh(list.get(n+1).getHigh());
                    uppivot.setPivotType(1);
                    pivots.add(uppivot);
                    break;
                }else if( dwpivot.getScratches().size()>0 && list.get(n).getLow()<=dwpivot.getLow()){
                    dwpivot.setLength(dwpivot.getLength()+list.get(n).getLength()-1);
                    dwpivot.setLow(list.get(n).getLow());
                    dwpivot.setPivotType(-1);
                    pivots.add(dwpivot);

                    uppivot.setLength(dwpivot.getStartId()-uppivot.getStartId()+1);
                    pivots.add(uppivot);
                    break;
                }else {
                    if(list.get(n).getStatus()==1){
                        uppivot.setLength(uppivot.getLength()+list.get(n).getLength()-1);
                        dwpivot.setLength(dwpivot.getLength()+list.get(n).getLength()-1);
                        dwpivot.getScratches().add(list.get(n));

                    }else if(list.get(n).getStatus()==-1){
                        uppivot.setLength(uppivot.getLength()+list.get(n).getLength()-1);
                        dwpivot.setLength(dwpivot.getLength()+list.get(n).getLength()-1);
                        uppivot.getScratches().add(list.get(n));
                    }else {
                        uppivot.setLength(uppivot.getLength()+list.get(n).getLength()-1);
                        dwpivot.setLength(dwpivot.getLength()+list.get(n).getLength()-1);
                    }
                    n++;
                }
            }
        }
         numberofLoop=n;
         return pivots;

    }
    @Override
    public List<Pivot> pivotExtension(List<Scratch> scratches, Pivot pivot, int n) {
        int nbegin=n;
        int endofmain=0;
        int endofsub=0;
        List<Pivot> pivots=new ArrayList<>();
        List<Scratch> scratchList=new ArrayList<>();
        Pivot uppivot=new Pivot();
        Pivot dwpivot=new Pivot();
        if(pivot.getPivotType()==1){
            dwpivot=new Pivot(scratches.get(n));
        }else {
            uppivot=new Pivot(scratches.get(n));
        }

        while (n<=scratches.size()-3){

            if(pivot.getPivotType()==1){

                if(scratches.get(n).getLow()<pivot.getLow()){

                    pivot.setLength(dwpivot.getStartId()-pivot.getStartId()+1);
                    pivots.add(pivot);

                    if(dwpivot.getScratches().size()>0){

                        dwpivot.setLength(scratches.get(n).getStartId()-dwpivot.getStartId()+scratches.get(n).getLength());
                        dwpivot.setLow(scratches.get(n).getLow());
                        dwpivot.setPivotType(-1);
                    }
                    /*System.out.println("11--pivot="+pivot.toString());
                    System.out.println("11--dwpivot="+dwpivot.toString());*/
                    pivots.add(dwpivot);
                    break;

                }else if(dwpivot.getScratches().size()>0 && scratches.get(n).getLow()<=dwpivot.getLow()) {

                    pivot.setLength(dwpivot.getStartId()-pivot.getStartId()+1);
                    pivots.add(pivot);

                    dwpivot.setLength(scratches.get(n).getStartId()-dwpivot.getStartId()+scratches.get(n).getLength());
                    dwpivot.setLow(scratches.get(n).getLow());
                    dwpivot.setPivotType(-1);
                    pivots.add(dwpivot);
                    /*System.out.println("22--pivot="+pivot.toString());
                    System.out.println("22--dwpivot="+dwpivot.toString());*/
                    break;
                }else if(scratches.get(n+1).getHigh()>=pivot.getHigh()) {

                    pivot.setLength(scratches.get(n+1).getStartId()-pivot.getStartId()+scratches.get(n+1).getLength());
                    pivot.setHigh(scratches.get(n+1).getHigh());
                    endofmain=n+1;
                    if(scratches.get(n).getStatus()==-1){
                        pivot.getScratches().add(scratches.get(n));
                    }
                    if(scratchList.size()>0){
                        pivot.getScratches().addAll(scratchList);
                        scratchList.clear();
                    }
                    dwpivot=new Pivot(scratches.get(n+2));
                    endofsub=n+2;
                    /*System.out.println("33--pivot="+pivot.toString());
                    System.out.println("33--dwpivot="+dwpivot.toString());
                    System.out.println("scratch n+1 ="+scratches.get(n+1).toString());
                    System.out.println("scratch n+2 ="+scratches.get(n+2).toString());*/
                }else {

                        if (scratches.get(n).getStatus()==-1 ){
                            if(n>nbegin && scratches.get(n-1).getLow()<scratches.get(n).getLow()
                            && scratches.get(n+1).getHigh()>scratches.get(n).getHigh()){
                                dwpivot.getScratches().clear();
                                pivots.add(pivot);
                                pivots.add(dwpivot);
                                n=endofsub;
                                /*System.out.println("00--pivot="+pivot.toString());
                                System.out.println("00--dwpivot="+dwpivot.toString());*/
                                break;
                            }else {
                                scratchList.add(scratches.get(n));
                            }
                        }
                        if(scratches.get(n+1).getStatus()==1){
                            dwpivot.getScratches().add(scratches.get(n+1));
                        }
                    /*System.out.println("44--pivot="+pivot.toString());
                    System.out.println("44--dwpivot="+dwpivot.toString());
                    System.out.println("44--scratchList="+scratchList.toString());*/
                }

            }else if(pivot.getPivotType()==-1){

                if(scratches.get(n).getHigh()>pivot.getHigh()){//2.1: downtrend major pivot broken by a new high;

                    pivot.setLength(uppivot.getStartId()-pivot.getStartId()+1);
                    pivots.add(pivot);

                    if(uppivot.getScratches().size()>0){

                        uppivot.setLength(scratches.get(n).getStartId()-uppivot.getStartId()+scratches.get(n).getLength());
                        uppivot.setHigh(scratches.get(n).getHigh());
                        uppivot.setPivotType(1);

                    }
                    /*System.out.println("55--pivot="+pivot.toString());
                    System.out.println("55--uppivot="+uppivot.toString());*/
                    pivots.add(uppivot);
                    break;

                }else if(uppivot.getScratches().size()>0 && scratches.get(n).getHigh()>=uppivot.getHigh()){//2.2: uppivot extend by a rebound new high;

                    pivot.setLength(uppivot.getStartId()-pivot.getStartId()+1);
                    pivots.add(pivot);

                    uppivot.setLength(scratches.get(n).getStartId()-uppivot.getStartId()+scratches.get(n).getLength());
                    uppivot.setHigh(scratches.get(n).getHigh());
                    uppivot.setPivotType(1);
                    pivots.add(uppivot);
                    /*System.out.println("66--pivot="+pivot.toString());
                    System.out.println("66--uppivot="+uppivot.toString());*/
                    break;
                }else if(scratches.get(n+1).getLow()<=pivot.getLow()) {//2.3: major downtrend pivot extend by a new low;

                    pivot.setLength(scratches.get(n+1).getLength()+scratches.get(n+1).getStartId()-pivot.getStartId());
                    pivot.setLow(scratches.get(n+1).getLow());
                    endofmain=n+1;
                    if(scratches.get(n).getStatus()==1){
                        pivot.getScratches().add(scratches.get(n));
                    }
                    if(scratchList.size()>0){
                       pivot.getScratches().addAll(scratchList);
                       scratchList.clear();
                    }
                    uppivot=new Pivot(scratches.get(n+2));
                    endofsub=n+2;
                    /*System.out.println("77--pivot="+pivot.toString());
                    System.out.println("77--uppivot="+uppivot.toString());*/

                }else {
                       if(scratches.get(n).getStatus()==1 ){
                           if(n>nbegin&&scratches.get(n-1).getHigh()>scratches.get(n).getHigh()
                                   &&scratches.get(n+1).getLow()<scratches.get(n).getLow()){
                               uppivot.getScratches().clear();
                               pivots.add(pivot);
                               pivots.add(uppivot);
                               n=endofsub;
                               /*System.out.println("99--pivot="+pivot.toString());
                               System.out.println("99--uppivot="+uppivot.toString());*/
                               break;
                           }else{
                               scratchList.add(scratches.get(n));
                           }
                       }
                       if(scratches.get(n+1).getStatus()==-1){
                           uppivot.getScratches().add(scratches.get(n+1));
                       }
                    /*System.out.println("88--pivot="+pivot.toString());
                    System.out.println("88--uppivot="+uppivot.toString());
                    System.out.println("88--scratchList="+scratchList.toString());*/
                }

            }
            n=n+2;
        }
        if(pivots.isEmpty()){
            if(pivot.getPivotType()==1){
                pivots.add(pivot);
                pivots.add(dwpivot);
            }else{
                pivots.add(pivot);
                pivots.add(uppivot);
            }
        }
        numberofLoop=n;
        return pivots;
    }
    @Override
    public List<Pivot> findMagaPivotList(List<Pivot> pivotList) {
        List<Dpattern> growDpatternList=new ArrayList<>();
        List<Pivot> magaPivotList=new ArrayList<>();

        int n=0;
        Dpattern majorDpattern=new Dpattern(pivotList.get(n));
        Dpattern subDpattern=new Dpattern();
        majorDpattern.getFeaturePivots().add(pivotList.get(n));

        while (n<pivotList.size()){
            float gap1=pivotList.get(n).getHigh()-pivotList.get(n).getLow();
            float gap2=pivotList.get(n+1).getHigh()-pivotList.get(n+1).getLow();
            if(gap1<gap2){
                growDpatternList.add(majorDpattern);
                majorDpattern=new Dpattern(pivotList.get(n+1));
                majorDpattern.getFeaturePivots().add(pivotList.get(n+1));
                n++;
            }else {
               subDpattern=new Dpattern(pivotList.get(n+1));
               subDpattern.getFeaturePivots().add(pivotList.get(n+1));
               break;
            }
        }

        n=0;                                              /////////////////
        while (pivotList.get(n).getPivotType()==0){        /////////////////
            n++;                                           /////////////////
        }                                                   /////////////////
        Pivot mainpivot=new Pivot(pivotList.get(n));         /////////////////
        Pivot subpivot=new Pivot(pivotList.get(n+1));       /////////////////

        int endNumberofsubpivot=n+1;
        while (n<pivotList.size()-2){

            if(majorDpattern.getPivotDirection()>0){ // Uptrend;
                if(pivotList.get(n+1).getLow()<majorDpattern.getLow()){ //Scenario 1
                    growDpatternList.add(majorDpattern);
                    if(endNumberofsubpivot<n-1){
                        float tmpPivotHigh=pivotList.get(endNumberofsubpivot+1).getHigh();
                        int nofHigh=endNumberofsubpivot+1;
                        for(int t=endNumberofsubpivot+3;t<=n+1;t=t+2){
                            if(pivotList.get(t).getHigh()>=tmpPivotHigh){
                                tmpPivotHigh=pivotList.get(t).getHigh();
                                nofHigh=t;
                            }
                            //subDpattern.getFeaturePivots().add(pivotList.get(t));
                        }
                        
                    }else {

                    }

                    subDpattern.setLength(pivotList.get(n+1).getStartId()-subDpattern.getStartId()+pivotList.get(n+1).getLength());
                    subDpattern.setLow(pivotList.get(n+1).getLow());
                    majorDpattern=new Dpattern(subDpattern);
                    subDpattern=new Dpattern(pivotList.get(n+2));
                    endNumberofsubpivot=n+2;

                }else {
                    if(pivotList.get(n+1).getLow()<subDpattern.getLow()){//Scenario 2

                        for(int t=endNumberofsubpivot+1;t<=n+1;t++){
                            subDpattern.setLength(pivotList.get(n+1).getStartId()-subDpattern.getStartId()+pivotList.get(n+1).getLength());
                            subDpattern.setLow(pivotList.get(n+1).getLow());
                            subDpattern.getFeaturePivots().add(pivotList.get(t));
                        }
                        endNumberofsubpivot=n+1;
                    }
                    if(pivotList.get(n+2).getHigh()>majorDpattern.getHigh()) {//Scenario 3
                        growDpatternList.add(subDpattern);


                        majorDpattern.setLength(pivotList.get(n+2).getStartId()-mainpivot.getStartId()
                                +pivotList.get(n+2).getLength());
                        majorDpattern.setHigh(pivotList.get(n+2).getHigh());
                        ////////////////////////////////////////////////////////////////////////
                        Scratch scratch=new Scratch(subpivot);
                        mainpivot.getScratches().add(scratch);
                        //System.out.println("stage 13----2222222222222");
                        if(endNumberofsubpivot<n+1){
                            for(int i=endNumberofsubpivot+2;i<=n+1;i=i+2){
                                magaPivotList.add(pivotList.get(i));
                                Scratch scratch1=new Scratch(pivotList.get(i));
                                mainpivot.getScratches().add(scratch1);
                            }
                        }
                        for(Scratch scratch2:pivotList.get(n+2).getScratches()){
                            mainpivot.getScratches().add(scratch2);
                        }

                        //System.out.println("stage 13----33333333333");
                        if(mainpivot.getScratches().size()>1){
                            mainpivot.getScratches().sort(Comparator.comparingInt(Scratch::getStartId));
                        }

                        if(n<pivotList.size()-3){
                            subpivot=new Pivot(pivotList.get(n+3));
                            endNumberofsubpivot=n+3;
                        }
                        /*System.out.println("stage 13----4444444444444444444");
                        System.out.println("Stage 13--mainpivot after handle="+mainpivot.toString());
                        System.out.println("Stage 13--subpivot after handle="+subpivot.toString());*/
                    }else {
                        /*System.out.println("Entering---14");
                        System.out.println("mainpivot before handle="+mainpivot.toString());
                        System.out.println("subpivot before handle="+subpivot.toString());*/
                        if(subpivot.getPivotType()>-5 && pivotList.get(n+2).getPivotType()==1){
                            subpivot.setPivotType(-5);
                        }
                    }

                }
                if(mainpivot.getPivotType()>=0){
                    n=n+2;
                }else {
                    n=n+1;
                }
            }else if(mainpivot.getPivotType()<0){//Downtrend;
                if(pivotList.get(n+1).getHigh()>mainpivot.getHigh()){//1
                    /*System.out.println("Entering------21");
                    System.out.println("subpivot before handle="+subpivot.toString());
                    System.out.println("mainpivot before handle="+mainpivot.toString());*/
                    magaPivotList.add(mainpivot);
                    if(subpivot.getPivotType()>=5){
                        List<Pivot> returnPivotList=subpivotHandle(pivotList,subpivot,n,endNumberofsubpivot);
                        magaPivotList.add(returnPivotList.get(0));
                        subpivot=new Pivot(returnPivotList.get(1));
                        mainpivot=new Pivot(subpivot);
                    }else {
                        mainpivot=new Pivot(pivotList.get(n+1));
                    }
                    /*System.out.println("subpivot after handle="+subpivot.toString());
                    System.out.println("mainpivot after handle="+mainpivot.toString());*/
                    subpivot=new Pivot(pivotList.get(n+2));
                    endNumberofsubpivot=n+2;

                }else {
                    if(subpivot.getPivotType()>=5 && pivotList.get(n+1).getHigh()>subpivot.getHigh()){//2
                        /*System.out.println("Entering------22");
                        System.out.println("subpivot before handle="+subpivot.toString());
                        System.out.println("mainpivot before handle="+mainpivot.toString());*/
                        List<Pivot> returnPivotList=subpivotHandle(pivotList,subpivot,n,endNumberofsubpivot);
                        magaPivotList.add(returnPivotList.get(0));
                        subpivot=new Pivot(returnPivotList.get(1));
                        /*System.out.println("subpivot after handle="+subpivot.toString());
                        System.out.println("mainpivot after handle="+mainpivot.toString());*/
                        endNumberofsubpivot=n+1;
                    }else {
                        if(mainpivot.getPivotType()>-5 && pivotList.get(n+1).getPivotType()==1){
                            mainpivot.setPivotType(-5);
                        }
                    }
                    if(pivotList.get(n+2).getLow()<mainpivot.getLow()) {//3

                        magaPivotList.add(subpivot);
                        mainpivot.setLength(pivotList.get(n+2).getStartId()-mainpivot.getStartId()
                                +pivotList.get(n+2).getLength());
                        mainpivot.setLow(pivotList.get(n+2).getLow());
                        mainpivot.setPivotType(-6);
                        Scratch scratch=new Scratch(subpivot);
                        mainpivot.getScratches().add(scratch);
                        if(endNumberofsubpivot<n+1){
                            for(int i=endNumberofsubpivot+2;i<=n+1;i=i+2){
                                magaPivotList.add(pivotList.get(i));
                                Scratch scratch1=new Scratch(pivotList.get(i));
                                mainpivot.getScratches().add(scratch1);
                            }
                        }
                        for(Scratch scratch2:pivotList.get(n+2).getScratches()){
                            mainpivot.getScratches().add(scratch2);
                        }
                        if(mainpivot.getScratches().size()>1){
                            mainpivot.getScratches().sort(Comparator.comparingInt(Scratch::getStartId));
                        }
                        if(n<pivotList.size()-3){
                            subpivot=new Pivot(pivotList.get(n+3));
                            endNumberofsubpivot=n+3;
                        }
                    }else {
                        if(subpivot.getPivotType()<5 && pivotList.get(n+2).getPivotType()==-1){
                            subpivot.setPivotType(5);
                        }
                    }
                }
                if(mainpivot.getPivotType()<=0){
                    n=n+2;
                }else {
                    n=n+1;
                }
            }
        }
        magaPivotList.sort(Comparator.comparingInt(Pivot::getStartId));
        mainpivot.getScratches().sort(Comparator.comparingInt(Scratch::getStartId));
        magaPivotList.add(mainpivot);
        return magaPivotList;
    }
    @Override
    public List<Scratch> findAllPivotsByScratch(List<Scratch> scratchList) {
        List<Pivot> pivotList=new ArrayList<>();
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
                for(int i=4;n+i<scratchList.size();i=i+2){ // to check if current found pivot could extend further;
                    boolean crite11=scratchList.get(n).getHigh()>=scratchList.get(n+i).getHigh() &&
                            scratchList.get(n+i).getLow()<=lowerLow;
                    if(crite11){
                       lowerLow=scratchList.get(n+i).getLow();
                       endofpivot=n+i;
                    }else {
                       break;
                    }
                }
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
                //System.out.println("scratch added to List "+scratch.toString());
                n=endofpivot+1;
                /*System.out.println("Stage--111 n= "+n);
                System.out.println("Stage--111 scratch next to be handled is "+scratchList.get(n).toString());*/
            }else if(crite2){    // Scenarial #2: scratch n is a start scratch of uptrend pivot
                int endofpivot=n+2;
                float higherHigh=scratchList.get(n+2).getHigh();
                for(int i=4;n+i<scratchList.size();i=i+2){
                    boolean crite21=scratchList.get(n+i).getHigh()>=higherHigh &&
                            scratchList.get(n).getLow()<=scratchList.get(n+i).getLow();
                    if(crite21){
                        higherHigh=scratchList.get(n+i).getHigh();
                        endofpivot=n+i;
                    }else {
                        break;
                    }
                }
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
                //System.out.println("scratch added to List "+scratch.toString());
                n=endofpivot+1;
                /*System.out.println("Stage--222 n= "+n);
                System.out.println("Stage--222 scratch next to be handled is "+scratchList.get(n).toString());*/
            }else {              // Scenarial #3: scratch n is not a start scratch of any pivot
               /* Pivot pivot= new Pivot(scratchList.get(n));
                pivotList.add(pivot);*/
                Scratch scratch=new Scratch(scratchList.get(n));
                scratchesforLoop.add(scratch);
                //System.out.println("scratch added to List "+scratch.toString());
                n=n+1;
                /*System.out.println("Stage--333 n= "+n);
                System.out.println("Stage--333 scratch next to be handled is "+scratchList.get(n).toString());*/
            }
        }
        for(int t=n;t<scratchList.size();t++){
         scratchesforLoop.add(scratchList.get(t));
        }

        System.out.println("Size of final pivotList "+pivotList.size());
        for( n=1;n<scratchesforLoop.size()-1;n++){
            boolean crite1=scratchesforLoop.get(n).getHigh()==scratchesforLoop.get(n+1).getHigh() && scratchesforLoop.get(n).getLow()==scratchesforLoop.get(n-1).getLow();
            boolean crite2=scratchesforLoop.get(n).getHigh()==scratchesforLoop.get(n-1).getHigh() && scratchesforLoop.get(n).getLow()==scratchesforLoop.get(n+1).getLow();
            if(!crite1 && !crite2) {
                System.out.println("Check Data with scratch id ="+scratchesforLoop.get(n).toString());
            }
        }
        return scratchesforLoop;
    }

    @Override
    public List<Pivot> findAllPivots(List<Pivot> pivotListforLoop) {
        List<Pivot> pivotList=new ArrayList<>();
        int n=0;
        while (n<pivotListforLoop.size()-2){
            boolean crite1=pivotListforLoop.get(n).getHigh()>=pivotListforLoop.get(n+2).getHigh() &&
                    pivotListforLoop.get(n+2).getLow()<=pivotListforLoop.get(n).getLow() && pivotListforLoop.get(n).getPivotType()<0;
            boolean crite2=pivotListforLoop.get(n).getLow()<=pivotListforLoop.get(n+2).getLow() &&
                    pivotListforLoop.get(n+2).getHigh()>=pivotListforLoop.get(n).getHigh() && pivotListforLoop.get(n).getPivotType()>0;
            if(crite1){    // Scenarial #1: scratch n is a start scratch of downtrend pivot
                int endofpivot=n+2;
                float lowerLow=pivotListforLoop.get(n+2).getLow();
                for(int i=4;i<pivotListforLoop.size()-n;i=i+2){
                   boolean crite11=pivotListforLoop.get(n).getHigh()>=pivotListforLoop.get(n+i).getHigh() &&
                           pivotListforLoop.get(n+i).getLow()<=pivotListforLoop.get(n).getLow();
                   if(crite11){
                       lowerLow=pivotListforLoop.get(n+i).getLow();
                       endofpivot=n+i;
                   }else {
                       break;
                   }
                   Pivot pivot=new Pivot(pivotListforLoop.get(n));
                   pivot.setLength(pivotListforLoop.get(endofpivot).getStartId()-pivotListforLoop.get(n).getStartId()+pivotListforLoop.get(endofpivot).getLength());
                   pivot.setLow(lowerLow);
                   int maxLevel=pivotListforLoop.subList(n+1,endofpivot).stream().mapToInt(Pivot::getPivotType).max().getAsInt();
                   pivot.setPivotType(-(maxLevel+1));
                    for (int t=n+1;t<=endofpivot;t++){
                        pivot.getScratches().addAll(pivotListforLoop.get(t).getScratches());
                    }
                    pivotList.add(pivot);
                    n=endofpivot+1;
                    System.out.println("Stage--111 n= "+n);
                }

            }else if(crite2){    // Scenarial #2: scratch n is a start scratch of uptrend pivot
                int endofpivot=n+2;
                float higherHigh=pivotListforLoop.get(n+2).getHigh();
                for(int i=4;i<pivotListforLoop.size()-n;i=i+2){
                    boolean crite21=pivotListforLoop.get(n).getLow()<=pivotListforLoop.get(n+i).getLow() &&
                            pivotListforLoop.get(n+i).getHigh()>=pivotListforLoop.get(n).getHigh();
                    if(crite21){
                        higherHigh=pivotListforLoop.get(n+i).getHigh();
                        endofpivot=n+i;
                    }else {
                        break;
                    }
                    Pivot pivot=new Pivot(pivotListforLoop.get(n));
                    pivot.setLength(pivotListforLoop.get(endofpivot).getStartId()-pivotListforLoop.get(n).getStartId()+pivotListforLoop.get(endofpivot).getLength());
                    pivot.setHigh(higherHigh);
                    int maxLevel=pivotListforLoop.subList(n+1,endofpivot).stream().mapToInt(Pivot::getPivotType).min().getAsInt();
                    pivot.setPivotType(-(maxLevel-1));
                    for (int t=n+1;t<=endofpivot;t++){
                        pivot.getScratches().addAll(pivotListforLoop.get(t).getScratches());
                    }
                    pivotList.add(pivot);
                    n=endofpivot+1;
                    System.out.println("Stage--222 n= "+n);
                }

            }else {       // Scenarial #3: scratch n is not a start scratch of any pivot
                pivotList.add(pivotListforLoop.get(n));
                n=n+1;
                System.out.println("Stage--333 n= "+n);
            }
            for(int t=n;t<pivotListforLoop.size();t++){
                pivotList.add(pivotListforLoop.get(t));
            }
            System.out.println("Size of final pivotList "+pivotList.size());
            for(n=1;n<pivotListforLoop.size()-1;n++){
                boolean c1=pivotList.get(n).getHigh()==pivotList.get(n-1).getHigh() && pivotList.get(n).getLow()==pivotList.get(n+1).getLow();
                boolean c2=pivotList.get(n).getHigh()==pivotList.get(n+1).getHigh() && pivotList.get(n).getLow()==pivotList.get(n-1).getLow();
                if(!(c1||c2)){
                    System.out.println("Check Data with scratch id ="+pivotList.get(n).toString());
                }
            }
        }
        return pivotList;
    }

}
