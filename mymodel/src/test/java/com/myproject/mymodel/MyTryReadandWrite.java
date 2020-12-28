package com.myproject.mymodel;

import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Scratch;
import org.junit.jupiter.api.Test;
import service.InAndOutHandle;
import service.PivotHandle;
import service.impl.InAndOutHandleImpl;
import service.impl.PivotHandleImpl;

import java.util.List;

public class MyTryReadandWrite {

    @Test
    void tryUpdate(){
        String priceBarAddress="C:\\Users\\bjsh2\\Documents\\Investment\\Data\\spxd1927.csv";
        String updateFileAddress="C:\\Users\\bjsh2\\Documents\\Investment\\Data\\updateData\\spxd.csv";
        String updatedPriceBarAddress="C:\\Users\\bjsh2\\Documents\\Investment\\Data\\updateData\\updatedspxd.csv";
        String basicScratchAddress="E:\\out\\tryWrite\\basicScratch.csv";
        String updatedBasicScratch="E:\\out\\tryWrite\\updatedbasicScratch.csv";
        InAndOutHandle inAndOutHandle=new InAndOutHandleImpl();
        PivotHandle pivotHandle=new PivotHandleImpl();
        List<HighLowPrice> highLowPrices = inAndOutHandle.readBarFromCSV(priceBarAddress);
        List<HighLowPrice> updatehighLowPrices=inAndOutHandle.readBarFromCSV(updateFileAddress);
        int baseSize=highLowPrices.size();
        for(int n=1;n<=updatehighLowPrices.size();n++){
            updatehighLowPrices.get(n-1).setId(baseSize+n);
        }
        highLowPrices.addAll(updatehighLowPrices);
        inAndOutHandle.savePriceBarToCSV(highLowPrices,updatedPriceBarAddress);
        List<Scratch> basicScratchList=inAndOutHandle.readScratchFromCSV(basicScratchAddress);
        Scratch upscratch=new Scratch();
        Scratch dwscratch=new Scratch();
        int endindex=basicScratchList.size()-1;
        if(basicScratchList.get(endindex).getStatus()>0){
            upscratch=new Scratch(basicScratchList.get(endindex));
            dwscratch=new Scratch(basicScratchList.get(endindex-1));
            upscratch.setStatus(0);
            if(dwscratch.getLength()<pivotHandle.getPivotLength()){
                dwscratch.setStatus(0);
            }
        }else {
            dwscratch=new Scratch(basicScratchList.get(endindex));
            upscratch=new Scratch(basicScratchList.get(endindex-1));
            dwscratch.setStatus(0);
            if(upscratch.getLength()<pivotHandle.getPivotLength()){
                upscratch.setStatus(0);
            }
        }
        int nofupscratch=upscratch.getStartId()+upscratch.getLength()-1;
        int nofdwscratch=dwscratch.getStartId()+dwscratch.getLength()-1;
        List<Scratch> scratchList = pivotHandle.findScratches(highLowPrices, inAndOutHandle.readBarFromCSV(priceBarAddress).size(),upscratch,dwscratch,nofupscratch,nofdwscratch);
        System.out.println(scratchList.size());
        inAndOutHandle.saveScratchListToCSV(scratchList,updatedBasicScratch);
    }

}
