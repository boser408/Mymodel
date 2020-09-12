package com.myproject.mymodel.mapper;

import com.myproject.mymodel.domain.Scratch;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface ScratchMapper {
    List<Scratch> selectAllScratches();
    List<Scratch> selectAndSortAll(int length);
    List<Scratch> selectByLength(int length);
    List<Scratch> selectSmallerByLength(int length);
    void deleteAll(String tableName);
    void save (Scratch scratch);
    void addDate();
    void batchinsert (List<Scratch> scratches);
    void batchsmallinsert (List<Scratch> scratches);

}
