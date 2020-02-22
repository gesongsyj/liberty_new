package com.liberty.system.strategy.Judger.filterParam.impl;

import com.liberty.system.strategy.Judger.filterParam.FilterParam;
import com.liberty.system.model.Currency;
import com.liberty.system.strategy.Judger.Impl.VolumeJudger;
import com.liberty.system.strategy.Judger.Judger;

import java.util.Date;

/**
 * 成交量相关参数
 */
public class VolumeParam implements FilterParam {

    // 参考成交量均值数,均值计算要算上当日成交量
    private Integer avgCount =5;
    // 相对均值成交量放大倍数
    private Double volumeIncreaseTimes = 2.0;

    public Integer getAvgCount() {
        return avgCount;
    }

    public void setAvgCount(Integer avgCount) {
        this.avgCount = avgCount;
    }

    public Double getVolumeIncreaseTimes() {
        return volumeIncreaseTimes;
    }

    public void setVolumeIncreaseTimes(Double volumeIncreaseTimes) {
        this.volumeIncreaseTimes = volumeIncreaseTimes;
    }

    @Override
    public Judger initJudger() {
        return new VolumeJudger(this);
    }

    /**
     * 调用判断器的判断方法
     */
    @Override
    public boolean judge(Currency currency, Date date){
        return initJudger().judgeItem(currency,date);
    }

}
