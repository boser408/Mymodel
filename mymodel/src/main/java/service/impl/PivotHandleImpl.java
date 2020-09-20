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
                    n=endofpivot+1;
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
            System.out.println("Size of final pivotList "+pivotList.size());
            for( n=1;n<scratchesforLoop.size()-1;n++){
                boolean crite1=scratchesforLoop.get(n).getHigh()==scratchesforLoop.get(n+1).getHigh() && scratchesforLoop.get(n).getLow()==scratchesforLoop.get(n-1).getLow();
                boolean crite2=scratchesforLoop.get(n).getHigh()==scratchesforLoop.get(n-1).getHigh() && scratchesforLoop.get(n).getLow()==scratchesforLoop.get(n+1).getLow();
                if(!crite1 && !crite2) {
                    System.out.println("Check Data with scratch id ="+scratchesforLoop.get(n).toString());
                }
            }                      // End Line of original simple method;
            allPivotList.addAll(pivotList);
            sizeoftable=scratchesforLoop.size();
            scratchList.clear();
            scratchList.addAll(scratchesforLoop);
        }                                  // End Line of Complex method;
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
            System.out.println("Size of Loop List "+ scratchesforLoop.size());
        }
        return keyPivotList;
    }
}
