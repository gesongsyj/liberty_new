package com.liberty.system.strategy.Judger.filterParam.impl;

import com.liberty.system.model.Currency;
import com.liberty.system.strategy.Judger.Impl.KlineFittingJudger;
import com.liberty.system.strategy.Judger.Judger;
import com.liberty.system.strategy.Judger.filterParam.FilterParam;

import java.util.Date;

public class KlineFittingParam implements FilterParam {
    // K线样本数
    private int klineCount = 10;
    // 拟合直线的斜率阈值
    private double kLimit = 0.011;
    // 离散程度阈值
    private double dispersionDegreeLimit = 0.019;

    public int getKlineCount() {
        return klineCount;
    }

    public void setKlineCount(int klineCount) {
        this.klineCount = klineCount;
    }

    public double getkLimit() {
        return kLimit;
    }

    public void setkLimit(double kLimit) {
        this.kLimit = kLimit;
    }

    public double getDispersionDegreeLimit() {
        return dispersionDegreeLimit;
    }

    public void setDispersionDegreeLimit(double dispersionDegreeLimit) {
        this.dispersionDegreeLimit = dispersionDegreeLimit;
    }

    @Override
    public Judger initJudger() {
        return new KlineFittingJudger(this);
    }

    @Override
    public boolean judge(Currency currency, Date date) {
        return initJudger().judgeItem(currency,date);
    }
}
