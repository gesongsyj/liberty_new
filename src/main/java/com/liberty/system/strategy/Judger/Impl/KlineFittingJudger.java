package com.liberty.system.strategy.Judger.Impl;

import com.liberty.common.utils.stock.MathUtil;
import com.liberty.system.bean.common.LsmParam;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.strategy.Judger.Judger;
import com.liberty.system.strategy.Judger.filterParam.impl.KlineFittingParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class KlineFittingJudger implements Judger {
    // 持有K线拟合直线参数
    private KlineFittingParam klineFittingParam;
    // 带参数构造器
    public KlineFittingJudger(KlineFittingParam klineFittingParam) {
        this.klineFittingParam = klineFittingParam;
    }

    @Override
    public boolean judgeItem(Currency currency, Date date) {
        List<Kline> klines = Kline.dao.listBeforeDate(currency.getId(), Kline.KLINE_TYPE_K, date, klineFittingParam.getKlineCount());
        if(klines.size()<klineFittingParam.getKlineCount()){
            return false;
        }
        Collections.reverse(klines);
        List<Double> doubles = new ArrayList<>();
        klines.forEach(kline -> doubles.add(kline.getMin()));
        // 进行标准化处理
        MathUtil.normalization(doubles);
        // 最小二乘法计算alpha和beta值
        LsmParam lsmParam = MathUtil.lsmCal(doubles);
        // 判断拟合度和斜率是否满足要求
        return MathUtil.lineFittingCheck(doubles, lsmParam, klineFittingParam.getkLimit(),klineFittingParam.getDispersionDegreeLimit());
    }
}
