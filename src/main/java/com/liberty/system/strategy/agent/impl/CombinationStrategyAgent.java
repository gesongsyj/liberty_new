package com.liberty.system.strategy.agent.impl;

import com.liberty.common.utils.MailUtil;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Strategy;
import com.liberty.system.strategy.Judger.filterParam.filter.StrategyFilter;
import com.liberty.system.strategy.Judger.filterParam.impl.*;
import com.liberty.system.strategy.agent.StrategyAgent;

import java.util.List;

public class CombinationStrategyAgent extends StrategyAgent {
    @Override
    public void executeSingle(Currency currency) {
        StrategyFilter strategyFilter = new StrategyFilter();

//        MaParam maParam = new MaParam();
//        VolumeParam volumeParam = new VolumeParam();
//        MaFittingParam maFittingParam = new MaFittingParam();
//        MacdFittingParam macdFittingParam = new MacdFittingParam();
        KlineFittingParam klineFittingParam = new KlineFittingParam();
//
//        strategyFilter.addFilterParam(maParam);
//        strategyFilter.addFilterParam(volumeParam);
//        strategyFilter.addFilterParam(maFittingParam);
//        strategyFilter.addFilterParam(macdFittingParam);
        strategyFilter.addFilterParam(klineFittingParam);

        boolean judge = strategyFilter.judge(currency,getExeDate());

        if(judge){
            Kline kline = Kline.dao.getByDate(currency.getId(),Kline.KLINE_TYPE_K, getExeDate());
            kline.setBosp("0");
            kline.update();
            MailUtil.addCurrency2Buy(getExeDate(),strategy,currency);
        }
    }

    @Override
    public void initInStrategyCurrencyList() {
//        inStrategyCurrencyList = Currency.dao.listAll();
        Currency byCode = Currency.dao.findByCode("002547");
        inStrategyCurrencyList.add(byCode);
    }

    @Override
    public void initStrategy() {
        strategy = Strategy.dao.findById(11);
    }
}
