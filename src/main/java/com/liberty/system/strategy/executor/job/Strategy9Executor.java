package com.liberty.system.strategy.executor.job;

import com.liberty.common.constant.ConstantDefine;
import com.liberty.common.utils.MailUtil;
import com.liberty.system.blackHouse.RemoveStrategyBh;
import com.liberty.system.model.*;
import com.liberty.system.strategy.executor.Executor;

import java.util.List;
import java.util.Vector;

/**
 * 新第一类买点
 */
public class Strategy9Executor extends StrategyExecutor implements Executor {
    public Strategy9Executor() {
        this.strategy = Strategy.dao.findById(9);
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
        if (stayCurrency.size() != 0) {
            MailUtil.sendMailToBuy(stayCurrency, this.getStrategy());
        }
        System.out.println("策略9执行完毕!");
        long end = System.currentTimeMillis();
        double time = (end - start) * 1.0 / 1000 / 60;
//        MailKit.send("530256489@qq.com", null, "策略[" + strategy.getDescribe() + "]执行耗时提醒!", "此次策略执行耗时:" + time + "分钟!");
        return stayCurrency;
    }

    @Override
    public boolean executeSingle(Currency currency) {
        List<Stroke> strokes = Stroke.dao.listAllByCurrencyId(currency.getId(), ConstantDefine.KLINE_TYPE_K);
        if (strokes.size() < 5) {
            return false;
        }
        int i = strokes.size() - 1;
        Stroke currentStroke = strokes.get(i);
        double currentMax = currentStroke.getMax();
        double currentMin = currentStroke.getMin();
        // 最后一笔方向向上,直接返回
        if (currentStroke.getDirection().equals(ConstantDefine.DIRECTION_UP)) {
            return false;
        }
        // 最后一笔方向向下,找到最近的一个三笔重叠区域
        while (i - 3 > 0 && Stroke.dao.overlap(strokes.get(i - 1), strokes.get(i - 2), strokes.get(i - 3)) != 0) {
            i = i - 2;
        }
        // 没有三笔重叠;或者直到第一根线才有三笔重叠,没有可比较的K线了
        if (i - 3 <= 0) {
            return false;
        }
        // 如果找重叠区域往后找了几笔,需要更新最大值,如果没有往后找,重新赋值也没有影响
        currentMax = strokes.get(i - 1).getMax();
        // 找到三笔重叠,获取重叠区域的最大最小值
        double max = getMax(strokes.get(i - 1).getMax(), strokes.get(i - 2).getMax(), strokes.get(i - 3).getMax());
        double min = getMin(strokes.get(i - 1).getMin(), strokes.get(i - 2).getMin(), strokes.get(i - 3).getMin());
        // 当前笔的最小值没有突破该重叠区域
        if (currentStroke.getMin() >= min) {
            return false;
        }
        while (true) {
            // 用来比较的K线最大值没有突破该区域
            if (strokes.get(i - 4).getMax() <= max) {
                if (i - 5 <= 0) {
                    return false;
                }
                // i-6的最大值在重叠区域最小值以下
                if (strokes.get(i - 6).getMax() < min) {
                    return false;
                }
                // i-6的最大值在重叠区域最大最小值之间
                if (strokes.get(i - 6).getMax() >= min && strokes.get(i - 6).getMax() <= max) {
                    if (strokes.get(i - 5).getMin() < min) {
                        min = strokes.get(i - 5).getMin();
                    }
                }
                i = i - 2;
            } else {
                double compareMax = strokes.get(i - 4).getMax();
                double compareMin = strokes.get(i - 4).getMin();
                // 可比较K线的最大值突破该区域,比较该笔与当前笔对应的macd面积[面积不好算,先比较跌幅吧]
                // 用来对比的可能也不是一笔,得往前找到重叠的才算
                while (i - 7 >= 0 && Stroke.dao.overlap(strokes.get(i - 5), strokes.get(i - 6), strokes.get(i - 7)) != 0) {
                    i = i - 2;
                }
                // 即使上一步的while没有执行i-2操作,下面的重新赋值也没有问题
                compareMax = strokes.get(i - 5).getMax();
                if (currentMax - currentMin < compareMax - compareMin) {
                    return true;
                }
                return false;
            }
        }
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
