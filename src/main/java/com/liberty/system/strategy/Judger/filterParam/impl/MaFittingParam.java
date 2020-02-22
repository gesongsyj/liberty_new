package com.liberty.system.strategy.Judger.filterParam.impl;

import com.liberty.system.model.Currency;
import com.liberty.system.strategy.Judger.Impl.MaFittingJudger;
import com.liberty.system.strategy.Judger.Judger;
import com.liberty.system.strategy.Judger.filterParam.FilterParam;

import java.util.Date;

/**
 * 移动平均线拟合直线参数
 */
public class MaFittingParam implements FilterParam {
    // 移动平均线周期数
    private int maCycleCount = 250;
    // 移动平均线样本数
    private int maCount = 5;
    // 拟合直线的斜率阈值
    private double kLimit = 0.00001;
    // 离散程度阈值
    private double dispersionDegreeLimit = 0.005;

    public int getMaCycleCount() {
        return maCycleCount;
    }

    public void setMaCycleCount(int maCycleCount) {
        this.maCycleCount = maCycleCount;
    }

    public int getMaCount() {
        return maCount;
    }

    public void setMaCount(int maCount) {
        this.maCount = maCount;
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
        return new MaFittingJudger(this);
    }

    @Override
    public boolean judge(Currency currency, Date date) {
        return initJudger().judgeItem(currency,date);
    }
}
