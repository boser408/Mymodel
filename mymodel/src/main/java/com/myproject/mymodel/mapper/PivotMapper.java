package com.myproject.mymodel.mapper;

import com.myproject.mymodel.domain.Pivot;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface PivotMapper {
    Void simpleinsert(Pivot pivot);
    void batchsaveAll(List<Pivot> pivotList);
}
