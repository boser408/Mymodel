package service;

import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Scratch;

import java.util.List;

public interface InAndOutHandle {
    List<HighLowPrice> readBarFromCSV(String fileAddress);
    List<HighLowPrice> readDataFromIBCSV(String fileAddress);
    void savePriceBarToCSV(List<HighLowPrice> highLowPrices,String fileAddress);
    void saveScratchListToCSV(List<Scratch> scratchList,String filePath);
    List<Scratch> readScratchFromCSV(String fileAddress);
}
