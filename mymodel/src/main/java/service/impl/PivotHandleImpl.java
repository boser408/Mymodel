package service.impl;

import com.myproject.mymodel.domain.Dpattern;
import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Pivot;
import com.myproject.mymodel.domain.Scratch;
import service.PivotHandle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PivotHandleImpl implements PivotHandle {
    public static final double controlFactor=0.7;
    public static final int pivotLength=5;
    public int numberofLoop;
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
                    /*float c1=pivot.getScratches().get(n).getHigh()-pivot.getScratches().get(n).getLow();
                    float c2=pivot.getScratches().get(i).getHigh()-pivot.getScratches().get(i).getLow();
                    float c3=pivot.getScratches().get(i).getLow()-pivot.getScratches().get(n).getHigh();
                    if(c3<(c1+c2)/2*controlFactor){
                        pivot.getScratches().remove(i);
                        i=i-1;
                        continue;
                    }*/
                    if((factor>controlFactor && factor<1/controlFactor) && pivot.getScratches().get(i).getLow()>pivot.getScratches().get(n).getHigh()){
                        /*System.out.println("Dpattern Found in"+pivot.toString());
                        System.out.println("the 1st Scratch is"+pivot.getScratches().get(n).toString());
                        System.out.println("the 2nd Scratch is"+pivot.getScratches().get(i).toString());*/
                        if(pivot.getScratches().get(i).getStatus()>-100){
                            pivot.getScratches().get(i).setStatus(pivot.getScratches().get(i).getStatus()*100);
                        }
                        Pivot tmppivot=new Pivot(scratch);
                        tmppivot.setPivotType(pivot.getPivotType());
                        tmppivot.getScratches().clear();
                        tmppivot.getScratches().add(pivot.getScratches().get(n));
                        tmppivot.getScratches().add(pivot.getScratches().get(i));
                        for(int t=i+1;t<pivot.getScratches().size();t++){     //Start Line of Looking for the first matching consolidation after Dpattern appeared;
                            double c=(double) pivot.getScratches().get(t).getLength()/pivot.getScratches().get(i).getLength();
                            if(c>controlFactor){
                                tmppivot.getScratches().add(pivot.getScratches().get(t));
                                break;
                            }
                        }                                                    //End Line of Looking for the first matching consolidation after Dpattern appeared;

                        if(tmppivot.getScratches().size()==3){               // Start Line of looking for the beginning point of currently found Dpatter;
                            Scratch scratch1=new Scratch(pivot);
                            if(n>0){
                                for(int r=n-1;r>=0;r--){
                                    double c=(double) pivot.getScratches().get(r).getLength()/pivot.getScratches().get(n).getLength();
                                    if(c>controlFactor){
                                        scratch1=new Scratch(pivot.getScratches().get(r));
                                        break;
                                    }
                                }
                            }
                            tmppivot.getScratches().add(scratch1);
                        }                                                   // End Line of looking for the beginning point of currently found Dpatter;
                        foundDpattern.getFeaturePivots().add(tmppivot);
                    }
                }else if(pivot.getPivotType()<=-1){
                    /*float c1=pivot.getScratches().get(n).getHigh()-pivot.getScratches().get(n).getLow();
                    float c2=pivot.getScratches().get(i).getHigh()-pivot.getScratches().get(i).getLow();
                    float c3=pivot.getScratches().get(n).getLow()-pivot.getScratches().get(i).getHigh();
                    if(c3<(c1+c2)/2*controlFactor){
                        pivot.getScratches().remove(i);
                        i=i-1;
                        continue;
                    }*/
                  if((factor>controlFactor && factor<1/controlFactor) && pivot.getScratches().get(n).getLow()>pivot.getScratches().get(i).getHigh()){
                        /*System.out.println("Dpattern Found in"+pivot.toString());
                        System.out.println("the 1st Scratch is"+pivot.getScratches().get(n).toString());
                        System.out.println("the 2nd Scratch is"+pivot.getScratches().get(i).toString());*/
                      if(pivot.getScratches().get(i).getStatus()<100){
                          pivot.getScratches().get(i).setStatus(pivot.getScratches().get(i).getStatus()*100);
                      }
                      Pivot tmppivot=new Pivot(scratch);
                      tmppivot.setPivotType(pivot.getPivotType());
                      tmppivot.getScratches().add(pivot.getScratches().get(n));
                      tmppivot.getScratches().add(pivot.getScratches().get(i));
                      for(int t=i+1;t<pivot.getScratches().size();t++){      //Start Line of Looking for the first matching consolidation after Dpattern appeared;
                          double c=(double) pivot.getScratches().get(t).getLength()/pivot.getScratches().get(i).getLength();
                          if(c>controlFactor){
                              tmppivot.getScratches().add(pivot.getScratches().get(t));
                              break;
                          }
                      }                                                     //End Line of Looking for the first matching consolidation after Dpattern appeared;
                      if(tmppivot.getScratches().size()==4){               // Start Line of looking for the beginning point of currently found Dpatter;
                          Scratch scratch1=new Scratch(pivot);
                          if(n>0){
                              for(int r=n-1;r>=0;r--){
                                  double c=(double) pivot.getScratches().get(r).getLength()/pivot.getScratches().get(n).getLength();
                                  if(c>controlFactor){
                                      scratch1=new Scratch(pivot.getScratches().get(r));
                                      break;
                                  }
                              }
                          }
                          tmppivot.getScratches().add(scratch1);
                      }                                                   // End Line of looking for the beginning point of currently found Dpatter;
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
    public List<Dpattern> findAllDpattern(List<Pivot> pivotList) {
        List<Dpattern> dpatternList=new ArrayList<>();
        for(Pivot pivot:pivotList){
            if(pivot.getScratches().size()>1){
                Dpattern dpattern=findDpattern(pivot);
                if(dpattern.getFeaturePivots().size()>0){
                    dpatternList.add(dpattern);
                }
            }
        }
        return dpatternList;
    }
    @Override
    public List<Scratch> findScratches(List<HighLowPrice> highLowPrices, int startindex, int length) {

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
                       // System.out.println("scratch added"+scratch.toString());

                        if(dwscratch.getStartId()<upscratch.getStartId()){
                            dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);
                            Scratch dscratch=new Scratch(dwscratch);
                            scratches.add(dscratch);
                            //System.out.println("dscratch added"+dscratch.toString());
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
                    /*for(int i=4;n+i<scratchList.size();i=i+2){ // to check if current found pivot could extend further;
                        boolean crite11=scratchList.get(n).getHigh()>=scratchList.get(n+i).getHigh() &&
                                scratchList.get(n+i).getLow()<=lowerLow;
                        if(crite11){
                            lowerLow=scratchList.get(n+i).getLow();
                            endofpivot=n+i;
                        }else {
                            break;
                        }
                    }*/
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
                    /*for(int i=4;n+i<scratchList.size();i=i+2){
                        boolean crite21=scratchList.get(n+i).getHigh()>=higherHigh &&
                                scratchList.get(n).getLow()<=scratchList.get(n+i).getLow();
                        if(crite21){
                            higherHigh=scratchList.get(n+i).getHigh();
                            endofpivot=n+i;
                        }else {
                            break;
                        }
                    }*/
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
            System.out.println("Size of Loop List "+ scratchesforLoop.size());
        }
        return keyPivotList;
    }
    @Override
    public List<Pivot> find3rdPattern(List<Pivot> pivotsForPatternSearch, List<Scratch> allCompoundScratches) {
        List<Pivot> pivotsof3rdPattern=new ArrayList<>();
        int nofoutlier=0;
        int nofmatch=0;
        int nofDPoutlier=0;
        int nofDPmatch=0;
        for(Pivot pivot:pivotsForPatternSearch){
            for(Scratch scratch:pivot.getScratches()){
                int nofStart=0;
                for(int n=0;n<allCompoundScratches.size();n++){
                    if(scratch.getStartId()==allCompoundScratches.get(n).getStartId()){
                        nofStart=n;
                        break;
                    }
                }
                for(int n=nofStart;n<allCompoundScratches.size();n++){
                    int c1=allCompoundScratches.get(n).getStatus()*scratch.getStatus();
                    float c2=(float)allCompoundScratches.get(n).getLength()/scratch.getLength();
                    /*System.out.println(scratch.toString());
                    System.out.println(allCompoundScratches.get(n).toString());
                    System.out.println("c2 = "+c2);*/
                    if(allCompoundScratches.get(n).getStartId()>scratch.getStartId() && c1>0 && c2>=controlFactor){
                        Pivot pivot1=new Pivot(scratch);
                        if(1/c2<controlFactor){
                            int deepStart=allCompoundScratches.get(n).getStartId()+(int)(scratch.getLength()*controlFactor);
                            int deepEnd=allCompoundScratches.get(n).getStartId()+(int)(scratch.getLength()/controlFactor);
                            int i;
                            for(i=n;i<allCompoundScratches.size();i++){
                               if(allCompoundScratches.get(i).getStartId()>=deepStart){
                                   break;
                               }
                            }
                            int flag=0;
                            while (allCompoundScratches.get(i).getStartId()>=deepStart && allCompoundScratches.get(i).getStartId()<=deepEnd){
                                if(allCompoundScratches.get(i).getStatus()*scratch.getStatus()<0){
                                    flag=1;
                                    System.out.println("flag=1");
                                    Scratch scratch1=new Scratch();
                                    scratch1.setLength(allCompoundScratches.get(i).getStartId()-allCompoundScratches.get(n).getStartId()+1);
                                    scratch1.setStartId(allCompoundScratches.get(n).getStartId());
                                    float high=allCompoundScratches.get(n).getHigh();
                                    float low=allCompoundScratches.get(i).getLow();
                                    if(scratch.getStatus()>0){
                                        high=allCompoundScratches.get(i).getHigh();
                                        low=allCompoundScratches.get(n).getLow();
                                    }
                                    scratch1.setHigh(high);
                                    scratch1.setLow(low);
                                    scratch1.setStatus(scratch.getStatus());
                                    pivot1.getScratches().add(scratch1);
                                    pivot1.getScratches().add(allCompoundScratches.get(i));
                                    break;
                                }
                                i++;
                            }
                            if(flag==0){
                                pivot1.getScratches().add(allCompoundScratches.get(n));
                                System.out.println("Outlier:"+pivot1.toString());
                                nofoutlier=nofoutlier+1;
                                if(scratch.getStatus()>=100 || scratch.getStatus()<=-100){
                                    nofDPoutlier=nofDPoutlier+1;
                                }
                                int endof2ndScratch=allCompoundScratches.get(n).getStartId()+allCompoundScratches.get(n).getLength()-1;
                                for( i=n+1;i<allCompoundScratches.size();i++){
                                    boolean c3=allCompoundScratches.get(i).getStatus()*scratch.getStatus()<0;
                                    if(allCompoundScratches.get(i).getStartId()==endof2ndScratch && c3){
                                        int t=i;
                                        while (allCompoundScratches.get(t).getStartId()==endof2ndScratch && c3){
                                            t++;
                                        }
                                        pivot1.getScratches().add(allCompoundScratches.get(t-1));
                                        break;
                                    }
                                }
                            }
                        }else {
                            pivot1.getScratches().add(allCompoundScratches.get(n));
                            int endof2ndScratch=allCompoundScratches.get(n).getStartId()+allCompoundScratches.get(n).getLength()-1;
                            for(int i=n+1;i<allCompoundScratches.size();i++){
                                boolean c3=allCompoundScratches.get(i).getStatus()*scratch.getStatus()<0;
                                if(allCompoundScratches.get(i).getStartId()==endof2ndScratch && c3){
                                    int t=i;
                                    while (allCompoundScratches.get(t).getStartId()==endof2ndScratch){
                                        float c4=(float)allCompoundScratches.get(t).getLength()/(scratch.getLength()+allCompoundScratches.get(n).getLength())*2;
                                        if(c4>=controlFactor){
                                            System.out.println("Match:"+pivot1.toString()+allCompoundScratches.get(t).toString());
                                            nofmatch=nofmatch+1;
                                            if(scratch.getStatus()>=100 || scratch.getStatus()<=-100){
                                                nofDPmatch=nofDPmatch+1;
                                            }
                                            t++;
                                            break;
                                        }
                                        t++;
                                    }
                                    pivot1.getScratches().add(allCompoundScratches.get(t-1));
                                    break;
                                }
                            }
                        }
                        pivotsof3rdPattern.add(pivot1);
                        break;
                    }
                }
            }
        }
        System.out.println("nofoutlier= "+nofoutlier+" nofmatch= "+nofmatch);
        System.out.println("nofDPoutlier= "+nofDPoutlier+" nofDPmatch= "+nofDPmatch);
        return pivotsof3rdPattern;
    }
    @Override
    public List<Pivot> find2ndPattern(List<Pivot> pivotsForPatternSearch, List<Scratch> allCompoundScratches) {
        List<Pivot> pivotsof2ndPattern=new ArrayList<>();
        List<Scratch> scratchesofNomatch=new ArrayList<>();
        int case0=0;
        int case1=0;
        int case2=0;
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

                    if(c1 && c2 && c3 && c4){
                        /*System.out.println("Current Scratch is "+scratch.toString());
                        System.out.println("Find a candidate "+allCompoundScratches.get(n).toString());*/
                        for(int i=n-1;allCompoundScratches.get(i).getStartId()>=startSearch;i--){
                            boolean c6=allCompoundScratches.get(n).getStartId()>allCompoundScratches.get(i).getStartId()+allCompoundScratches.get(i).getLength();
                            if(scratch.getStatus()>0){
                                if(c6 && allCompoundScratches.get(i).getLow()<scratch.getLow()
                                        && allCompoundScratches.get(i).getLow()<allCompoundScratches.get(n).getLow()){
                                    Pivot pivot1=new Pivot(scratch);

                                    pivot1.getScratches().add(allCompoundScratches.get(n));
                                    pivotsof2ndPattern.add(pivot1);
                                    flag=2;
                                    /*System.out.println("flag=2 "+pivot1.toString());
                                    System.out.println("Common Lower Low is "+allCompoundScratches.get(i).toString());*/
                                    break;
                                }
                            }else {
                                if(c6 && allCompoundScratches.get(i).getHigh()>scratch.getHigh()
                                        && allCompoundScratches.get(i).getHigh()>allCompoundScratches.get(n).getHigh()){
                                    Pivot pivot1=new Pivot(scratch);

                                    pivot1.getScratches().add(allCompoundScratches.get(n));
                                    pivotsof2ndPattern.add(pivot1);
                                    flag=2;
                                    /*System.out.println("flag=2 "+pivot1.toString());
                                    System.out.println("Common Higher High is "+allCompoundScratches.get(i).toString());*/
                                    break;
                                }
                            }
                        }
                    }
                    if(flag==2){
                        case2++;
                        break;
                    }
                }                                                        // End Line of 2nd Pattern Searching;
                if(flag<2){
                    if(flag==1){
                        System.out.println("NonMatch Case--111111111 "+scratch.toString());
                        case1++;
                    }else {
                        System.out.println("NonMatch Case--000000000 "+scratch.toString());
                        case0++;
                    }
                    scratchesofNomatch.add(scratch);
                }
            }
        }
        System.out.println("Size of scratchesofNomatch is "+scratchesofNomatch.size());
        System.out.println("case0 = "+case0 +" case1 = "+case1);
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
        return pivots;
    }
    @Override
    public List<Pivot> findSubScratch(List<Pivot> pivotsof2ndPattern, List<HighLowPrice> allPrices) {
        List<Pivot> returnPivotList=new ArrayList<>();
        for(Pivot pivot:pivotsof2ndPattern){
            float ratio=(float)pivot.getScratches().get(1).getLength()/pivot.getScratches().get(0).getLength();
            if(ratio>1/controlFactor){
               int startId=pivot.getScratches().get(1).getStartId()+(int)(pivot.getScratches().get(0).getLength()*controlFactor)-1;
               int endId=pivot.getScratches().get(1).getStartId()+(int)(pivot.getScratches().get(0).getLength()/controlFactor)-1;
               int n=0;
               while (allPrices.get(n).getId()<startId){
                   n++;
               }
               int cutpoint=n;
               if(pivot.getPivotType()<0){
                   float high=allPrices.get(n).getHigh();
                   while (allPrices.get(n).getId()<endId){
                       if(allPrices.get(n).getHigh()>high){
                           high=allPrices.get(n).getHigh();
                           cutpoint=n;
                       }
                       n++;
                   }
                   Scratch scratch=new Scratch(allPrices.get(cutpoint));
                   float low=allPrices.get(cutpoint).getLow();
                   int t=cutpoint;
                   int endpoint=t;
                   if(allPrices.get(cutpoint+1).getId()<pivot.getScratches().get(1).getStartId()+pivot.getScratches().get(1).getLength()-1
                           &&allPrices.get(cutpoint+1).getHigh()<=allPrices.get(cutpoint).getHigh()){
                       low=allPrices.get(cutpoint+1).getLow();
                       t=cutpoint+1;
                       endpoint=t+1;
                   }

                   while (allPrices.get(t).getId()<pivot.getScratches().get(1).getStartId()+pivot.getScratches().get(1).getLength()-1){
                       if(allPrices.get(t).getLow()<low){
                           low=allPrices.get(t).getLow();
                           endpoint=t;
                       }
                       t++;
                   }
                   scratch.setStatus(-1);
                   scratch.setLength(allPrices.get(endpoint).getId()-scratch.getStartId()+1);
                   scratch.setLow(low);
                   Pivot pivot1=new Pivot(pivot);
                   pivot1.getScratches().add(scratch);
                   returnPivotList.add(pivot1);
               }else {
                   float low=allPrices.get(n).getLow();
                   while (allPrices.get(n).getId()<endId){
                       if(allPrices.get(n).getLow()<low){
                           low=allPrices.get(n).getLow();
                           cutpoint=n;
                       }
                       n++;
                   }
                   Scratch scratch=new Scratch(allPrices.get(cutpoint));
                   float high=allPrices.get(cutpoint).getHigh();
                   int t=cutpoint;
                   int endpoint=t;
                   if(allPrices.get(cutpoint+1).getId()<pivot.getScratches().get(1).getStartId()+pivot.getScratches().get(1).getLength()-1
                           &&allPrices.get(cutpoint+1).getLow()>=allPrices.get(cutpoint).getLow() ){
                        high=allPrices.get(cutpoint+1).getHigh();
                        t=cutpoint+1;
                        endpoint=t+1;
                   }

                   while (allPrices.get(t).getId()<pivot.getScratches().get(1).getStartId()+pivot.getScratches().get(1).getLength()-1){
                       if(allPrices.get(t).getHigh()>high){
                           high=allPrices.get(t).getHigh();
                           endpoint=t;
                       }
                       t++;
                   }
                   scratch.setStatus(1);
                   scratch.setLength(allPrices.get(endpoint).getId()-scratch.getStartId()+1);
                   scratch.setHigh(high);
                   Pivot pivot1=new Pivot(pivot);
                   pivot1.getScratches().add(scratch);
                   returnPivotList.add(pivot1);
               }

            }
        }
        return returnPivotList;
    }
}
