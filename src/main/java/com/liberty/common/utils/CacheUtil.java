package com.liberty.common.utils;

import com.jfinal.plugin.activerecord.Page;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Strategy;
import com.liberty.system.query.CurrencyQueryObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheUtil {
    private static Map<String,Map> strategyCurrencyMap = new ConcurrentHashMap();

    public static boolean isExisted(long strategyId,String currencyId){
        String strategyIdStr = String.valueOf(strategyId);
        if(strategyCurrencyMap.containsKey(strategyIdStr)){
            Map currencyMap = strategyCurrencyMap.get(strategyIdStr);
            if(currencyMap.containsKey(currencyId)){
                return true;
            }
        }
        return false;
    }

    public static void initCacheCurrency(long strategyId,List<Currency> currencyList){
        String strategyIdStr = String.valueOf(strategyId);
        Map<String,String> currencyMap = new ConcurrentHashMap<>();
        currencyList.parallelStream().forEach(e->currencyMap.put(e.getCode(),e.getName()));
        strategyCurrencyMap.put(strategyIdStr,currencyMap);
    }

    public static void putCurrency(long strategyId,Currency currency){
        String strategyIdStr = String.valueOf(strategyId);
        Map currencyMap = strategyCurrencyMap.get(strategyIdStr);
        currencyMap.put(currency.getCode(),currency.getName());
    }

    public static void removeCurrency(long strategyId,Currency currency){
        String strategyIdStr = String.valueOf(strategyId);
        Map currencyMap = strategyCurrencyMap.get(strategyIdStr);
        currencyMap.remove(currency.getCode());
    }

    public static void initCache(long strategeId){
        CurrencyQueryObject qo = new CurrencyQueryObject();
        qo.setCurrentPage(1);
        qo.setPageSize(5000);
        qo.setStrategyId("3");
        Page<Currency> currencyPage = Currency.dao.paginateToBuy(qo);
        initCacheCurrency(strategeId,currencyPage.getList());
    }
}
