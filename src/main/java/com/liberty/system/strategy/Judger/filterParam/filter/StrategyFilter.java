package com.liberty.system.strategy.Judger.filterParam.filter;

import com.liberty.common.utils.PlateUtil;
import com.liberty.system.strategy.Judger.filterParam.FilterParam;
import com.liberty.system.model.Currency;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 策略的过滤参数,是一个组合参数
 */
public class StrategyFilter {

    // 所属板块集合
    private List<Currency> plates;
    // 过滤参数集合
    private List<FilterParam> filterParams = new ArrayList<>();

    public List<Currency> getPlates() {
        return plates;
    }

    public void setPlates(List<Currency> plates) {
        this.plates = plates;
    }

    /**
     * 添加过滤参数
     * @param param
     */
    public void addFilterParam(FilterParam param){
        this.filterParams.add(param);
    }


    /**
     * 按板块集合判断
     * @param plates
     * @return
     */
    public List<Currency> judgeInPlate(List<Currency> plates,Date date){
        List<Currency> cs;
        if(null == plates || plates.isEmpty()){
            cs = Currency.dao.listAll();
        }else{
            cs = PlateUtil.queryCurrencyInPlates(plates);
        }
        Iterator<Currency> iterator = cs.iterator();
        while (iterator.hasNext()){
            Currency next = iterator.next();
            if(!judge(next,date)){
                iterator.remove();
            }
        }
        return cs;
    }

    /**
     * 判断单个股票
     * @param currency
     * @return
     */
    public boolean judge(Currency currency,Date date){
        for (FilterParam filterParam : filterParams) {
            if(!filterParam.judge(currency,date)){
                return false;
            }
        }
        return true;
    }
}
