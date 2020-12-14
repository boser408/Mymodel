package com.myproject.mymodel;

import com.myproject.mymodel.domain.HighLowPrice;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
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
}
