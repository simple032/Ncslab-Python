package com.ncslab.utils;

import com.ncslab.database.MdlBlock;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MdlBlockMapper {

    // 创建（Insert）
    int insert(MdlBlock mdlBlock);

    // 读取（Select）
    MdlBlock selectById(Integer id);

    // 读取所有记录（Select all）
    List<MdlBlock> selectAll();

    // 更新（Update）
    int update(MdlBlock mdlBlock);

    // 删除（Delete）
    int deleteById(Integer id);
}
