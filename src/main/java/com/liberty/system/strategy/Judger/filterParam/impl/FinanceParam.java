package com.liberty.system.strategy.Judger.filterParam.impl;

import com.liberty.system.strategy.Judger.filterParam.FilterParam;
import com.liberty.system.model.Currency;
import com.liberty.system.strategy.Judger.Impl.FinanceJudger;
import com.liberty.system.strategy.Judger.Judger;

import java.util.Date;

/**
 * 财务状况相关参数
 */
public class FinanceParam implements FilterParam {

    // 归属净利润是否连年增长
    private Boolean increaseEnsure = true;
    // 判断年数
    private Integer judgeYearCount = 2;
    // 净资产收益率阈值
    private Double jzcsylLimit = 25.0;
    // 允许的误差范围
    private Double errorRangeLimit = 0.05;

    public Boolean getIncreaseEnsure() {
        return increaseEnsure;
    }

    public void setIncreaseEnsure(Boolean increaseEnsure) {
        this.increaseEnsure = increaseEnsure;
    }

    public Integer getJudgeYearCount() {
        return judgeYearCount;
    }

    public void setJudgeYearCount(Integer judgeYearCount) {
        this.judgeYearCount = judgeYearCount;
    }

    public Double getJzcsylLimit() {
        return jzcsylLimit;
    }

    public void setJzcsylLimit(Double jzcsylLimit) {
        this.jzcsylLimit = jzcsylLimit;
    }

    public Double getErrorRangeLimit() {
        return errorRangeLimit;
    }

    public void setErrorRangeLimit(Double errorRangeLimit) {
        this.errorRangeLimit = errorRangeLimit;
    }

    @Override
    public Judger initJudger() {
        return new FinanceJudger(this);
    }

    /**
     * 调用判断器的判断方法
     * @param currency
     * @return
     */
    @Override
    public boolean judge(Currency currency, Date date){
        return initJudger().judgeItem(currency,date);
    }
}
