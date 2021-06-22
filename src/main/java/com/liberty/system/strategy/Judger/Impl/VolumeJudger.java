package com.liberty.system.strategy.Judger.Impl;

import com.liberty.common.utils.stock.VolumeUtil;
import com.liberty.system.strategy.Judger.filterParam.impl.VolumeParam;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.strategy.Judger.Judger;

import java.util.Date;
import java.util.List;

public class VolumeJudger implements Judger {
    // 持有成交量参数对象
    private VolumeParam volumeParam;
    // 带参数构造器
    public VolumeJudger(VolumeParam volumeParam) {
        this.volumeParam = volumeParam;
    }

    @Override
    public boolean judgeItem(Currency currency, Date date) {
        List<Kline> klines = Kline.dao.listBeforeDate(currency.getId(), Kline.KLINE_TYPE_K, date, volumeParam.getAvgCount());
        if(klines.size()<volumeParam.getAvgCount()){
            return false;
        }
        // 得到计算的平均值
        Double volumeAvg = VolumeUtil.calculateVolumePoint(klines, volumeParam.getAvgCount());
        // 当前成交量必须高于平均值一定倍数
        return klines.get(0).getVolume()>=volumeParam.getVolumeIncreaseTimes()*volumeAvg;
    }
}
