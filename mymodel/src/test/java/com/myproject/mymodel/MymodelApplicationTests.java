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
    void contextLoads() {

        List<HighLowPrice> highLowPrices = highLowPriceMapper.selectHighLow();
        float high=highLowPrices.get(0).getHigh();
        float low=highLowPrices.get(0).getLow();
        highLowPriceMapper.save(highLowPrices.get(0));
        int bottom = highLowPrices.size();

        for(int i=1;i<bottom;i++){
            float hi=highLowPrices.get(i).getHigh();
            float li=highLowPrices.get(i).getLow();

        /* if(hi>high && li>=low ){
                highLowPrices.get(i).setRelation(1);
            }else if(hi>high && li<low){
                highLowPrices.get(i).setRelation(2);
            }else if(hi<high && li>=low){
                highLowPrices.get(i).setRelation(3);
            }else {
                highLowPrices.get(i).setRelation(4);
            }

            if(scratch.getDirection()==0){
                if(hi>scratch.getHigh() && li>=scratch.getLow() ){
                    scratch.setDirection(1);
                    scratch.setLength(scratch.getLength()+1);
                    scratch.setEndId(i+1);
                    scratch.setHigh(hi);

                }else if(hi>scratch.getHigh() && li<scratch.getLow()){

                }else if(hi<scratch.getHigh() && li>=scratch.getLow()){

                }else {

                }
            }else if(scratch.getDirection()==1){

            }else {

            }*/

        }

        highLowPriceMapper.batchinsert(highLowPrices);

    }
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
    void findScratches(){

        List<HighLowPrice> highLowPrices = highLowPriceMapper.selectHighLow();
        List<Scratch> scratches = new ArrayList<>();
        Scratch upscratch=new Scratch(1,1,highLowPrices.get(0).getHigh(),highLowPrices.get(0).getLow(),0);
        Scratch dwscratch=new Scratch(1,1,highLowPrices.get(0).getHigh(),highLowPrices.get(0).getLow(),0);
        int pivotLength=6; // Definition of the length of shortest bar pivot;
        int bottom = highLowPrices.size()-1;

        for(int n=1;n<bottom;n++){
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

                        Scratch dscratch=new Scratch(dwscratch.getLength(),dwscratch.getStartId(),dwscratch.getHigh(),dwscratch.getLow(),dwscratch.getStatus());
                        scratches.add(dscratch);
                        //scratchMapper.save(dwscratch);

                        dwscratch.setLength(1);
                        dwscratch.setStartId(n+1);
                        dwscratch.setHigh(highLowPrices.get(n).getHigh());
                        dwscratch.setLow(highLowPrices.get(n).getLow());
                        dwscratch.setStatus(0);

                        Scratch scratch=new Scratch(upscratch.getLength(),upscratch.getStartId(),upscratch.getHigh(),upscratch.getLow(),upscratch.getStatus());
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

                        Scratch dscratch=new Scratch(dwscratch.getLength(),dwscratch.getStartId(),dwscratch.getHigh(),dwscratch.getLow(),dwscratch.getStatus());
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

                        Scratch scratch=new Scratch(upscratch.getLength(),upscratch.getStartId(),upscratch.getHigh(),upscratch.getLow(),upscratch.getStatus());
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
                    upscratch.setLength(upscratch.getLength()+1);
                    dwscratch.setLength(dwscratch.getLength()+1);

                }else {
                    upscratch.setLength(upscratch.getLength()+1);
                    dwscratch.setLength(dwscratch.getLength()+1);
                }

            }

        }
        scratches.add(upscratch);
        scratches.add(dwscratch);
        scratchMapper.batchinsert(scratches);
    }
    @Test
    void tryUseClass(){ // Create the table of "findscratch"
        List<HighLowPrice> highLowPrices = highLowPriceMapper.selectHighLow();
        PivotHandle pivotHandle=new PivotHandleImpl();
        List<Scratch> scratches = pivotHandle.findScratches(highLowPrices, 1, highLowPrices.size(), 6);
        System.out.println(scratches.size());
        scratchMapper.batchinsert(scratches);
    }
    @Test
    void findSmallScratch(){ // Create the table of "smallscratch"
        List<HighLowPrice> highLowPrices = highLowPriceMapper.selectHighLow();
        List<Scratch> list1=scratchMapper.selectSmallerByLength(12);
        List<Scratch> list=scratchMapper.selectByLength(12);

        for(Scratch scratch:list){
           List<Scratch> finalsmallscratches=new ArrayList<>();
            List<Scratch> scratchList=new ArrayList<Scratch>();
            PivotHandle pivotHandle=new PivotHandleImpl();
            List<Scratch> scratches = pivotHandle.findScratches(highLowPrices, scratch.getStartId(),
                    scratch.getLength(), 6);

            for(Scratch scratch1:scratches){
                if(scratch.getStatus()>0){
                    if(scratch.getStatus()*scratch1.getStatus()==-1 && scratch1.getLow()>scratch.getLow()){
                        scratchList.add(scratch1);
                    }
                }else {
                    if(scratch.getStatus()*scratch1.getStatus()==-1 && scratch1.getHigh()<scratch.getHigh()){
                        scratchList.add(scratch1);
                    }
                }

            }

            if(scratchList.isEmpty()){
                finalsmallscratches.add(scratch);
            }else {

                for(Scratch scratch1:scratches){
                    finalsmallscratches.add(scratch1);
                }
            }
            list1.addAll(finalsmallscratches);

        }
        Collections.sort(list1, Comparator.comparing(Scratch::getStartId));
        for(int n=1;n<list1.size();n++){
            if(list1.get(n-1).getStartId()==list1.get(n).getStartId()){
                if(list1.get(n-1).getLength()<=list1.get(n).getLength()){
                    list1.remove(n-1);
                }else {
                    list1.remove(n);
                }

            }
        }
        for(int n=0;n<list1.size()-1;n++){
            if(list1.get(n).getStatus()*list1.get(n+1).getStatus()==1){
                System.out.println("Check Data with Index ="+n);
                break;
            }
        }
        System.out.println("Data is Clean!");
        scratchMapper.batchsmallinsert(list1);
    }
    @Test
    void produceScratchTable(){ // find all the basic pivots combined by scratches
        List<Scratch> listofsmall=scratchMapper.selectAllsmall();
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
                /*System.out.println("pExtension: savpivot="+savepivot.toString());
                System.out.println("pExtension: currentpivot="+currentpivot.toString());
                System.out.println("pExtension: pivot of n="+listofsmall.get(n).toString());*/
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
                /*System.out.println("pFinding: savpivot="+savepivot.toString());
                System.out.println("pFinding: tmppivot="+tmppivot.toString());
                System.out.println("pFinding: currentpivot="+currentpivot.toString());*/
            }
        }
        for(Pivot pivot:pivotList){
            if(pivot.getPivotType()==0 && pivot.getScratches().size()>0){
                pivot.getScratches().clear();
            }
            //System.out.println(pivot.toString());
        }
        /*System.out.println("Size of pivotList is:"+pivotList.size());
        System.out.println("Sorted pivotList is:");*/
        for(Pivot pivot:pivotList){
            if(!pivot.getScratches().isEmpty()){
                pivot.getScratches().sort(Comparator.comparingInt(Scratch::getStartId));
            }
            //System.out.println(pivot.toString());
        }
       // List<Dpattern> doublePivotPatternList=pivotHandle.findDPatterninPivots(pivotList);

        /*for(Dpattern dpattern:doublePivotPatternList){
            System.out.println(dpattern);
        }*/
        /*List<Pivot> cleanedPivotList=pivotHandle.scratchClean(pivotList);
        for(Pivot pivot:cleanedPivotList){
            System.out.println(pivot.toString());
        }
        System.out.println("Size of cleanedPivotList is:"+cleanedPivotList.size());*/
        List<Dpattern> finalDpatternList = pivotHandle.findallDpattern(pivotList);
        //finalDpatternList.addAll(doublePivotPatternList);
        /*for(Dpattern dpattern:finalDpatternList){
            System.out.println(dpattern);
        }*/
    }

}




