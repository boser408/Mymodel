package service;

import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Scratch;

import java.util.List;

public interface InAndOutHandle {
    List<HighLowPrice> readBarFromCSV(String fileAddress);
    void saveScratchListToCSV(List<Scratch> scratchList,String filePath);
    List<Scratch> readScratchFromCSV(String fileAddress);
}
