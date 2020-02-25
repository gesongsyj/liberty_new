package com.liberty.system.strategy.Judger.Impl;

import com.liberty.common.utils.stock.MathUtil;
import com.liberty.system.bean.common.LsmParam;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.strategy.Judger.Judger;
import com.liberty.system.strategy.Judger.filterParam.impl.MacdFittingParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MacdFittingJudger implements Judger {
    // 持有MACD拟合直线参数对象
    private MacdFittingParam macdFittingParam;

    // 带参数构造器
    public MacdFittingJudger(MacdFittingParam macdFittingParam) {
        this.macdFittingParam = macdFittingParam;
    }

    @Override
    public boolean judgeItem(Currency currency, Date date) {
        List<Kline> klines = Kline.dao.listBeforeDate(currency.getCode(), Kline.KLINE_TYPE_K, date, macdFittingParam.getMacdCount());
        // 当前不能是绿柱子
        if(klines.get(0).getBar()<0){
            return false;
        }
        // 翻转成正序
        Collections.reverse(klines);
        List<Double> bars = new ArrayList<>();
        klines.forEach(kline -> bars.add(kline.getBar()));
        // 进行标准化处理
        MathUtil.maxNormalization(bars);
        // 最小二乘法计算alpha和beta值
        LsmParam lsmParam = MathUtil.lsmCal(bars);
        // 判断拟合度和斜率是否满足要求
        return MathUtil.lineFittingCheck(bars, lsmParam, macdFittingParam.getkLimit(),macdFittingParam.getDispersionDegreeLimit());
    }
}
