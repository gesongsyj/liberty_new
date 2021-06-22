package com.liberty.system.strategy.Judger.Impl;

import com.liberty.common.utils.stock.MaUtil;
import com.liberty.common.utils.stock.MathUtil;
import com.liberty.system.bean.common.LsmParam;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.strategy.Judger.Judger;
import com.liberty.system.strategy.Judger.filterParam.impl.MaFittingParam;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MaFittingJudger implements Judger {

    // 持有均线拟合参数对象
    private MaFittingParam maFittingParam;

    // 带参数构造器
    public MaFittingJudger(MaFittingParam maFittingParam) {
        this.maFittingParam = maFittingParam;
    }

    @Override
    public boolean judgeItem(Currency currency, Date date) {
        // 判断的样本值
        int count = maFittingParam.getMaCount();
        List<Kline> klines = Kline.dao.listBeforeDate(currency.getId(), Kline.KLINE_TYPE_K,date,maFittingParam.getMaCount()+maFittingParam.getMaCycleCount());
        Collections.reverse(klines);
        List<Double> mas = MaUtil.calculateMA(klines, maFittingParam.getMaCycleCount());
        if(mas.size()<count){
            return false;
        }
        // 对集合进行翻转
        Collections.reverse(mas);
        // 取前100条记录
        List<Double> masSub = mas.subList(0, count);
        // 在翻转回来
        Collections.reverse(masSub);
        // 进行标准化处理
        MathUtil.normalization(masSub);
        // 最小二乘法计算alpha和beta值
        LsmParam lsmParam = MathUtil.lsmCal(masSub);
        // 判断拟合度和斜率是否满足要求
        boolean b = MathUtil.lineFittingCheck(masSub, lsmParam,maFittingParam.getkLimit(),maFittingParam.getDispersionDegreeLimit());
        return b;
    }
}
