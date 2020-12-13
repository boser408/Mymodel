package com.myproject.mymodel;

import com.myproject.mymodel.controller.GlobalController;
import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Scratch;
import com.myproject.mymodel.mapper.HighLowPriceMapper;
import com.myproject.mymodel.mapper.ScratchMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import service.PatternStats;
import service.PivotHandle;
import service.impl.PatternStatsImpl;
import service.impl.PivotHandleImpl;

import java.util.List;

@SpringBootApplication
@MapperScan("com.myproject.mymodel.mapper")
public class MymodelApplication {
    public static void main(String[] args) {
        SpringApplication.run(MymodelApplication.class, args);
    }

}
