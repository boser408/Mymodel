package service.impl;

import com.myproject.mymodel.domain.*;
import service.PivotHandle;

import java.util.ArrayList;
import java.util.List;

public class PivotHandleImpl implements PivotHandle {
    public static final double controlFactor=0.7;
    public int numberofLoop;
    public int getNumberofLoop() {
        return numberofLoop;
    }

    @Override
    public Pivot cleanPivot(Pivot pivot) {
        for(int n=pivot.getScratches().size()-1;n>0;n--){
            for(int i=n-1;i>-1;i--){
                if(pivot.getScratches().get(n).getLength()>pivot.getScratches().get(i).getLength()){
                    pivot.getScratches().remove(i);
                    n=n-1;
                }
            }
        }
        return pivot;
    }
    @Override
    public Dpattern findDpattern(Pivot pivot, Dpattern dpattern) {
        List<Scratch> scratchList=new ArrayList<>();
        Dpattern foundDpattern=new Dpattern(pivot,scratchList);
        for(int n=0;n<pivot.getScratches().size();n++){
            for(int i=n+1;i<pivot.getScratches().size();i++){
                if(pivot.getPivotType()==1){
                    if((pivot.getScratches().get(i).getLength()/pivot.getScratches().get(n).getLength()>controlFactor &&
                            pivot.getScratches().get(i).getLength()/pivot.getScratches().get(n).getLength()<1/controlFactor) &&
                            pivot.getScratches().get(i).getLow()>pivot.getScratches().get(n).getHigh()){

                        foundDpattern.getFeatureScratches().add(pivot.getScratches().get(n));
                        foundDpattern.getFeatureScratches().add(pivot.getScratches().get(i));

                    }
                }else if(pivot.getPivotType()==-1){
                    if((pivot.getScratches().get(i).getLength()/pivot.getScratches().get(n).getLength()>controlFactor &&
                            pivot.getScratches().get(i).getLength()/pivot.getScratches().get(n).getLength()<1/controlFactor) &&
                            pivot.getScratches().get(n).getLow()>pivot.getScratches().get(i).getHigh()){

                        foundDpattern.getFeatureScratches().add(pivot.getScratches().get(n));
                        foundDpattern.getFeatureScratches().add(pivot.getScratches().get(i));

                    }
                }
            }

        }
        return foundDpattern;
    }

    @Override
    public Pivot tempPivotClean(Pivot subpivot, Pivot currentpivot, List<Pivot> uppivots, List<Pivot> dwpivots) {
        Pivot temppivot=new Pivot();
        if(subpivot.getPivotType()>0){
           int endpivotId=dwpivots.get(0).getStartId();
           temppivot=new Pivot(dwpivots.get(0));
           for(Pivot pivot:dwpivots){
                if(pivot.getLow()<temppivot.getLow()){
                    temppivot.setLow(pivot.getLow());
                    temppivot.setLength(pivot.getStartId()-temppivot.getStartId()+pivot.getLength());
                    endpivotId=pivot.getStartId();
                }
           }
           for(Pivot pivot:dwpivots){
               if (pivot.getStartId()>temppivot.getStartId() && pivot.getStartId()<=endpivotId){
                   temppivot.getScratches().addAll(pivot.getScratches());
               }
           }
           for(Pivot pivot:uppivots){
               if (pivot.getStartId()<endpivotId){
                   Scratch scratch=new Scratch(pivot);
                   temppivot.getScratches().add(scratch);
               }
           }

        }else {
            int endpivotId=uppivots.get(0).getStartId();
            temppivot=new Pivot(uppivots.get(0));
            for(Pivot pivot:uppivots){
                if(pivot.getHigh()>temppivot.getHigh()){
                    temppivot.setHigh(pivot.getHigh());
                    temppivot.setLength(pivot.getStartId()-temppivot.getStartId()+pivot.getLength());
                    endpivotId=pivot.getStartId();
                }
            }
            for(Pivot pivot:uppivots){
                if (pivot.getStartId()>temppivot.getStartId() && pivot.getStartId()<=endpivotId){
                    temppivot.getScratches().addAll(pivot.getScratches());
                }
            }
            for(Pivot pivot:dwpivots){
                if (pivot.getStartId()<endpivotId){
                    Scratch scratch=new Scratch(pivot);
                    temppivot.getScratches().add(scratch);
                }
            }
        }

        return temppivot;
    }

