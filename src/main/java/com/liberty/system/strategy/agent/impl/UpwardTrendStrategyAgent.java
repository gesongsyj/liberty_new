package com.liberty.system.strategy.agent.impl;

import com.liberty.common.utils.stock.MaUtil;
import com.liberty.common.utils.MailUtil;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Strategy;
import com.liberty.system.model.Stroke;
import com.liberty.system.strategy.agent.StrategyAgent;

import java.util.List;

/**
 * 上升趋势策略
 */
public class UpwardTrendStrategyAgent extends StrategyAgent {
    @Override
    public void executeSingle(Currency currency) {
        // 获取最后一笔
        Stroke last1ByCode = Stroke.dao.getLastBeforeDate(currency.getCode(), Kline.KLINE_TYPE_K,getExeDate());
        if(null == last1ByCode){
            return;
        }
        // 最后一笔不能向上
        if(Stroke.STROKE_TYPE_UP.equals(last1ByCode.getDirection())){
            return;
        }

        // 计算移动平均值
        int dayCount = 250;
        List<Kline> klinesOfLast1Stroke = Kline.dao.listBeforeDate(currency.getCode(), Kline.KLINE_TYPE_K, last1ByCode.getEndDate(), dayCount);
        if (klinesOfLast1Stroke.size() < 250) {
            return;
        }
        // 得到计算的移动平均线的值
        Double maPointOfLast1Stroke = MaUtil.calculateMAPoint(klinesOfLast1Stroke, dayCount);
        // 最后一笔的最小值必须在移动平均值之下
        if(last1ByCode.getMin()>maPointOfLast1Stroke){
            return;
        }

        // 当前K线
        Kline last1 = Kline.dao.getLastOneByCodeAndDate(currency.getCode(), Kline.KLINE_TYPE_K,getExeDate());
        if(last1.getDiff()<0 || last1.getDea()<0){
            return;
        }
        List<Kline> klines = Kline.dao.listBeforeDate(currency.getCode(), Kline.KLINE_TYPE_K, last1.getDate(), dayCount);
        // 得到计算的移动平均线的值
        Double maPointOfKlines = MaUtil.calculateMAPoint(klines, dayCount);
        // 当前K线的最高点必须在移动平均值之上
        if(last1.getMax()<maPointOfKlines){
            return;
        }

        Kline kline = Kline.dao.getByDate(currency.getCode(),Kline.KLINE_TYPE_K, getExeDate());
        kline.setBosp("0");
        kline.update();

        MailUtil.addCurrency2Buy(getExeDate(),strategy,currency);
    }

    @Override
    public void initInStrategyCurrencyList() {
//        inStrategyCurrencyList = Currency.dao.listAll();
        Currency byCode = Currency.dao.findByCode("600267");
        inStrategyCurrencyList.add(byCode);
    }

    @Override
    public void initStrategy() {
        strategy = Strategy.dao.findById(10);
    }

}
