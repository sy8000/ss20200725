package cn.besbing.Dao;

import cn.besbing.Entities.Component;
import cn.besbing.Entities.ComponentExample;
import cn.besbing.Entities.ComponentKey;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ComponentMapper {
    long countByExample(ComponentExample example);

    int deleteByExample(ComponentExample example);

    int deleteByPrimaryKey(ComponentKey key);

    int insert(Component record);

    int insertSelective(Component record);

    List<Component> selectByExample(ComponentExample example);

    Component selectByPrimaryKey(ComponentKey key);

    int updateByExampleSelective(@Param("record") Component record, @Param("example") ComponentExample example);

    int updateByExample(@Param("record") Component record, @Param("example") ComponentExample example);

    int updateByPrimaryKeySelective(Component record);

    int updateByPrimaryKey(Component record);
}