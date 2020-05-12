package cn.besbing.Service.Impl;

import cn.besbing.CommonUtils.MaintainModel.PageDataResult;
import cn.besbing.CommonUtils.MaintainModel.UserSearchDTO;
import cn.besbing.Dao.InstrumentsMapper;
import cn.besbing.Entities.InstrumentsExample;
import cn.besbing.Entities.InstrumentsWithBLOBs;
import cn.besbing.Service.IInstrumentsService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstrumentsServiceImpl implements IInstrumentsService {

    @Autowired(required = false)
    InstrumentsMapper instrumentsMapper;

    public List<InstrumentsWithBLOBs> allInstruments(){
        return instrumentsMapper.selectAllInstruments();
    }


    @Override
    public PageDataResult getPdrData(UserSearchDTO userSearch) {
        PageDataResult pdr = new PageDataResult();
        PageHelper.startPage(userSearch.getPage(), userSearch.getLimit(),true);
        List<InstrumentsWithBLOBs> urList = instrumentsMapper.getInstruments(userSearch.getKeyworld());
        //logger.debug("urList:"+urList.size());

        // 获取分页查询后的数据
        PageInfo<InstrumentsWithBLOBs> pageInfo = new PageInfo<>(urList);
        // 设置获取到的总记录数total：
        //logger.debug("page:"+userSearch.getPage()+"limit:"+userSearch.getLimit()+"总行数:"+pageInfo.getTotal());
        pdr.setTotals(Long.valueOf(pageInfo.getTotal()).intValue());
        pdr.setList(urList);
        return pdr;
    }

    @Override
    public long countByExample(InstrumentsExample example) {
        return instrumentsMapper.countByExample(null);
    }
}
