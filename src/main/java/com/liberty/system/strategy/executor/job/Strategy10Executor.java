package com.liberty.system.strategy.executor.job;

import com.liberty.common.constant.ConstantDefine;
import com.liberty.system.blackHouse.RemoveStrategyBh;
import com.liberty.system.model.Currency;
import com.liberty.system.model.Kline;
import com.liberty.system.model.Strategy;
import com.liberty.system.model.Stroke;
import com.liberty.system.strategy.executor.Executor;

import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * 新第二类买点
 */
public class Strategy10Executor extends StrategyExecutor implements Executor {
    private Strategy9Executor innerExecutor = new Strategy9Executor();

    public Strategy10Executor() {
        this.strategy = Strategy.dao.findById(10);
    }

    @Override
    public Vector<Currency> execute(String code) {
        long start = System.currentTimeMillis();
        Vector<Currency> stayCurrency = new Vector<>();
        if (code == null) {
            List<Currency> allCurrency = Currency.dao.listAll();
            for (Currency currency : allCurrency) {
                if (RemoveStrategyBh.inBlackHouse(currency)) {// 在小黑屋里面,跳过
                    allCurrency.remove(currency);
                }
            }
            multiProExe(allCurrency, stayCurrency);
        } else {
            Currency currency = Currency.dao.findByCode(code);
            if (!RemoveStrategyBh.inBlackHouse(code)) {// 不在小黑屋里且满足策略
                if (executeSingle(currency)) {
                    if (notExistsRecord(currency)) {
                        stayCurrency.add(currency);
                    }
                }
            }
        }
        sendMailToBuy(stayCurrency, this);
        System.out.println("策略[" + this.getStrategy().getDescribe() + "]执行完毕!");
        long end = System.currentTimeMillis();
        double time = (end - start) * 1.0 / 1000 / 60;
        sendMailTimecost(time);
        return stayCurrency;
    }

    @Override
    public boolean executeSingle(Currency currency) {
        // 以下两条是倒叙排列的
        List<Stroke> strokes = Stroke.dao.getLastSomeByCurrencyId(currency.getId(), ConstantDefine.KLINE_TYPE_K, 2);
        Collections.reverse(strokes);
        if (strokes.size() < 2) {
            return false;
        }
        if (strokes.get(strokes.size() - 1).getDirection().equals(ConstantDefine.DIRECTION_UP)) {
            return false;
        }
        if (strokes.get(strokes.size() - 1).getMin() < strokes.get(strokes.size() - 2).getMin()) {
            return false;
        }
        List<Kline> klinesAfterLastStroke = Kline.dao.getListAfterDate(currency.getId(), ConstantDefine.KLINE_TYPE_K, strokes.get(strokes.size() - 1).getEndDate());
        // 时机已过
        if (klinesAfterLastStroke.size() > 4) {
            return false;
        }
        innerExecutor.setOffset(2);
        return innerExecutor.executeSingle(currency);
    }

    public double getMax(double d1, double d2, double d3) {
        double max = d1;
        if (d2 > max) {
            max = d2;
        }
        if (d3 > max) {
            max = d3;
        }
        return max;
    }

    public double getMin(double d1, double d2, double d3) {
        double min = d1;
        if (d2 < min) {
            min = d2;
        }
        if (d3 < min) {
            min = d3;
        }
        return min;
    }
}