    @Override
    public List<Scratch> findScratches(List<HighLowPrice> highLowPrices, int startindex, int length, int pivotLength) {

        List<Scratch> scratches = new ArrayList<>();
        Scratch upscratch=new Scratch(1,startindex,highLowPrices.get(startindex-1).getHigh(),highLowPrices.get(startindex-1).getLow(),0);
        Scratch dwscratch=new Scratch(1,startindex,highLowPrices.get(startindex-1).getHigh(),highLowPrices.get(startindex-1).getLow(),0);

        for(int n=startindex;n<startindex+length-1;n++){
            if (upscratch.getStatus()==1) { //Scenario 1: A formed up trend scratch exists;

                if(highLowPrices.get(n).getHigh()>upscratch.getHigh()
                        && highLowPrices.get(n).getLow()>=upscratch.getLow() ){

                    upscratch.setLength(upscratch.getLength()+1);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());

                    dwscratch.setLength(1);
                    dwscratch.setStartId(n+1);
                    dwscratch.setHigh(highLowPrices.get(n).getHigh());
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    dwscratch.setStatus(0);

                }else if(highLowPrices.get(n).getHigh() >upscratch.getHigh()
                        && highLowPrices.get(n).getLow()<upscratch.getLow()){

                    if(highLowPrices.get(n).getOpen()>highLowPrices.get(n).getClose()){
                        upscratch.setLength(upscratch.getLength()+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        Scratch scratch=new Scratch(upscratch.getLength(),upscratch.getStartId(),upscratch.getHigh(),upscratch.getLow(),upscratch.getStatus());
                        scratches.add(scratch);
                        //scratchMapper.save(upscratch);

                        upscratch.setLength(1);
                        upscratch.setStartId(n+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLow(highLowPrices.get(n).getLow());
                        upscratch.setStatus(0);

                        dwscratch.setLength(1);
                        dwscratch.setStartId(n+1);
                        dwscratch.setHigh(highLowPrices.get(n).getHigh());
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        dwscratch.setStatus(0);


                    }else {
                        dwscratch.setLength(dwscratch.getLength()+1);
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        if(n-dwscratch.getStartId()>=pivotLength-2){ // A down trend scratch formed
                            dwscratch.setStatus(-1);
                        }

                        Scratch dscratch=new Scratch(dwscratch); // amended because new constructor is added;
                        scratches.add(dscratch);
                        //scratchMapper.save(dwscratch);

                        dwscratch.setLength(1);
                        dwscratch.setStartId(n+1);
                        dwscratch.setHigh(highLowPrices.get(n).getHigh());
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        dwscratch.setStatus(0);

                        Scratch scratch=new Scratch(upscratch);// amended because new constructor is added;
                        scratches.add(scratch);
                        //scratchMapper.save(upscratch);

                        upscratch.setLength(1);
                        upscratch.setStartId(n+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLow(highLowPrices.get(n).getLow());
                        upscratch.setStatus(0);

                    }

                }else if(highLowPrices.get(n).getHigh()<=dwscratch.getHigh()
                        && highLowPrices.get(n).getLow()<=dwscratch.getLow()
                        && n-dwscratch.getStartId()>=pivotLength-2){

                    dwscratch.setLength(dwscratch.getLength()+1);
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    dwscratch.setStatus(-1);

                    if(upscratch.getStartId()<dwscratch.getStartId()){
                        upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);

                        Scratch scratch=new Scratch(upscratch.getLength(),upscratch.getStartId(),upscratch.getHigh(),upscratch.getLow(),upscratch.getStatus());
                        scratches.add(scratch);
                        //scratchMapper.save(upscratch);

                    }
                    upscratch.setLength(1);
                    upscratch.setStartId(n+1);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    upscratch.setLow(highLowPrices.get(n).getLow());
                    upscratch.setStatus(0);
                }else {
                    if(highLowPrices.get(n).getLow()<upscratch.getLow()){
                        upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);
                        Scratch scratch=new Scratch(upscratch.getLength(),upscratch.getStartId(),upscratch.getHigh(),upscratch.getLow(),upscratch.getStatus());
                        scratches.add(scratch);

                        upscratch.setLength(1);
                        upscratch.setStartId(n+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLow(highLowPrices.get(n).getLow());
                        upscratch.setStatus(0);

                        dwscratch.setLength(dwscratch.getLength()+1);
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                    }else if(highLowPrices.get(n).getLow()<dwscratch.getLow()){
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        dwscratch.setLength(dwscratch.getLength()+1);
                        upscratch.setLength(upscratch.getLength()+1);
                    }else {
                        upscratch.setLength(upscratch.getLength()+1);
                        dwscratch.setLength(dwscratch.getLength()+1);
                    }
                }

            }else if (dwscratch.getStatus()==-1){ //Scenario 2: A formed down trend scratch exists;
                if(highLowPrices.get(n).getLow()<dwscratch.getLow()&&highLowPrices.get(n).getHigh()<=dwscratch.getHigh()){
                    dwscratch.setLength(dwscratch.getLength()+1);
                    dwscratch.setLow(highLowPrices.get(n).getLow());

                    upscratch.setLength(1);
                    upscratch.setStartId(n+1);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    upscratch.setLow(highLowPrices.get(n).getLow());
                    upscratch.setStatus(0);

                }else if(highLowPrices.get(n).getLow()<dwscratch.getLow()&&highLowPrices.get(n).getHigh()>dwscratch.getHigh()){

                    if(highLowPrices.get(n).getOpen()>highLowPrices.get(n).getClose()){
                        upscratch.setLength(upscratch.getLength()+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        if(n-upscratch.getStartId()>=pivotLength-2){ // A up trend scratch formed
                            upscratch.setStatus(1);
                        }

                        Scratch scratch=new Scratch(upscratch.getLength(),upscratch.getStartId(),upscratch.getHigh(),upscratch.getLow(),upscratch.getStatus());
                        scratches.add(scratch);
                        //scratchMapper.save(upscratch);
                        upscratch.setLength(1);
                        upscratch.setStartId(n+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLow(highLowPrices.get(n).getLow());
                        upscratch.setStatus(0);

                        Scratch dscratch=new Scratch(dwscratch.getLength(),dwscratch.getStartId(),dwscratch.getHigh(),dwscratch.getLow(),dwscratch.getStatus());
                        scratches.add(dscratch);
                        //scratchMapper.save(dwscratch);

                        dwscratch.setLength(1);
                        dwscratch.setStartId(n+1);
                        dwscratch.setHigh(highLowPrices.get(n).getHigh());
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        dwscratch.setStatus(0);

                    }else {
                        dwscratch.setLength(dwscratch.getLength()+1);
                        dwscratch.setLow(highLowPrices.get(n).getLow());

                        Scratch dscratch=new Scratch(dwscratch.getLength(),dwscratch.getStartId(),dwscratch.getHigh(),dwscratch.getLow(),dwscratch.getStatus());
                        scratches.add(dscratch);
                        //scratchMapper.save(dwscratch);

                        dwscratch.setLength(1);
                        dwscratch.setStartId(n+1);
                        dwscratch.setHigh(highLowPrices.get(n).getHigh());
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        dwscratch.setStatus(0);

                        upscratch.setLength(1);
                        upscratch.setStartId(n+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLow(highLowPrices.get(n).getLow());
                        upscratch.setStatus(0);

                    }

                }else if(highLowPrices.get(n).getHigh()>=upscratch.getHigh()
                        && highLowPrices.get(n).getLow()>=upscratch.getLow()
                        && n-upscratch.getStartId()>=pivotLength-2){

                    upscratch.setLength(upscratch.getLength()+1);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    upscratch.setStatus(1);
                    if(dwscratch.getStartId()<upscratch.getStartId()){
                        dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);

                        Scratch dscratch=new Scratch(dwscratch.getLength(),dwscratch.getStartId(),dwscratch.getHigh(),dwscratch.getLow(),dwscratch.getStatus());
                        scratches.add(dscratch);
                        //scratchMapper.save(dwscratch);

                    }

                    dwscratch.setLength(1);
                    dwscratch.setStartId(n+1);
                    dwscratch.setHigh(highLowPrices.get(n).getHigh());
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    dwscratch.setStatus(0);

                }else {
                    if(highLowPrices.get(n).getHigh()>dwscratch.getHigh()){
                        dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);
                        Scratch dscratch=new Scratch(dwscratch.getLength(),dwscratch.getStartId(),dwscratch.getHigh(),dwscratch.getLow(),dwscratch.getStatus());
                        scratches.add(dscratch);

                        dwscratch.setLength(1);
                        dwscratch.setStartId(n+1);
                        dwscratch.setHigh(highLowPrices.get(n).getHigh());
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        dwscratch.setStatus(0);

                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLength(upscratch.getLength()+1);
                    }else if(highLowPrices.get(n).getHigh()>upscratch.getHigh()){
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLength(upscratch.getLength()+1);
                        dwscratch.setLength(dwscratch.getLength()+1);
                    }else {
                        upscratch.setLength(upscratch.getLength()+1);
                        dwscratch.setLength(dwscratch.getLength()+1);
                    }

                }

            }else {     // Scenario 3:  no direction
                if(highLowPrices.get(n).getHigh()>upscratch.getHigh() && highLowPrices.get(n).getHigh()>dwscratch.getHigh()
                        && highLowPrices.get(n).getLow()>=upscratch.getLow()){
                    upscratch.setLength(upscratch.getLength()+1);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());

                    if(dwscratch.getStartId()<upscratch.getStartId()){
                        dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);

                        Scratch dscratch=new Scratch(dwscratch);//amended because new constructor is added;
                        scratches.add(dscratch);
                        // scratchMapper.save(dwscratch);

                    }
                    dwscratch.setLength(1);
                    dwscratch.setStartId(n+1);
                    dwscratch.setHigh(highLowPrices.get(n).getHigh());
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    dwscratch.setStatus(0);

                    if(n-upscratch.getStartId()>=pivotLength-2){ // An up trend scratch formed
                        upscratch.setStatus(1);
                    }
                }else if(highLowPrices.get(n).getLow()<dwscratch.getLow() && highLowPrices.get(n).getLow()< upscratch.getLow()
                        && highLowPrices.get(n).getHigh()<=dwscratch.getHigh()){
                    dwscratch.setLength(dwscratch.getLength()+1);
                    dwscratch.setLow(highLowPrices.get(n).getLow());

                    if(upscratch.getStartId()<dwscratch.getStartId()){
                        upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);

                        Scratch scratch=new Scratch(upscratch);//amended because new constructor is added;
                        scratches.add(scratch);
                        //scratchMapper.save(upscratch);

                    }
                    upscratch.setLength(1);
                    upscratch.setStartId(n+1);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    upscratch.setLow(highLowPrices.get(n).getLow());
                    upscratch.setStatus(0);

                    if(n-dwscratch.getStartId()>=pivotLength-2){ // A down trend scratch formed
                        dwscratch.setStatus(-1);
                    }

                }else if(highLowPrices.get(n).getHigh()>=upscratch.getHigh() && n-upscratch.getStartId()>=pivotLength-2){
                    upscratch.setLength(upscratch.getLength()+1);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    upscratch.setStatus(1);
                    if(dwscratch.getStartId()<upscratch.getStartId()){
                        dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);

                        Scratch dscratch=new Scratch(dwscratch.getLength(),dwscratch.getStartId(),dwscratch.getHigh(),dwscratch.getLow(),dwscratch.getStatus());
                        scratches.add(dscratch);
                        //scratchMapper.save(dwscratch);

                    }

                    dwscratch.setLength(1);
                    dwscratch.setStartId(n+1);
                    dwscratch.setHigh(highLowPrices.get(n).getHigh());
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    dwscratch.setStatus(0);
                }else if(highLowPrices.get(n).getLow()<dwscratch.getLow() && n-dwscratch.getStartId()>=pivotLength-2){
                    dwscratch.setLength(dwscratch.getLength()+1);
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    dwscratch.setStatus(-1);

                    if(upscratch.getStartId()<dwscratch.getStartId()){
                        upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);

                        Scratch scratch=new Scratch(upscratch.getLength(),upscratch.getStartId(),upscratch.getHigh(),upscratch.getLow(),upscratch.getStatus());
                        scratches.add(scratch);
                        //scratchMapper.save(upscratch);

                    }
                    upscratch.setLength(1);
                    upscratch.setStartId(n+1);
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    upscratch.setLow(highLowPrices.get(n).getLow());
                    upscratch.setStatus(0);
                }else if(highLowPrices.get(n).getHigh()>upscratch.getHigh() && highLowPrices.get(n).getHigh()>dwscratch.getHigh()
                        && highLowPrices.get(n).getLow()<upscratch.getLow() && highLowPrices.get(n).getLow()<dwscratch.getLow()){
                    if(highLowPrices.get(n).getOpen()>highLowPrices.get(n).getClose()){
                        upscratch.setLength(upscratch.getLength()+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        if(n-upscratch.getStartId()>=pivotLength-2){ // An up trend scratch formed
                            upscratch.setStatus(1);
                        }

                        Scratch scratch=new Scratch(upscratch.getLength(),upscratch.getStartId(),upscratch.getHigh(),upscratch.getLow(),upscratch.getStatus());
                        scratches.add(scratch);
                        // scratchMapper.save(upscratch);


                        upscratch.setLength(1);
                        upscratch.setStartId(n+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLow(highLowPrices.get(n).getLow());
                        upscratch.setStatus(0);

                        if(dwscratch.getStartId()<=upscratch.getStartId()){
                            dwscratch.setLength(upscratch.getStartId()-dwscratch.getStartId()+1);

                            Scratch dscratch=new Scratch(dwscratch.getLength(),dwscratch.getStartId(),dwscratch.getHigh(),dwscratch.getLow(),dwscratch.getStatus());
                            scratches.add(dscratch);
                            //scratchMapper.save(dwscratch);


                        }
                        dwscratch.setLength(1);
                        dwscratch.setStartId(n+1);
                        dwscratch.setHigh(highLowPrices.get(n).getHigh());
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        dwscratch.setStatus(0);


                    }else {
                        dwscratch.setLength(dwscratch.getLength()+1);
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        if(n-dwscratch.getStartId()>=pivotLength-2){ // A down trend scratch formed
                            dwscratch.setStatus(-1);
                        }

                        Scratch dscratch=new Scratch(dwscratch.getLength(),dwscratch.getStartId(),dwscratch.getHigh(),dwscratch.getLow(),dwscratch.getStatus());
                        scratches.add(dscratch);
                        //scratchMapper.save(dwscratch);

                        dwscratch.setLength(1);
                        dwscratch.setStartId(n+1);
                        dwscratch.setHigh(highLowPrices.get(n).getHigh());
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        dwscratch.setStatus(0);
                        if(upscratch.getStartId()<dwscratch.getStartId()){
                            upscratch.setLength(dwscratch.getStartId()-upscratch.getStartId()+1);

                            Scratch scratch=new Scratch(upscratch.getLength(),upscratch.getStartId(),upscratch.getHigh(),upscratch.getLow(),upscratch.getStatus());
                            scratches.add(scratch);
                            //scratchMapper.save(upscratch);

                        }
                        upscratch.setLength(1);
                        upscratch.setStartId(n+1);
                        upscratch.setHigh(highLowPrices.get(n).getHigh());
                        upscratch.setLow(highLowPrices.get(n).getLow());
                        upscratch.setStatus(0);

                    }

                }else if(highLowPrices.get(n).getHigh()>upscratch.getHigh()){
                    upscratch.setHigh(highLowPrices.get(n).getHigh());
                    upscratch.setLength(upscratch.getLength()+1);
                    dwscratch.setLength(dwscratch.getLength()+1);

                }else if(highLowPrices.get(n).getLow()<dwscratch.getLow()){
                    dwscratch.setLow(highLowPrices.get(n).getLow());
                    dwscratch.setLength(dwscratch.getLength()+1);
                    upscratch.setLength(upscratch.getLength()+1);


                }else {
                    upscratch.setLength(upscratch.getLength()+1);
                    dwscratch.setLength(dwscratch.getLength()+1);
                }

            }

        }
        scratches.add(upscratch);
        scratches.add(dwscratch);
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
        List<Pivot> pivots=new ArrayList<>();
        List<Scratch> scratchList=new ArrayList<>();
        Pivot uppivot=new Pivot();
        Pivot dwpivot=new Pivot();
        if(pivot.getPivotType()==1){
            dwpivot.setLength(scratches.get(n).getLength());
            dwpivot.setStartId(scratches.get(n).getStartId());
            dwpivot.setHigh(scratches.get(n).getHigh());
            dwpivot.setLow(scratches.get(n).getLow());
            dwpivot.setPivotType(0);
            dwpivot.setScratches(scratchList);
        }else {
            uppivot.setLength(scratches.get(n).getLength());
            uppivot.setStartId(scratches.get(n).getStartId());
            uppivot.setHigh(scratches.get(n).getHigh());
            uppivot.setLow(scratches.get(n).getLow());
            uppivot.setPivotType(0);
            uppivot.setScratches(scratchList);
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
                        pivots.add(dwpivot);
                        break;
                    }

