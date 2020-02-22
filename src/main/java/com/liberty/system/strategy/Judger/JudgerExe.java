package com.liberty.system.strategy.Judger;

import com.alibaba.fastjson.JSON;
import com.jfplugin.mail.MailKit;
import com.liberty.common.utils.DateUtil;
import com.liberty.common.utils.MailUtil;
import com.liberty.common.utils.PlateUtil;
import com.liberty.system.strategy.Judger.filterParam.filter.StrategyFilter;
import com.liberty.system.strategy.Judger.filterParam.impl.FinanceParam;
import com.liberty.system.strategy.Judger.filterParam.impl.MaParam;
import com.liberty.system.strategy.Judger.filterParam.impl.VolumeParam;
import com.liberty.system.model.Currency;

import java.util.*;

public class JudgerExe {
    public static void main(String[] args) {
        Currency currency = new Currency();
        currency.setCode("002223");
        currency.setCurrencyType(Currency.CURRENCY_TYPE_SZ);
        List<Currency> currencies = PlateUtil.queryOwnerPlate(currency);
        System.out.println(JSON.toJSONString(currencies));
    }

    public void execute(){
        StrategyFilter strategyFilter = new StrategyFilter();

        FinanceParam financeParam = new FinanceParam();
        financeParam.setIncreaseEnsure(false);
        financeParam.setJzcsylLimit(1.0);
        MaParam maParam = new MaParam();
        VolumeParam volumeParam = new VolumeParam();

        strategyFilter.addFilterParam(financeParam);
        strategyFilter.addFilterParam(maParam);
        strategyFilter.addFilterParam(volumeParam);

        List<Currency> plates = new ArrayList<>();
        Currency c1 = new Currency();
        c1.setCode("BK0888");
        Currency c2 = new Currency();
        c2.setCode("BK0669");
        plates.add(c1);
        plates.add(c2);

        Date date = DateUtil.strDate("2019-04-1 00:00:00", "yyyy-MM-dd HH:mm:ss");
        Map<String,String> result = new LinkedHashMap<>();
        for (int i = 0; i < 60; i++) {
            date = DateUtil.getNextDay(date);

//            List<Currency> cs = strategyFilter.judgeInPlate(plates);
            List<Currency> cs = strategyFilter.judgeInPlate(null,date);
            if(!cs.isEmpty()){
                result.put(date.toLocaleString(),JSON.toJSONString(cs));
            }
        }

        System.out.println("执行完毕!");
    }
}
