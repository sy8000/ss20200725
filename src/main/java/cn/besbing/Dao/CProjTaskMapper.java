package cn.besbing.Dao;

import cn.besbing.Entities.CProjTask;
import cn.besbing.Entities.CProjTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CProjTaskMapper {
    long countByExample(CProjTaskExample example);

    int deleteByExample(CProjTaskExample example);

    int deleteByPrimaryKey(Long seqNum);

    int insert(CProjTask record);

    int insertSelective(CProjTask record);

    List<CProjTask> selectByExample(CProjTaskExample example);

    CProjTask selectByPrimaryKey(Long seqNum);

    int updateByExampleSelective(@Param("record") CProjTask record, @Param("example") CProjTaskExample example);

    int updateByExample(@Param("record") CProjTask record, @Param("example") CProjTaskExample example);

    int updateByPrimaryKeySelective(CProjTask record);

    int updateByPrimaryKey(CProjTask record);
}