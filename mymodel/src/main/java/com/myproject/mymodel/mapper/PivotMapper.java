package com.myproject.mymodel.mapper;

import com.myproject.mymodel.domain.Dpattern;
import com.myproject.mymodel.domain.Pivot;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface PivotMapper {
    List<Dpattern> selectAll();
    Void simpleinsert(Pivot pivot);
    void batchsaveAll(List<Dpattern> dpatternList);
}
