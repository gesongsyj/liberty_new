package com.liberty.system.strategy.Judger.filterParam.impl;

import com.liberty.system.strategy.Judger.filterParam.FilterParam;
import com.liberty.system.model.Currency;
import com.liberty.system.strategy.Judger.Impl.MaJudger;
import com.liberty.system.strategy.Judger.Judger;

import java.util.Date;

/**
 * 移动平均线相关参数
 */
public class MaParam implements FilterParam {

    // 上穿均线,包含CROSSTYPE_ON
    public static final String CROSSTYPE_UP = "0";
    // 下穿均线,包含CROSSTYPE_UNDER
    public static final String CROSSTYPE_DOWN = "1";
    // 最低点在均线之上
    public static final String CROSSTYPE_ON = "2";
    // 最高点在均线之下
    public static final String CROSSTYPE_UNDER = "3";

    // 移动平均线周期
    private Integer maCount = 250;
    // 穿越均线类型
    private String crossType = CROSSTYPE_UP;

    public Integer getMaCount() {
        return maCount;
    }

    public void setMaCount(Integer maCount) {
        this.maCount = maCount;
    }

    public String getCrossType() {
        return crossType;
    }

    public void setCrossType(String crossType) {
        this.crossType = crossType;
    }

    @Override
    public Judger initJudger() {
        return new MaJudger(this);
    }

    /**
     *调用判断器的判断方法
     */
    @Override
    public boolean judge(Currency currency, Date date){
        return initJudger().judgeItem(currency,date);
    }
}
