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
    // 向左偏移多少能够适配另一个策略
    private int offset = 0;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Strategy9Executor() {
        this.strategy = Strategy.dao.findById(9);
    }

    @Override
    public Vector<Currency> execute(String code) {
        deleteExecuteRecord(code);
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
        List<Stroke> strokes = Stroke.dao.listAllByCurrencyId(currency.getId(), ConstantDefine.KLINE_TYPE_K);
        if (offset != 0) {
            strokes = strokes.subList(0, strokes.size() - offset);
        }
        if (strokes.size() < 5) {
            return false;
        }
        int i = strokes.size() - 1;
        Stroke currentStroke = strokes.get(i);
        // 最后一笔方向向上,直接返回
        if (currentStroke.getDirection().equals(ConstantDefine.DIRECTION_UP)) {
            return false;
        }
        double currentMax = currentStroke.getMax();
        double currentMin = currentStroke.getMin();
        // 最后一笔方向向下,找到最近的一个三笔重叠区域
        while (i - 3 > 0 && Stroke.dao.overlap(strokes.get(i - 3), strokes.get(i - 2), strokes.get(i - 1)) != 0) {
            i = i - 2;
        }
        // 没有三笔重叠;或者直到第一根线才有三笔重叠,没有可比较的K线了
        if (i - 3 <= 0) {
            return false;
        }
        // 如果找重叠区域往后找了几笔,需要更新最大值,如果没有往后找,重新赋值也没有影响
        currentMax = strokes.get(i).getMax();
        // 找到三笔重叠,获取重叠区域的最大最小值
        double max = getMax(strokes.get(i - 1).getMax(), strokes.get(i - 2).getMax(), strokes.get(i - 3).getMax());
        double min = getMin(strokes.get(i - 1).getMin(), strokes.get(i - 2).getMin(), strokes.get(i - 3).getMin());
        List<Kline> klinesAfterLastStroke = Kline.dao.getListAfterDate(currency.getId(), ConstantDefine.KLINE_TYPE_K, strokes.get(strokes.size() - 1).getEndDate());
        // offset==0表示不是其他策略的前置条件. 判断时机
        if (offset == 0 && !checkPoint(klinesAfterLastStroke, strokes.get(strokes.size() - 1))) {
            return false;
        }
        while (true) {
            if (i - 4 < 0) {
                return false;
            }
            // 当前笔的最小值没有突破该重叠区域
            if (currentMin >= min) {
                return false;
            }
            // 用来比较的K线最大值没有突破该区域
            if (strokes.get(i - 4).getMax() <= max) {
                if (i - 6 < 0) {
                    return false;
                }
                // i-6的最大值在重叠区域最小值以下
                if (strokes.get(i - 6).getMax() < min) {
                    return false;
                }
                max = Math.max(strokes.get(i - 5).getMax(), max);
                min = Math.min(strokes.get(i - 5).getMin(), min);
                i = i - 2;
                continue;
            } else {
                double compareMax = strokes.get(i - 4).getMax();
                double compareMin = strokes.get(i - 4).getMin();
                // 先判断一把,满足就直接返回
                if (currentMax - currentMin < compareMax - compareMin) {
                    return true;
                }
                // 如果比较笔突破中枢的力度超过中枢高度的一半,必须比较
                if (compareMax - max > (max - min) * 0.5) {
                    if (currentMax - currentMin < compareMax - compareMin) {
                        return true;
                    } else {
                        return false;
                    }
                }
                // 重叠则中枢扩展
                if (i - 5 >= 0 && Stroke.dao.overlap(strokes.get(i - 5), strokes.get(i - 4), strokes.get(i - 3)) == 0) {
                    max = Math.max(strokes.get(i - 5).getMax(), max);
                    min = Math.min(strokes.get(i - 5).getMin(), min);
                    i = i - 2;
                    continue;
                }
                // 可比较K线的最大值突破该区域,比较该笔与当前笔对应的macd面积[面积不好算,先比较跌幅吧]
                // 用来对比的可能也不是一笔,得往前找到重叠的才算
                while (i - 7 >= 0 && Stroke.dao.overlap(strokes.get(i - 7), strokes.get(i - 6), strokes.get(i - 5)) != 0) {
                    i = i - 2;
                }
                // 即使上一步的while没有执行i-2操作,下面的重新赋值也没有问题
                compareMax = strokes.get(i - 4).getMax();
                if (currentMax - currentMin < compareMax - compareMin) {
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * 找到条件堪堪成立的那天
     *
     * @param klinesAfterLastStroke
     * @param lastStroke
     * @return
     */
    public boolean checkPoint(List<Kline> klinesAfterLastStroke, Stroke lastStroke) {
        if (klinesAfterLastStroke.size() < 2) {
            return false;
        }
        double max = klinesAfterLastStroke.get(0).getMax();
        double min = klinesAfterLastStroke.get(0).getMin();
        if (lastStroke.getDirection().equals(ConstantDefine.DIRECTION_UP)) {
            return false;
        }
        for (int i = 1; i < klinesAfterLastStroke.size(); i++) {
            // 判断包含
            if (klinesAfterLastStroke.get(i).getMax() <= max && klinesAfterLastStroke.get(i).getMin() >= min) {
                // 向下,包含取下下
                max = klinesAfterLastStroke.get(i).getMax();
                continue;
            }
            if (klinesAfterLastStroke.get(i).getMax() > max) {
                if (i == klinesAfterLastStroke.size() - 1) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
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
