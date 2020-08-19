package com.myproject.mymodel;

import com.myproject.mymodel.domain.HighLowPrice;
import com.myproject.mymodel.domain.Scratch;
import com.myproject.mymodel.mapper.HighLowPriceMapper;
import com.myproject.mymodel.mapper.ScratchMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import service.PivotHandle;
import service.impl.PivotHandleImpl;

import java.util.List;

@SpringBootApplication
public class MymodelApplication {

    public static void main(String[] args) {
        SpringApplication.run(MymodelApplication.class, args);
    }


}
