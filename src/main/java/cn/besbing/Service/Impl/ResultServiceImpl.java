package cn.besbing.Service.Impl;

import cn.besbing.Dao.ResultMapper;
import cn.besbing.Entities.Result;
import cn.besbing.Service.IResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ResultServiceImpl implements IResultService {

    @Autowired(required = false)
    ResultMapper resultMapper;

    public void setResultUpdate(Result result){
        resultMapper.updateByDefKey(result);
    }


}
