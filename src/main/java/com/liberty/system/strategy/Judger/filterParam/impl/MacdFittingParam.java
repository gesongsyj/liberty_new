package com.liberty.system.strategy.Judger.filterParam.impl;

import com.liberty.system.model.Currency;
import com.liberty.system.strategy.Judger.Impl.MaFittingJudger;
import com.liberty.system.strategy.Judger.Impl.MacdFittingJudger;
import com.liberty.system.strategy.Judger.Judger;
import com.liberty.system.strategy.Judger.filterParam.FilterParam;

import java.util.Date;

public class MacdFittingParam implements FilterParam {
    // MACD样本数
    private int macdCount = 5;
    // 拟合直线的斜率阈值
    private double kLimit = 0.028;
//    private double kLimit = 0.050;
    // 离散程度阈值
    private double dispersionDegreeLimit = 0.031;

    public int getMacdCount() {
        return macdCount;
    }

    public void setMacdCount(int macdCount) {
        this.macdCount = macdCount;
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
        return new MacdFittingJudger(this);
    }

    @Override
    public boolean judge(Currency currency, Date date) {
        return initJudger().judgeItem(currency,date);
    }
}
