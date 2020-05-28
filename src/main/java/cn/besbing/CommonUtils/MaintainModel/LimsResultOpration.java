package cn.besbing.CommonUtils.MaintainModel;

import cn.besbing.Entities.Result;
import cn.besbing.Entities.ResultDefKey;

import java.util.List;

public class LimsResultOpration {

    /**
     * 拿到小垂的result结果，封装ResultThreePrimary实体，然后查询
     * 有：则update
     * 无：则insert
     * 返回ResultThreePrimary.toString()
     * @param result
     * @return
     */
    public String paramProcessor(Result result){
        ResultDefKey resultDefKey = new ResultDefKey();
        resultDefKey.setName(result.getName());
        resultDefKey.setSampleNumber(result.getSampleNumber());
        resultDefKey.setTestNumber(result.getTestNumber());

        return "";
    }

}
