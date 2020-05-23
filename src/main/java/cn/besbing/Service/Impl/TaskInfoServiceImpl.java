package cn.besbing.Service.Impl;

import cn.besbing.Dao.CProjTaskMapper;
import cn.besbing.Entities.TableTaskFields;
import cn.besbing.Service.ITaskInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskInfoServiceImpl implements ITaskInfoService {

    @Autowired(required = false)
    private CProjTaskMapper cProjTaskMapper;

    public List<TableTaskFields> getTask(){
        return cProjTaskMapper.selectForTable();
    }

}
