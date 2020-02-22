package com.liberty.system.strategy.agent.impl;

import com.liberty.common.utils.DateUtil;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Strategy;
import com.liberty.system.strategy.agent.StrategyAgent;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 打板策略
 */
public class BeatingBoardStrategyAgent extends StrategyAgent {
    @Override
    public void executeSingle(Currency currency) {

    }

    @Override
    public void initInStrategyCurrencyList() {
        inStrategyCurrencyList = filterDailyLimitLastDay();
    }

    @Override
    public void initStrategy() {
        Strategy.dao.findById(9);
    }

    private List<Currency> filterDailyLimitLastDay(){
        Date yesterday = DateUtil.getSomeDay(new Date(), -1);
        List<Currency> currencies = Currency.dao.listAll();
        Iterator<Currency> iterator = currencies.iterator();
        while (iterator.hasNext()){
            Currency next = iterator.next();
            Kline lastOne = Kline.dao.getLastOneByCodeAndDate(next.getCode(), Kline.KLINE_TYPE_K, yesterday);
            if(null != lastOne.getAoi() && lastOne.getAoi()<0.099){
                iterator.remove();
            }
        }
        return currencies;
    }
}