                    pivots.add(dwpivot);
                    break;

                }else if(dwpivot.getScratches().size()>0 && scratches.get(n).getLow()<=dwpivot.getLow()) {

                    pivot.setLength(dwpivot.getStartId()-pivot.getStartId()+1);
                    pivots.add(pivot);

                    dwpivot.setLength(scratches.get(n).getStartId()-dwpivot.getStartId()+scratches.get(n).getLength());
                    dwpivot.setLow(scratches.get(n).getLow());
                    dwpivot.setPivotType(-1);
                    pivots.add(dwpivot);
                    break;
                }else if(scratches.get(n+1).getHigh()>=pivot.getHigh()) {

                    pivot.setLength(scratches.get(n+1).getStartId()-pivot.getStartId()+scratches.get(n+1).getLength());
                    pivot.setHigh(scratches.get(n+1).getHigh());
                    if(scratches.get(n).getStatus()==-1){
                        pivot.getScratches().add(scratches.get(n));
                    }

                    dwpivot.setLength(scratches.get(n+2).getLength());
                    dwpivot.setStartId(scratches.get(n+2).getStartId());
                    dwpivot.setHigh(scratches.get(n+2).getHigh());
                    dwpivot.setLow(scratches.get(n+2).getLow());
                    dwpivot.setPivotType(0);
                    dwpivot.setScratches(scratchList);

                }else {

                        if(scratches.get(n+1).getStatus()==1){

                            dwpivot.getScratches().add(scratches.get(n+1));
                        }
                }

            }else if(pivot.getPivotType()==-1){

                if(scratches.get(n).getHigh()>pivot.getHigh()){

                    pivot.setLength(uppivot.getStartId()-pivot.getStartId()+1);
                    pivots.add(pivot);

                    if(uppivot.getScratches().size()>0){

                        uppivot.setLength(scratches.get(n).getStartId()-uppivot.getStartId()+scratches.get(n).getLength());
                        uppivot.setHigh(scratches.get(n).getHigh());
                        uppivot.setPivotType(1);
                        pivots.add(uppivot);
                        break;
                    }

                    pivots.add(uppivot);
                    break;

                }else if(uppivot.getScratches().size()>0 && scratches.get(n).getHigh()>=uppivot.getHigh()){

                    pivot.setLength(uppivot.getStartId()-pivot.getStartId()+1);
                    pivots.add(pivot);

                    uppivot.setLength(scratches.get(n).getStartId()-uppivot.getStartId()+scratches.get(n).getLength());
                    uppivot.setHigh(scratches.get(n).getHigh());
                    uppivot.setPivotType(1);
                    pivots.add(uppivot);
                    break;
                }else if(scratches.get(n+1).getLow()<=pivot.getLow()) {

                    pivot.setLength(scratches.get(n+1).getLength()+scratches.get(n+1).getStartId()-pivot.getStartId());
                    pivot.setLow(scratches.get(n+1).getLow());
                    if(scratches.get(n).getStatus()==1){
                        pivot.getScratches().add(scratches.get(n));
                    }

                    uppivot.setLength(scratches.get(n+2).getLength());
                    uppivot.setStartId(scratches.get(n+2).getStartId());
                    uppivot.setHigh(scratches.get(n+2).getHigh());
                    uppivot.setLow(scratches.get(n+2).getLow());
                    uppivot.setPivotType(0);
                    uppivot.setScratches(scratchList);
                }else {

                       if(scratches.get(n+1).getStatus()==-1){
                           uppivot.getScratches().add(scratches.get(n+1));
                       }
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
    public List<Pivot> scratchClean(List<Pivot> basicPivotList) {
        List<Pivot> cleanedPivotList=new ArrayList<>();
        for(Pivot pivot: basicPivotList){

            if(pivot.getScratches().size()<=1){
                Pivot cleanedPivot=new Pivot(pivot);
                cleanedPivotList.add(cleanedPivot);
            }else {

                Pivot cleanedPivot=cleanPivot(pivot);
                cleanedPivotList.add(cleanedPivot);
            }

        }
        return cleanedPivotList;
    }
    @Override
    public List<Dpattern> findDPatterninPivots(List<Pivot> basicPivotList) {
        List<Dpattern> doublePivotPatternList=new ArrayList<>();
        for(Pivot pivot: basicPivotList){
            List<Scratch> featureScratches=new ArrayList<>();
            Dpattern dpattern=new Dpattern(pivot,featureScratches);
           if(pivot.getScratches().size()>1){
               dpattern=findDpattern(pivot,dpattern);
           }
           if(dpattern.getFeatureScratches().size()>=2){
               doublePivotPatternList.add(dpattern);
           }

        }
        return doublePivotPatternList;
    }
    @Override
    public List<Dpattern> findallDpattern(List<Pivot> cleanedPivotList) {
        List<Dpattern> finalDpatternList=new ArrayList<>();
        List<Pivot> magaPivotList=new ArrayList<>();
        List<Pivot> uppivots=new ArrayList<>();
        List<Pivot> dwpivots=new ArrayList<>();
        int n=0;
        while (cleanedPivotList.get(n).getPivotType()==0){
            n++;
        }
        Pivot mainpivot=new Pivot(cleanedPivotList.get(n));
        Pivot subpivot=new Pivot(cleanedPivotList.get(n+1));
        while (n<cleanedPivotList.size()-3){

            if(mainpivot.getPivotType()>0){ // Uptrend;
                if(cleanedPivotList.get(n+1).getLow()<mainpivot.getLow()){ //Scenario 1
                    magaPivotList.add(mainpivot);

                    if(subpivot.getPivotType()>-5){
                        mainpivot=new Pivot(cleanedPivotList.get(n+1));
                        subpivot=new Pivot(cleanedPivotList.get(n+2));
                    }else if(subpivot.getPivotType()<=-5){
                        subpivot.setLength(cleanedPivotList.get(n+1).getStartId()-subpivot.getStartId()
                                +cleanedPivotList.get(n+1).getLength());
                        subpivot.setLow(cleanedPivotList.get(n+1).getLow());
                        dwpivots.clear();
                        for(Pivot pivot: uppivots){
                            Scratch scratch=new Scratch(pivot);
                            subpivot.getScratches().add(scratch);
                        }
                        for(Scratch scratch:cleanedPivotList.get(n+1).getScratches()){
                            subpivot.getScratches().add(scratch);
                        }
                        Dpattern dpattern=new Dpattern(subpivot,subpivot.getScratches());
                        Dpattern returndpattern=findDpattern(subpivot,dpattern);
                        if(returndpattern.getFeatureScratches().size()>=2){
                            System.out.println("returndpattern---11:"+returndpattern.toString());
                            finalDpatternList.add(returndpattern);
                        }

                        if(subpivot.getScratches().size()>1){
                            subpivot=cleanPivot(subpivot);
                        }
                        mainpivot=new Pivot(subpivot);
                        subpivot=new Pivot(cleanedPivotList.get(n+2));
                    }
                }else if(subpivot.getPivotType()<=-5 && cleanedPivotList.get(n+1).getLow()<subpivot.getLow()){//Scenario 2
                    if(uppivots.size()>=2){
                        Pivot temppivot=tempPivotClean(subpivot, cleanedPivotList.get(n+1), uppivots, dwpivots);
                        System.out.println("temppivot--12="+temppivot.toString());
                        break;
                    }

                    subpivot.setLength(cleanedPivotList.get(n+1).getStartId()-subpivot.getStartId()
                            +cleanedPivotList.get(n+1).getLength());
                    subpivot.setLow(cleanedPivotList.get(n+1).getLow());
                    dwpivots.clear();
                    for(Pivot pivot: uppivots){
                        Scratch scratch=new Scratch(pivot);
                        subpivot.getScratches().add(scratch);
                    }
                    for(Scratch scratch:cleanedPivotList.get(n+1).getScratches()){
                        subpivot.getScratches().add(scratch);
                    }
                    Dpattern dpattern=new Dpattern(subpivot,subpivot.getScratches());
                    Dpattern returndpattern=findDpattern(subpivot,dpattern);
                    if(returndpattern.getFeatureScratches().size()>=2){
                        System.out.println("returndpattern---12:"+returndpattern.toString());
                        finalDpatternList.add(returndpattern);
                    }

                    if(subpivot.getScratches().size()>1){
                        subpivot=cleanPivot(subpivot);
                    }
                }else if(cleanedPivotList.get(n+2).getHigh()>mainpivot.getHigh()) {//Scenario 3
                    if(subpivot.getPivotType()<-5){
                        magaPivotList.add(subpivot);

                    }
                    uppivots.clear();
                    mainpivot.setLength(cleanedPivotList.get(n+2).getStartId()-mainpivot.getStartId()
                    +cleanedPivotList.get(n+2).getLength());
                    mainpivot.setHigh(cleanedPivotList.get(n+2).getHigh());
                    mainpivot.setPivotType(6);
                    for(Pivot pivot:dwpivots){
                        Scratch scratch=new Scratch(pivot);
                        mainpivot.getScratches().add(scratch);
                    }
                    for(Scratch scratch:cleanedPivotList.get(n+2).getScratches()){
                        mainpivot.getScratches().add(scratch);
                    }
                    Dpattern dpattern=new Dpattern(mainpivot,mainpivot.getScratches());
                    Dpattern returndpattern=findDpattern(mainpivot,dpattern);
                    if(returndpattern.getFeatureScratches().size()>=2){
                        System.out.println("returndpattern---13:"+returndpattern.toString());
                        finalDpatternList.add(returndpattern);
                    }
                    if(mainpivot.getScratches().size()>1){
                        mainpivot=cleanPivot(mainpivot);
                    }
                    if(n<cleanedPivotList.size()-4){
                        subpivot=new Pivot(cleanedPivotList.get(n+3));
                    }
                }else {//Scenario 4
                    if(mainpivot.getPivotType()<5 && cleanedPivotList.get(n+1).getPivotType()==-1){
                        mainpivot.setPivotType(5);
                    }
                    if(subpivot.getPivotType()>-5 && cleanedPivotList.get(n+2).getPivotType()==1){
                        subpivot.setPivotType(-5);
                    }
                    if(cleanedPivotList.get(n+1).getPivotType()==-1){
                        dwpivots.add(cleanedPivotList.get(n+1));
                    }
                    if(cleanedPivotList.get(n+2).getPivotType()==1){
                        uppivots.add(cleanedPivotList.get(n+2));
                    }

                }
                if(mainpivot.getPivotType()>=0){
                    n=n+2;
                }else {
                    n=n+1;
                }
            }else if(mainpivot.getPivotType()<0){//Downtrend;
                if(cleanedPivotList.get(n+1).getHigh()>mainpivot.getHigh()){//1
                    magaPivotList.add(mainpivot);

                    if(subpivot.getPivotType()<5){
                        mainpivot=new Pivot(cleanedPivotList.get(n+1));
                        subpivot=new Pivot(cleanedPivotList.get(n+2));

                    }else if(subpivot.getPivotType()>=5){
                        subpivot.setLength(cleanedPivotList.get(n+1).getStartId()-subpivot.getStartId()
                                +cleanedPivotList.get(n+1).getLength());
                        subpivot.setHigh(cleanedPivotList.get(n+1).getHigh());
                        for(Pivot pivot: dwpivots){
                            Scratch scratch=new Scratch(pivot);
                            subpivot.getScratches().add(scratch);
                        }
                        for(Scratch scratch:cleanedPivotList.get(n+1).getScratches()){
                            subpivot.getScratches().add(scratch);
                        }
                        Dpattern dpattern=new Dpattern(subpivot,subpivot.getScratches());
                        Dpattern returndpattern=findDpattern(subpivot,dpattern);
                        if(returndpattern.getFeatureScratches().size()>=2){
                            System.out.println("returndpattern---21:"+returndpattern.toString());
                            finalDpatternList.add(returndpattern);
                        }
                        if(subpivot.getScratches().size()>1){
                            subpivot=cleanPivot(subpivot);
                        }

                        mainpivot=new Pivot(subpivot);
                        subpivot=new Pivot(cleanedPivotList.get(n+2));
                    }
                }else if(subpivot.getPivotType()>=5 && cleanedPivotList.get(n+1).getHigh()>subpivot.getHigh()){//2
                    if(dwpivots.size()>=2){
                        Pivot temppivot=tempPivotClean(subpivot, cleanedPivotList.get(n+1), uppivots, dwpivots);
                        System.out.println("temppivot--22="+temppivot.toString());
                        break;
                    }
                    subpivot.setLength(cleanedPivotList.get(n+1).getStartId()-subpivot.getStartId()
                            +cleanedPivotList.get(n+1).getLength());
                    subpivot.setHigh(cleanedPivotList.get(n+1).getHigh());
                    for(Pivot pivot: dwpivots){
                        Scratch scratch=new Scratch(pivot);
                        subpivot.getScratches().add(scratch);
                    }
                    for(Scratch scratch:cleanedPivotList.get(n+1).getScratches()){
                        subpivot.getScratches().add(scratch);
                    }
                    Dpattern dpattern=new Dpattern(subpivot,subpivot.getScratches());
                    Dpattern returndpattern=findDpattern(subpivot,dpattern);
                    if(returndpattern.getFeatureScratches().size()>=2){
                        System.out.println("returndpattern---22:"+returndpattern.toString());
                        finalDpatternList.add(returndpattern);
                    }
                    if(subpivot.getScratches().size()>1){
                        subpivot=cleanPivot(subpivot);
                    }
                }else if(cleanedPivotList.get(n+2).getLow()<mainpivot.getLow()) {//3
                    if(subpivot.getPivotType()>5){
                        magaPivotList.add(subpivot);

                    }
                    dwpivots.clear();
                    mainpivot.setLength(cleanedPivotList.get(n+2).getStartId()-mainpivot.getStartId()
                            +cleanedPivotList.get(n+2).getLength());
                    mainpivot.setLow(cleanedPivotList.get(n+2).getLow());
                    mainpivot.setPivotType(-6);
                    for(Pivot pivot:uppivots){
                        Scratch scratch=new Scratch(pivot);
                        mainpivot.getScratches().add(scratch);
                    }
                    for(Scratch scratch:cleanedPivotList.get(n+2).getScratches()){
                        mainpivot.getScratches().add(scratch);
                    }
                    Dpattern dpattern=new Dpattern(mainpivot,mainpivot.getScratches());
                    Dpattern returndpattern=findDpattern(mainpivot,dpattern);
                    if(returndpattern.getFeatureScratches().size()>=2){
                        System.out.println("returndpattern---23:"+returndpattern.toString());
                        finalDpatternList.add(returndpattern);
                    }
                    if(mainpivot.getScratches().size()>1){
                        mainpivot=cleanPivot(mainpivot);
                    }
                    if(n<cleanedPivotList.size()-4){
                        subpivot=new Pivot(cleanedPivotList.get(n+3));
                    }
                }else {//4
                    if(mainpivot.getPivotType()>-5 && cleanedPivotList.get(n+1).getPivotType()==1){
                        mainpivot.setPivotType(-5);
                    }
                    if(subpivot.getPivotType()<5 && cleanedPivotList.get(n+2).getPivotType()==-1){
                        subpivot.setPivotType(5);
                    }
                    if(cleanedPivotList.get(n+2).getPivotType()==-1){
                        dwpivots.add(cleanedPivotList.get(n+2));
                    }
                    if(cleanedPivotList.get(n+1).getPivotType()==1){
                        uppivots.add(cleanedPivotList.get(n+1));
                    }
                }
                if(mainpivot.getPivotType()<=0){
                    n=n+2;
                }else {
                    n=n+1;
                }
            }

        }
        System.out.println("mainpivot final:"+mainpivot.toString());
        System.out.println("subpivot:"+subpivot.toString());
        for(Pivot pivot:magaPivotList){
            System.out.println("pivot in magaPivotList:"+pivot.toString());
        }
        return finalDpatternList;
    }

}
