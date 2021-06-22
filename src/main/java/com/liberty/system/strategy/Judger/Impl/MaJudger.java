package com.liberty.system.strategy.Judger.Impl;

import com.liberty.common.utils.stock.MaUtil;
import com.liberty.system.model.Line;
import com.liberty.system.strategy.Judger.filterParam.impl.MaParam;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.strategy.Judger.Judger;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MaJudger implements Judger {
    // 持有移动平均线参数对象
    private MaParam maParam;

    // 带参数构造器
    public MaJudger(MaParam maParam) {
        this.maParam = maParam;
    }

    /**
     * 校验最后一天线段后是否有K线在移动平均线之下
     * @param currency
     * @param date
     * @return
     */
    private boolean checkUnderMa(Currency currency,Date date){
        Line lastLine = Line.dao.getLastBeforeDate(currency.getCode(),Kline.KLINE_TYPE_K,date);
        List<Kline> klinesByDateRange = Kline.dao.getByDateRange(currency.getId(), Kline.KLINE_TYPE_K, lastLine.getEndDate(), date);
        List<Kline> klinesForCalMa = Kline.dao.listBeforeDate(currency.getId(), Kline.KLINE_TYPE_K, date, maParam.getMaCount() + klinesByDateRange.size());
        Collections.reverse(klinesForCalMa);
        List<Double> maPoints = MaUtil.calculateMA(klinesForCalMa, maParam.getMaCount());
        for (int i = 0; i < klinesByDateRange.size(); i++) {
            if(klinesByDateRange.get(i).getMin()<maPoints.get(i)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean judgeItem(Currency currency, Date date) {
        if(!checkUnderMa(currency,date)){
            return false;
        }

        // 查询指定时间之前的K线
        List<Kline> klines = Kline.dao.listBeforeDate(currency.getId(), Kline.KLINE_TYPE_K, date, maParam.getMaCount());
        if (klines.size() < maParam.getMaCount()) {
            return false;
        }
        // 得到计算的移动平均线的值
        Double maPointOfKline = MaUtil.calculateMAPoint(klines, maParam.getMaCount());
        // 当前K线的最高点必须高于移动平均线
        return klines.get(0).getMax()>=maPointOfKline;
    }

//    @Override
//    public boolean judgeItem(Currency currency, Date date) {
//        Stroke stroke = Stroke.dao.getLastBeforeDate(currency.getCode(),Kline.KLINE_TYPE_K,date);
//        if(null == stroke){
//            return false;
//        }
//        // 最后一笔需向上
//        if(stroke.getDirection().equals(Stroke.STROKE_TYPE_UP)){
//            return false;
//        }
//        List<Kline> klinesBeforeStroke = Kline.dao.listBeforeDate(currency.getCode(), Kline.KLINE_TYPE_K, stroke.getEndDate(), maParam.getMaCount());
//        if(klinesBeforeStroke.size()<maParam.getMaCount()){
//            return false;
//        }
//        // 得到计算的移动平均线的值
//        Double maPointOfStroke = MaUtil.calculateMAPoint(klinesBeforeStroke, maParam.getMaCount());
//
//        // 查询指定时间之前的K线
//        List<Kline> klines = Kline.dao.listBeforeDate(currency.getCode(), Kline.KLINE_TYPE_K, date, maParam.getMaCount());
//        if (klines.size() < maParam.getMaCount()) {
//            return false;
//        }
//        // 得到计算的移动平均线的值
//        Double maPointOfKline = MaUtil.calculateMAPoint(klines, maParam.getMaCount());
//        // 当前K线的最高点必须高于移动平均线
//        return stroke.getMin() < maPointOfStroke && klines.get(0).getMax()>=maPointOfKline;
//    }
}
