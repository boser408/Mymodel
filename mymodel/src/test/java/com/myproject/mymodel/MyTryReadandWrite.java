package com.myproject.mymodel;

import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Pivot;
import com.myproject.mymodel.domain.Scratch;
import org.junit.jupiter.api.Test;
import service.InAndOutHandle;
import service.PatternStats;
import service.PivotHandle;
import service.impl.InAndOutHandleImpl;
import service.impl.PatternStatsImpl;
import service.impl.PivotHandleImpl;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MyTryReadandWrite {
    @Test
    void tryReadWrite(){
        List<HighLowPrice> highLowPrices=new ArrayList<>();
        try {
            String pathname = "C:\\Users\\bjsh2\\Documents\\Investment\\Data\\spxd1927.csv";
            File filename = new File(pathname);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
            BufferedReader br = new BufferedReader(reader);
            String line = "";
            String cvsSplitBy=",";
            int t=1;
            while ((line = br.readLine())!= null) {
               String[] pricebar=line.split(cvsSplitBy);
               HighLowPrice highLowPrice=new HighLowPrice();
               highLowPrice.setId(t);
               highLowPrice.setDate(pricebar[0]);
               highLowPrice.setOpen(Float.parseFloat(pricebar[1]));
               highLowPrice.setHigh(Float.parseFloat(pricebar[2]));
               highLowPrice.setLow(Float.parseFloat(pricebar[3]));
               highLowPrice.setClose(Float.parseFloat(pricebar[4]));
               highLowPrices.add(highLowPrice);
               t++;
            }

            System.out.println("Size of Pricebar is: "+highLowPrices.size());
            for(int n=0;n<=9;n++){
                System.out.println("Price bar is"+highLowPrices.get(n).toString());
            }

            File writename = new File("E:\\out\\tryWrite\\output.csv");
            writename.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            for(HighLowPrice highLowPrice:highLowPrices){
                out.write(highLowPrice.getId()+","+highLowPrice.getDate()+","+highLowPrice.getOpen()+","
                        +highLowPrice.getHigh()+","+highLowPrice.getLow()+","+highLowPrice.getClose());
                out.newLine();
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    void tryRewrite(){
        List<HighLowPrice> highLowPrices=new ArrayList<>();
        try{
            String pathname = "E:\\out\\tryWrite\\output.csv";
            File filename = new File(pathname);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
            BufferedReader br = new BufferedReader(reader);
            String line = "";
            String cvsSplitBy=",";
            while ((line = br.readLine())!= null) {
                String[] pricebar=line.split(cvsSplitBy);
                HighLowPrice highLowPrice=new HighLowPrice();
                highLowPrice.setId(Integer.parseInt(pricebar[0]));
                highLowPrice.setDate(pricebar[1]);
                highLowPrice.setOpen(Float.parseFloat(pricebar[2]));
                highLowPrice.setHigh(Float.parseFloat(pricebar[3]));
                highLowPrice.setLow(Float.parseFloat(pricebar[4]));
                highLowPrice.setClose(Float.parseFloat(pricebar[5]));
                highLowPrices.add(highLowPrice);
            }
            System.out.println("Size of Pricebar is: "+highLowPrices.size());
            for(int n=0;n<=9;n++){
                System.out.println("Price bar is"+highLowPrices.get(n).toString());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
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
    @Test
    void trymergeDailyandIntraday(){
        String dailyAddress="E:\\Data\\EigenScratch\\eigenScrachspxd1927.csv";
        String intradayAddress="E:\\Data\\EigenScratch\\eigenScrachESZ015mins.csv";
        InAndOutHandle inAndOutHandle=new InAndOutHandleImpl();
        List<Scratch> dailyEigenScratches=inAndOutHandle.readScratchFromCSV(dailyAddress);
        List<Scratch> intradayEigenScratches=inAndOutHandle.readScratchFromCSV(intradayAddress);
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
        System.out.println("Size of mergedScratches is "+mergedScratches.size());
        for(Scratch scratch:mergedScratches){
            System.out.println(scratch.toString());
        }
    }
}
