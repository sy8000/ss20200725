package cn.besbing.Service.Impl;

import cn.besbing.Dao.InstrumentsMapper;
import cn.besbing.Entities.InstrumentsWithBLOBs;
import cn.besbing.Service.IInstrumentsService;
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

}
