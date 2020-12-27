package com.myproject.mymodel;

import com.myproject.mymodel.controller.GlobalController;
import com.myproject.mymodel.domain.HighLowPrice;
import org.junit.jupiter.api.Test;
import service.InAndOutHandle;
import service.impl.InAndOutHandleImpl;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

class MymodelApplicationTests {
    @Test
    void trySummaryStats(){
        InAndOutHandle inAndOutHandle=new InAndOutHandleImpl();
        List<HighLowPrice> highLowPrices = inAndOutHandle.readBarFromCSV("C:\\Users\\bjsh2\\Documents\\Investment\\Data\\spxw1927.csv");
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
    }
    @Test
    void GlobalControl(){
        String[] contractList={"ES","NQ","YM","RTY","GC"};
        String downloadDataPath="E:\\Data\\DownloadData\\";
        String basicScratchAddress="E:\\out\\tryWrite\\basicScratch.csv";
        String allCompoundScratchAddress="E:\\out\\tryWrite\\AllCompoundScratch.csv";
        String eigenScratchAddress="E:\\Data\\EigenScratch\\eigenScrach";
        //String contractClass="ES"; //Ticker like: ES, NQ, YM, RTY, GC, GLD...
        String contractSubLabel="Z0"; //Supplementary description for contract such as "Z0" for ES, then build the full ticker of a contract like "ESZ0";
        String priceBarType="15mins";
        for(String string:contractList){
            GlobalController globalController=new GlobalController(downloadDataPath,eigenScratchAddress,basicScratchAddress,allCompoundScratchAddress,string,contractSubLabel,priceBarType);
            globalController.dataHandle();
        }
    }
}




