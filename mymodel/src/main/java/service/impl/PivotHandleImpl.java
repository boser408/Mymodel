package service.impl;

import com.myproject.mymodel.domain.Dpattern;
import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Pivot;
import com.myproject.mymodel.domain.Scratch;
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
    public Dpattern findDpattern(Pivot pivot) {
        List<Scratch> scratchList=new ArrayList<>();
        //System.out.println("pivot in handling is:"+pivot.toString());
        for(int n=0;n<pivot.getScratches().size()-1;n++){
            for(int i=n+1;i<pivot.getScratches().size();i++){
                double factor=(double) pivot.getScratches().get(i).getLength()/pivot.getScratches().get(n).getLength();
                if(pivot.getPivotType()==1){
                    if((factor>controlFactor && factor<1/controlFactor) && pivot.getScratches().get(i).getLow()>pivot.getScratches().get(n).getHigh()){
                        /*System.out.println("Dpattern Found in"+pivot.toString());
                        System.out.println("the 1st Scratch is"+pivot.getScratches().get(n).toString());
                        System.out.println("the 2nd Scratch is"+pivot.getScratches().get(i).toString());*/
                        scratchList.add(pivot.getScratches().get(n));
                        scratchList.add(pivot.getScratches().get(i));

                    }
                }else if(pivot.getPivotType()==-1){
                  if((factor>controlFactor && factor<1/controlFactor) && pivot.getScratches().get(n).getLow()>pivot.getScratches().get(i).getHigh()){
                        /*System.out.println("Dpattern Found in"+pivot.toString());
                        System.out.println("the 1st Scratch is"+pivot.getScratches().get(n).toString());
                        System.out.println("the 2nd Scratch is"+pivot.getScratches().get(i).toString());*/
                        scratchList.add(pivot.getScratches().get(n));
                        scratchList.add(pivot.getScratches().get(i));

                    }
                }
            }

        }
            Dpattern foundDpattern=new Dpattern(pivot,scratchList);
            return foundDpattern;
    }

    @Override
    public Pivot dwsubpivotHandle(List<Pivot> cleanedPivotList, Pivot subpivot, int n, int endNumberofsubpivot) {
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

                        Scratch scratch=new Scratch(tmpPivot); // 2. Merge the tmpPivot into subpivot;
                        subpivot.getScratches().add(scratch);
                        if(numberofTmpLow<n){ // 3. Handle the low point of tmpPivot and merge the left pivots into subpivot
                            for(int t=numberofTmpLow+2;t<=n;t=t+2){
                                Scratch scratch2=new Scratch(cleanedPivotList.get(t));
                                subpivot.getScratches().add(scratch2);
                            }
                        }

        }else { // Only 1 pivot between the beginning subpivot and ending subpivot;
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
                      subpivot=cleanPivot(subpivot);
                    }

        Pivot returnpivot=new Pivot(subpivot);
        return returnpivot;
    }

    @Override
    public Pivot subpivotHandle(List<Pivot> cleanedPivotList, Pivot subpivot, int n, int endNumberofsubpivot) {

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

            Scratch scratch=new Scratch(tmpPivot); // 2. Merge the tmpPivot into subpivot;
            subpivot.getScratches().add(scratch);
            if(numberofTmpLow<n){ // 3. Handle the low point of tmpPivot and merge the left pivots into subpivot
                for(int t=numberofTmpLow+2;t<=n;t=t+2){
                    Scratch scratch2=new Scratch(cleanedPivotList.get(t));
                    subpivot.getScratches().add(scratch2);
                }
            }

        }else { // Only 1 pivot between the beginning subpivot and ending subpivot;
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

            subpivot=cleanPivot(subpivot);
        }
        Pivot returnpivot=new Pivot(subpivot);
        return returnpivot;
    }

    @Override
    public List<Scratch> findScratches(List<HighLowPrice> highLowPrices, int startindex, int length, int pivotLength) {

        List<Scratch> scratches = new ArrayList<>();
        Scratch upscratch=new Scratch(1,startindex,highLowPrices.get(startindex-1).getHigh(),highLowPrices.get(startindex-1).getLow(),0);
        Scratch dwscratch=new Scratch(1,startindex,highLowPrices.get(startindex-1).getHigh(),highLowPrices.get(startindex-1).getLow(),0);

        for(int n=startindex;n<startindex+length-1;n++){
            if (upscratch.getStatus()==1) { //Scenario 1: A formed up trend scratch exists;

                if(highLowPrices.get(n).getHigh()>=upscratch.getHigh()
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
                if(highLowPrices.get(n).getLow()<=dwscratch.getLow()&&highLowPrices.get(n).getHigh()<=dwscratch.getHigh()){
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
                       /* pivots.add(dwpivot);
                        break;*/
                    }
                    System.out.println("11--pivot="+pivot.toString());
                    System.out.println("11--dwpivot="+dwpivot.toString());
                    pivots.add(dwpivot);
                    break;

                }else if(dwpivot.getScratches().size()>0 && scratches.get(n).getLow()<=dwpivot.getLow()) {

                    pivot.setLength(dwpivot.getStartId()-pivot.getStartId()+1);
                    pivots.add(pivot);

                    dwpivot.setLength(scratches.get(n).getStartId()-dwpivot.getStartId()+scratches.get(n).getLength());
                    dwpivot.setLow(scratches.get(n).getLow());
                    dwpivot.setPivotType(-1);
                    pivots.add(dwpivot);
                    System.out.println("22--pivot="+pivot.toString());
                    System.out.println("22--dwpivot="+dwpivot.toString());
                    break;
                }else if(scratches.get(n+1).getHigh()>=pivot.getHigh()) {

                    pivot.setLength(scratches.get(n+1).getStartId()-pivot.getStartId()+scratches.get(n+1).getLength());
                    pivot.setHigh(scratches.get(n+1).getHigh());
                    if(scratches.get(n).getStatus()==-1){
                        pivot.getScratches().add(scratches.get(n));
                    }
                    if(scratchList.size()>0){
                        pivot.getScratches().addAll(scratchList);
                        scratchList.clear();
                    }
                    dwpivot=new Pivot(scratches.get(n+2));
                    System.out.println("33--pivot="+pivot.toString());
                    System.out.println("33--dwpivot="+dwpivot.toString());
                }else {

                        if (scratches.get(n).getStatus()==-1 ){
                            if(n>nbegin && scratches.get(n-1).getLow()<scratches.get(n).getLow()
                            && scratches.get(n+1).getHigh()>scratches.get(n).getHigh()){
                                dwpivot.getScratches().clear();
                                pivots.add(pivot);
                                pivots.add(dwpivot);
                                n=n-2;
                                System.out.println("00--pivot="+pivot.toString());
                                System.out.println("00--dwpivot="+dwpivot.toString());
                                break;
                            }else {
                                scratchList.add(scratches.get(n));
                            }
                        }
                        if(scratches.get(n+1).getStatus()==1){
                            dwpivot.getScratches().add(scratches.get(n+1));
                        }
                    System.out.println("44--pivot="+pivot.toString());
                    System.out.println("44--dwpivot="+dwpivot.toString());
                    System.out.println("44--scratchList="+scratchList.toString());
                }

            }else if(pivot.getPivotType()==-1){

                if(scratches.get(n).getHigh()>pivot.getHigh()){

                    pivot.setLength(uppivot.getStartId()-pivot.getStartId()+1);
                    pivots.add(pivot);

                    if(uppivot.getScratches().size()>0){

                        uppivot.setLength(scratches.get(n).getStartId()-uppivot.getStartId()+scratches.get(n).getLength());
                        uppivot.setHigh(scratches.get(n).getHigh());
                        uppivot.setPivotType(1);
                        /*pivots.add(uppivot);
                        break;*/
                    }
                    System.out.println("55--pivot="+pivot.toString());
                    System.out.println("55--uppivot="+uppivot.toString());
                    pivots.add(uppivot);
                    break;

                }else if(uppivot.getScratches().size()>0 && scratches.get(n).getHigh()>=uppivot.getHigh()){

                    pivot.setLength(uppivot.getStartId()-pivot.getStartId()+1);
                    pivots.add(pivot);

                    uppivot.setLength(scratches.get(n).getStartId()-uppivot.getStartId()+scratches.get(n).getLength());
                    uppivot.setHigh(scratches.get(n).getHigh());
                    uppivot.setPivotType(1);
                    pivots.add(uppivot);
                    System.out.println("66--pivot="+pivot.toString());
                    System.out.println("66--uppivot="+uppivot.toString());
                    break;
                }else if(scratches.get(n+1).getLow()<=pivot.getLow()) {

                    pivot.setLength(scratches.get(n+1).getLength()+scratches.get(n+1).getStartId()-pivot.getStartId());
                    pivot.setLow(scratches.get(n+1).getLow());
                    if(scratches.get(n).getStatus()==1){
                        pivot.getScratches().add(scratches.get(n));
                    }
                    if(scratchList.size()>0){
                       pivot.getScratches().addAll(scratchList);
                       scratchList.clear();
                    }
                    uppivot=new Pivot(scratches.get(n+2));
                    System.out.println("77--pivot="+pivot.toString());
                    System.out.println("77--uppivot="+uppivot.toString());

                }else {
                       if(scratches.get(n).getStatus()==1 ){
                           if(n>nbegin&&scratches.get(n-1).getHigh()>scratches.get(n).getHigh()
                                   &&scratches.get(n+1).getLow()<scratches.get(n).getLow()){
                               uppivot.getScratches().clear();
                               pivots.add(pivot);
                               pivots.add(uppivot);
                               n=n-2;
                               System.out.println("99--pivot="+pivot.toString());
                               System.out.println("99--uppivot="+uppivot.toString());
                               break;
                           }else{
                               scratchList.add(scratches.get(n));
                           }
                       }
                       if(scratches.get(n+1).getStatus()==-1){
                           uppivot.getScratches().add(scratches.get(n+1));
                       }
                    System.out.println("88--pivot="+pivot.toString());
                    System.out.println("88--uppivot="+uppivot.toString());
                    System.out.println("88--scratchList="+scratchList.toString());
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
          Dpattern dpattern=new Dpattern();
           if(pivot.getScratches().size()>1){
              // System.out.println("pivot send to Search="+pivot.toString());
               dpattern=findDpattern(pivot);
              // System.out.println("dpattern received="+dpattern.toString());
               if(dpattern.getFeatureScratches().size()>=2){
                   doublePivotPatternList.add(dpattern);
               }
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
        int endNumberofsubpivot=n+1;
        while (n<cleanedPivotList.size()-3){

            if(mainpivot.getPivotType()>0){ // Uptrend;
                if(cleanedPivotList.get(n+1).getLow()<mainpivot.getLow()){ //Scenario 1
                    System.out.println("Entering------11");
                    System.out.println("subpivot before handle="+subpivot.toString());
                    System.out.println("mainpivot before handle="+mainpivot.toString());
                    magaPivotList.add(mainpivot);
                    if(subpivot.getPivotType()<=-5){
                        subpivot=dwsubpivotHandle(cleanedPivotList,subpivot,n,endNumberofsubpivot);
                        mainpivot=new Pivot(subpivot);
                    }else {
                        mainpivot=new Pivot(cleanedPivotList.get(n+1));
                    }
                    subpivot=new Pivot(cleanedPivotList.get(n+2));
                    endNumberofsubpivot=n+2;

                }else if(subpivot.getPivotType()<=-5 && cleanedPivotList.get(n+1).getLow()<subpivot.getLow()){//Scenario 2

                    subpivot=dwsubpivotHandle(cleanedPivotList,subpivot,n,endNumberofsubpivot);
                    endNumberofsubpivot=n+1;
                }else if(cleanedPivotList.get(n+2).getHigh()>mainpivot.getHigh()) {//Scenario 3
                    System.out.println("Entering---13");
                    System.out.println("uppivots="+uppivots.toString());
                    System.out.println("dwpivots="+dwpivots.toString());
                    System.out.println("mainpivot before handle="+mainpivot.toString());
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
                    System.out.println("mainpivot after handle="+mainpivot.toString());
                    Dpattern returndpattern=findDpattern(mainpivot);
                    if(returndpattern.getFeatureScratches().size()>=2){
                        System.out.println("returndpattern---13:"+returndpattern.toString());
                        finalDpatternList.add(returndpattern);
                    }
                    if(mainpivot.getScratches().size()>1){
                        mainpivot=cleanPivot(mainpivot);
                    }
                    if(n<cleanedPivotList.size()-4){
                        subpivot=new Pivot(cleanedPivotList.get(n+3));
                        endNumberofsubpivot=n+3;
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
                    System.out.println("Entering------21");
                    System.out.println("subpivot before handle="+subpivot.toString());
                    System.out.println("mainpivot before handle="+mainpivot.toString());
                    magaPivotList.add(mainpivot);
                    if(subpivot.getPivotType()>=5){
                        subpivot=subpivotHandle(cleanedPivotList,subpivot,n,endNumberofsubpivot);
                        mainpivot=new Pivot(subpivot);
                    }else {
                        mainpivot=new Pivot(cleanedPivotList.get(n+1));
                    }
                    System.out.println("subpivot after handle="+subpivot.toString());
                    System.out.println("mainpivot after handle="+mainpivot.toString());
                    subpivot=new Pivot(cleanedPivotList.get(n+2));
                    endNumberofsubpivot=n+2;

                }else if(subpivot.getPivotType()>=5 && cleanedPivotList.get(n+1).getHigh()>subpivot.getHigh()){//2
                    System.out.println("Entering------22");
                    System.out.println("subpivot before handle="+subpivot.toString());
                    System.out.println("mainpivot before handle="+mainpivot.toString());
                    subpivot=subpivotHandle(cleanedPivotList,subpivot,n,endNumberofsubpivot);

                    System.out.println("subpivot after handle="+subpivot.toString());
                    System.out.println("mainpivot after handle="+mainpivot.toString());
                    endNumberofsubpivot=n+1;
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

                    Dpattern returndpattern=findDpattern(mainpivot);
                    if(returndpattern.getFeatureScratches().size()>=2){
                        System.out.println("returndpattern---23:"+returndpattern.toString());
                        finalDpatternList.add(returndpattern);
                    }
                    if(mainpivot.getScratches().size()>1){
                        mainpivot=cleanPivot(mainpivot);
                    }
                    if(n<cleanedPivotList.size()-4){
                        subpivot=new Pivot(cleanedPivotList.get(n+3));
                        endNumberofsubpivot=n+3;
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
