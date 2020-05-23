package cn.besbing.Service;

import cn.besbing.CommonUtils.MaintainModel.PageDataResult;
import cn.besbing.CommonUtils.MaintainModel.FormatSearchDTO;
import cn.besbing.Entities.InstrumentsExample;
import org.springframework.stereotype.Service;

@Service
public interface IInstrumentsService {
    public PageDataResult getPdrData(FormatSearchDTO userSearch);
    long countByExample(InstrumentsExample example);
}
