package com.myproject.mymodel.mapper;

import com.myproject.mymodel.domain.HighLowPrice;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface HighLowPriceMapper {
    List<HighLowPrice> selectHighLow();
    void save (HighLowPrice highLowPrice);
    void batchinsert (List<HighLowPrice> highLowPrices);
}
