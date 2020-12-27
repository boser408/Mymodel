package service.impl;

import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Scratch;
import service.InAndOutHandle;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class InAndOutHandleImpl implements InAndOutHandle {
    @Override
    public List<HighLowPrice> readBarFromCSV(String fileAddress) {
        List<HighLowPrice> highLowPrices=new ArrayList<>();
        try {
            File filename = new File(fileAddress);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return highLowPrices;
    }

    @Override
    public List<HighLowPrice> readDataFromIBCSV(String fileAddress) {
        List<HighLowPrice> highLowPrices=new ArrayList<>();
        try {
            File filename = new File(fileAddress);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return highLowPrices;
    }

    @Override
    public void savePriceBarToCSV(List<HighLowPrice> highLowPrices, String fileAddress) {
        try{
            File writename = new File(fileAddress);
            writename.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            for(HighLowPrice highLowPrice:highLowPrices){
                out.write(highLowPrice.getId()+","+highLowPrice.getDate()+","
                        +highLowPrice.getOpen()+","+highLowPrice.getHigh()+","+highLowPrice.getLow()+","+highLowPrice.getClose());
                out.newLine();
            }
            out.flush();
            out.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveScratchListToCSV(List<Scratch> scratchList,String filePath) {
        try{
            File writename = new File(filePath);
            writename.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            for(Scratch scratch:scratchList){
                out.write(scratch.getStartdate()+","+scratch.getLength()+","
                        +scratch.getStartId()+","+scratch.getHigh()+","+scratch.getLow()+","+scratch.getStatus());
                out.newLine();
            }
            out.flush();
            out.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Scratch> readScratchFromCSV(String fileAddress) {
        List<Scratch> scratchList=new ArrayList<>();
        try {
            File filename = new File(fileAddress);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
            BufferedReader br = new BufferedReader(reader);
            String line = "";
            String cvsSplitBy=",";
            while ((line = br.readLine())!= null) {
                String[] pricebar=line.split(cvsSplitBy);
                Scratch scratch=new Scratch();
                scratch.setStartdate(pricebar[0]);
                scratch.setLength(Integer.parseInt(pricebar[1]));
                scratch.setStartId(Integer.parseInt(pricebar[2]));
                scratch.setHigh(Float.parseFloat(pricebar[3]));
                scratch.setLow(Float.parseFloat(pricebar[4]));
                scratch.setStatus(Integer.parseInt(pricebar[5]));
                scratchList.add(scratch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scratchList;
    }


}
